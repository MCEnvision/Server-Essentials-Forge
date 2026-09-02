package com.enviouse.sef.audit;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

/**
 * Opens the audit file with the operating system's descriptor API and validates
 * the object held by that descriptor. Java NIO does not expose that identity
 * portably, and path checks alone are vulnerable to a check then use race.
 */
final class NativeAuditFileProvider {
    private static final int WINDOWS_OPEN_EXISTING = 3;
    private static final int WINDOWS_OPEN_ALWAYS = 4;
    private static final int WINDOWS_FILE_STANDARD_INFORMATION = 4;
    private static final int WINDOWS_FILE_ATTRIBUTE_TAG_INFORMATION = 9;
    private static final int WINDOWS_FILE_ID_INFORMATION = 18;

    private final Platform platform;
    private final Path directory;

    private NativeAuditFileProvider(Platform platform, Path directory) {
        this.platform = platform;
        this.directory = directory;
    }

    static NativeAuditFileProvider open(Path directory) throws IOException {
        Path normalized = directory.toAbsolutePath().normalize();
        Platform platform = Platform.detect();
        NativeAuditFileProvider provider = new NativeAuditFileProvider(platform, normalized);
        provider.verifyDirectory();
        return provider;
    }

    void validate(Path file) throws IOException {
        if (platform == Platform.WINDOWS) {
            Windows.validate(file);
        } else {
            Posix.validate(directory, file.getFileName().toString());
        }
    }

    void append(Path file, byte[] bytes) throws IOException {
        if (bytes.length == 0) {
            return;
        }
        if (platform == Platform.WINDOWS) {
            Windows.append(file, bytes);
        } else {
            Posix.append(directory, file.getFileName().toString(), bytes);
        }
    }

    private void verifyDirectory() throws IOException {
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("security audit directory is not a directory");
        }
        if (platform == Platform.WINDOWS) {
            Windows.validateDirectory(directory);
        } else {
            Posix.validateDirectory(directory);
        }
    }

    private enum Platform {
        POSIX,
        WINDOWS;

        static Platform detect() throws IOException {
            String name = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (name.contains("win")) {
                return WINDOWS;
            }
            if (name.contains("linux") || name.contains("mac") || name.contains("darwin")) {
                return POSIX;
            }
            throw new IOException("security audit provider does not support this operating system");
        }
    }

    private record PosixIdentity(long device, long inode, long links, long mode, long size) {
        boolean regular() {
            return (mode & 0170000L) == 0100000L;
        }

        boolean directory() {
            return (mode & 0170000L) == 0040000L;
        }

        boolean sameObject(PosixIdentity other) {
            return device == other.device && inode == other.inode;
        }
    }

    private static final class Posix {
        private static final int O_WRONLY = 1;
        private static final int O_RDONLY = 0;
        private static final int O_CREAT_LINUX = 0x40;
        private static final int O_CREAT_MAC = 0x200;
        private static final int O_APPEND_LINUX = 0x400;
        private static final int O_APPEND_MAC = 0x8;
        private static final int O_NOFOLLOW_LINUX = 0x20000;
        private static final int O_NOFOLLOW_MAC = 0x100;
        private static final int AT_FDCWD = -100;

        private interface LibC extends Library {
            int open(String path, int flags, int mode);

            int openat(int directory, String path, int flags, int mode);

            long write(int descriptor, byte[] bytes, int length);

            int fstat(int descriptor, Pointer attributes);

            int close(int descriptor);

            int fsync(int descriptor);
        }

        private static final class Holder {
            private static final LibC INSTANCE = Native.load("c", LibC.class);
        }

        static void validateDirectory(Path directory) throws IOException {
            int descriptor = openDirectory(directory);
            close(descriptor);
        }

        static void validate(Path directory, String fileName) throws IOException {
            int parent = openDirectory(directory);
            int descriptor = -1;
            try {
                descriptor = openAt(parent, fileName, readFlags(), 0);
                PosixIdentity identity = identity(descriptor);
                if (!identity.regular() || identity.links() != 1L) {
                    throw new IOException("security audit file is not a single-link regular file");
                }
            } finally {
                close(descriptor);
                close(parent);
            }
        }

        static void append(Path directory, String fileName, byte[] bytes) throws IOException {
            int parent = openDirectory(directory);
            int descriptor = -1;
            try {
                descriptor = openAt(parent, fileName, appendFlags(), 0600);
                PosixIdentity before = identity(descriptor);
                if (!before.regular() || before.links() != 1L) {
                    throw new IOException("security audit file is not a single-link regular file");
                }
                int offset = 0;
                while (offset < bytes.length) {
                    int length = Math.min(64 * 1024, bytes.length - offset);
                    byte[] chunk = Arrays.copyOfRange(bytes, offset, offset + length);
                    long written = Holder.INSTANCE.write(descriptor, chunk, chunk.length);
                    if (written <= 0L) {
                        throw new IOException("security audit native write failed");
                    }
                    offset += Math.toIntExact(written);
                }
                if (Holder.INSTANCE.fsync(descriptor) != 0) {
                    throw new IOException("security audit native flush failed");
                }
                PosixIdentity after = identity(descriptor);
                if (!before.sameObject(after) || !after.regular() || after.links() != 1L) {
                    throw new IOException("security audit file changed during native write");
                }
            } finally {
                close(descriptor);
                close(parent);
            }
        }

        private static int openDirectory(Path directory) throws IOException {
            Path root = directory.getRoot();
            if (root == null) {
                throw new IOException("security audit directory has no root");
            }
            int descriptor = Holder.INSTANCE.open(root.toString(), readFlags(), 0);
            if (descriptor < 0) {
                throw new IOException("security audit native directory open failed");
            }
            try {
                if (!identity(descriptor).directory()) {
                    throw new IOException("security audit root is not a directory");
                }
                for (Path part : directory) {
                    int next = openAt(descriptor, part.toString(), readFlags(), 0);
                    close(descriptor);
                    descriptor = next;
                    if (!identity(descriptor).directory()) {
                        throw new IOException("security audit path contains a non directory");
                    }
                }
                return descriptor;
            } catch (IOException | RuntimeException exception) {
                close(descriptor);
                throw exception;
            }
        }

        private static int openAt(int parent, String name, int flags, int mode) throws IOException {
            int descriptor = Holder.INSTANCE.openat(parent, name, flags, mode);
            if (descriptor < 0) {
                throw new IOException("security audit native file open failed");
            }
            return descriptor;
        }

        private static PosixIdentity identity(int descriptor) throws IOException {
            com.sun.jna.Memory attributes = new com.sun.jna.Memory(256);
            if (Holder.INSTANCE.fstat(descriptor, attributes) != 0) {
                throw new IOException("security audit native descriptor inspection failed");
            }
            boolean mac = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
            if (mac) {
                return new PosixIdentity(
                        attributes.getInt(0) & 0xffffffffL,
                        attributes.getLong(8),
                        attributes.getShort(6) & 0xffffL,
                        attributes.getShort(4) & 0xffffL,
                        attributes.getLong(96));
            }
            return new PosixIdentity(
                    attributes.getLong(0),
                    attributes.getLong(8),
                    attributes.getLong(16),
                    attributes.getInt(24) & 0xffffffffL,
                    attributes.getLong(48));
        }

        private static int readFlags() {
            return noFollow();
        }

        private static int appendFlags() {
            boolean mac = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
            return O_WRONLY | (mac ? O_CREAT_MAC | O_APPEND_MAC | O_NOFOLLOW_MAC : O_CREAT_LINUX | O_APPEND_LINUX | O_NOFOLLOW_LINUX);
        }

        private static int noFollow() {
            return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac")
                    ? O_NOFOLLOW_MAC
                    : O_NOFOLLOW_LINUX;
        }

        private static void close(int descriptor) {
            if (descriptor >= 0) {
                Holder.INSTANCE.close(descriptor);
            }
        }
    }

    private record WindowsIdentity(long volume, byte[] fileId, int links, long size, boolean directory, boolean reparse) {
        boolean regular() {
            return !directory && !reparse;
        }

        boolean sameObject(WindowsIdentity other) {
            return volume == other.volume && Arrays.equals(fileId, other.fileId);
        }
    }

    private static final class Windows {
        private static final int GENERIC_READ = 0x80000000;
        private static final int FILE_APPEND_DATA = 0x00000004;
        private static final int FILE_READ_ATTRIBUTES = 0x00000080;
        private static final int FILE_SHARE_READ = 0x00000001;
        private static final int FILE_SHARE_WRITE = 0x00000002;
        private static final int FILE_ATTRIBUTE_NORMAL = 0x00000080;
        private static final int FILE_ATTRIBUTE_REPARSE_POINT = 0x00000400;
        private static final int FILE_FLAG_OPEN_REPARSE_POINT = 0x00200000;
        private static final int FILE_FLAG_BACKUP_SEMANTICS = 0x02000000;
        private static final int FILE_TYPE_DISK = 1;

        static void validateDirectory(Path directory) throws IOException {
            WinNT.HANDLE handle = open(directory, GENERIC_READ, WINDOWS_OPEN_EXISTING,
                    FILE_ATTRIBUTE_NORMAL | FILE_FLAG_OPEN_REPARSE_POINT | FILE_FLAG_BACKUP_SEMANTICS);
            try {
                WindowsIdentity identity = identity(handle);
                if (!identity.directory() || identity.reparse()) {
                    throw new IOException("security audit directory is not a safe directory");
                }
            } finally {
                Kernel32.INSTANCE.CloseHandle(handle);
            }
        }

        static void validate(Path file) throws IOException {
            WinNT.HANDLE handle = open(file, GENERIC_READ, WINDOWS_OPEN_EXISTING,
                    FILE_ATTRIBUTE_NORMAL | FILE_FLAG_OPEN_REPARSE_POINT);
            try {
                WindowsIdentity identity = identity(handle);
                if (!identity.regular() || identity.links() != 1) {
                    throw new IOException("security audit file is not a single link regular file");
                }
            } finally {
                Kernel32.INSTANCE.CloseHandle(handle);
            }
        }

        static void append(Path file, byte[] bytes) throws IOException {
            WinNT.HANDLE handle = open(file, FILE_APPEND_DATA | FILE_READ_ATTRIBUTES, WINDOWS_OPEN_ALWAYS,
                    FILE_ATTRIBUTE_NORMAL | FILE_FLAG_OPEN_REPARSE_POINT);
            try {
                WindowsIdentity before = identity(handle);
                if (!before.regular() || before.links() != 1) {
                    throw new IOException("security audit file is not a single link regular file");
                }
                for (int offset = 0; offset < bytes.length;) {
                    int length = Math.min(64 * 1024, bytes.length - offset);
                    byte[] chunk = Arrays.copyOfRange(bytes, offset, offset + length);
                    IntByReference written = new IntByReference();
                    if (!Kernel32.INSTANCE.WriteFile(handle, chunk, chunk.length, written, null)
                            || written.getValue() != chunk.length) {
                        throw new IOException("security audit native write failed");
                    }
                    offset += chunk.length;
                }
                if (!Kernel32.INSTANCE.FlushFileBuffers(handle)) {
                    throw new IOException("security audit native flush failed");
                }
                WindowsIdentity after = identity(handle);
                if (!before.sameObject(after) || !after.regular() || after.links() != 1) {
                    throw new IOException("security audit file changed during native write");
                }
            } finally {
                Kernel32.INSTANCE.CloseHandle(handle);
            }
        }

        private static WinNT.HANDLE open(Path path, int access, int disposition, int attributes) throws IOException {
            WinNT.HANDLE handle = Kernel32.INSTANCE.CreateFile(
                    path.toString(),
                    access,
                    FILE_SHARE_READ | FILE_SHARE_WRITE,
                    null,
                    disposition,
                    attributes,
                    null);
            if (handle == null || Pointer.nativeValue(handle.getPointer()) == -1L) {
                throw new IOException("security audit native file open failed");
            }
            if (Kernel32.INSTANCE.GetFileType(handle) != FILE_TYPE_DISK) {
                Kernel32.INSTANCE.CloseHandle(handle);
                throw new IOException("security audit native handle is not a disk file");
            }
            return handle;
        }

        private static WindowsIdentity identity(WinNT.HANDLE handle) throws IOException {
            WinBase.FILE_STANDARD_INFO standard = new WinBase.FILE_STANDARD_INFO();
            if (!Kernel32.INSTANCE.GetFileInformationByHandleEx(
                    handle,
                    WINDOWS_FILE_STANDARD_INFORMATION,
                    standard.getPointer(),
                    new WinDef.DWORD(standard.size()))) {
                throw new IOException("security audit descriptor attributes are unavailable");
            }
            standard.read();
            WinBase.FILE_ATTRIBUTE_TAG_INFO tag = new WinBase.FILE_ATTRIBUTE_TAG_INFO();
            if (!Kernel32.INSTANCE.GetFileInformationByHandleEx(
                    handle,
                    WINDOWS_FILE_ATTRIBUTE_TAG_INFORMATION,
                    tag.getPointer(),
                    new WinDef.DWORD(tag.size()))) {
                throw new IOException("security audit descriptor reparse state is unavailable");
            }
            tag.read();
            WinBase.FILE_ID_INFO fileId = new WinBase.FILE_ID_INFO();
            if (!Kernel32.INSTANCE.GetFileInformationByHandleEx(
                    handle,
                    WINDOWS_FILE_ID_INFORMATION,
                    fileId.getPointer(),
                    new WinDef.DWORD(fileId.size()))) {
                throw new IOException("security audit descriptor identity is unavailable");
            }
            fileId.read();
            fileId.FileId.read();
            byte[] identifier = new byte[fileId.FileId.Identifier.length];
            for (int index = 0; index < identifier.length; index++) {
                identifier[index] = fileId.FileId.Identifier[index].byteValue();
            }
            return new WindowsIdentity(
                    fileId.VolumeSerialNumber,
                    identifier,
                    standard.NumberOfLinks,
                    standard.EndOfFile.getValue(),
                    standard.Directory,
                    (tag.FileAttributes & FILE_ATTRIBUTE_REPARSE_POINT) != 0);
        }
    }
}
