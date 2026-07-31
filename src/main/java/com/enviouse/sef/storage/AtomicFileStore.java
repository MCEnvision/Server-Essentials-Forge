package com.enviouse.sef.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public final class AtomicFileStore {
    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("uuuuMMddHHmmssSSS")
                    .withZone(java.time.ZoneOffset.UTC);

    private AtomicFileStore() {
    }

    public static void write(Path target, byte[] content) throws IOException {
        write(target, content, false);
    }

    static void write(Path target, byte[] content, boolean forceFallback) throws IOException {
        Path normalized = normalized(target);
        Path parent = requireParent(normalized);
        createSafeDirectories(parent);
        rejectUnsafeExistingTarget(normalized);
        Path temporary = Files.createTempFile(
                parent,
                normalized.getFileName().toString(),
                ".tmp");
        boolean moved = false;
        try {
            writeAndForce(temporary, content);
            if (!forceFallback) {
                try {
                    Files.move(
                            temporary,
                            normalized,
                            StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING);
                    moved = true;
                    forceDirectory(parent);
                    Files.deleteIfExists(previousPath(normalized));
                    forceDirectory(parent);
                    return;
                } catch (AtomicMoveNotSupportedException ignored) {
                }
            }

            Path previous = previousPath(normalized);
            rejectUnsafeExistingTarget(previous);
            if (Files.exists(normalized, LinkOption.NOFOLLOW_LINKS)) {
                durableCopy(normalized, previous);
            }
            Files.move(temporary, normalized, StandardCopyOption.REPLACE_EXISTING);
            moved = true;
            forceDirectory(parent);
        } finally {
            if (!moved) {
                Files.deleteIfExists(temporary);
            }
        }
    }

    public static Path backup(
            Path source,
            Path backupDirectory,
            String suffix,
            Clock clock
    ) throws IOException {
        Path normalizedSource = requireSafeRegularFile(source);
        Path normalizedBackupDirectory = normalized(backupDirectory);
        createSafeDirectories(normalizedBackupDirectory);
        String timestamp = FILE_TIMESTAMP.format(clock.instant());
        Path backup = uniquePath(normalizedBackupDirectory.resolve(
                normalizedSource.getFileName() + "." + timestamp + "." + suffix));
        durableCopy(normalizedSource, backup);
        return backup;
    }

    public static Path quarantine(
            Path source,
            Path quarantineDirectory,
            Clock clock
    ) throws IOException {
        Path normalizedSource = requireSafeRegularFile(source);
        Path normalizedQuarantineDirectory = normalized(quarantineDirectory);
        createSafeDirectories(normalizedQuarantineDirectory);
        String timestamp = FILE_TIMESTAMP.format(clock.instant());
        Path quarantine = uniquePath(normalizedQuarantineDirectory.resolve(
                normalizedSource.getFileName() + "." + timestamp + ".corrupt"));
        try {
            Files.move(normalizedSource, quarantine, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(normalizedSource, quarantine);
        }
        forceDirectory(requireParent(normalizedSource));
        forceDirectory(normalizedQuarantineDirectory);
        return quarantine;
    }

    public static byte[] readBounded(Path source, int maximumBytes) throws IOException {
        if (maximumBytes < 1) {
            throw new IllegalArgumentException("Maximum byte count must be positive");
        }
        Path normalized = requireSafeRegularFile(source);
        Set<OpenOption> options = Set.of(
                StandardOpenOption.READ,
                LinkOption.NOFOLLOW_LINKS);
        try (FileChannel channel = FileChannel.open(normalized, options);
             ByteArrayOutputStream output = new ByteArrayOutputStream(
                     (int) Math.min(channel.size(), maximumBytes))) {
            ByteBuffer buffer = ByteBuffer.allocate(8_192);
            int total = 0;
            while (true) {
                int read = channel.read(buffer);
                if (read < 0) {
                    break;
                }
                if (read == 0) {
                    continue;
                }
                total = Math.addExact(total, read);
                if (total > maximumBytes) {
                    throw new DocumentLimitException("Managed document exceeds its byte limit");
                }
                output.write(buffer.array(), 0, read);
                buffer.clear();
            }
            return output.toByteArray();
        }
    }

    static boolean restorePrevious(Path target, int maximumBytes) throws IOException {
        Path normalized = normalized(target);
        if (Files.exists(normalized, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }
        Path previous = previousPath(normalized);
        if (!Files.exists(previous, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }
        byte[] content = readBounded(previous, maximumBytes);
        write(normalized, content);
        return true;
    }

    static Path previousPath(Path target) {
        Path normalized = normalized(target);
        return normalized.resolveSibling(normalized.getFileName() + ".previous");
    }

    static Path requireSafeRegularFile(Path source) throws IOException {
        Path normalized = normalized(source);
        validateExistingParents(requireParent(normalized));
        BasicFileAttributes attributes = Files.readAttributes(
                normalized,
                BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
        if (attributes.isSymbolicLink() || !attributes.isRegularFile()) {
            throw new UnsafeStoragePathException("Managed path is not a regular file");
        }
        return normalized;
    }

    static void createSafeDirectories(Path directory) throws IOException {
        Path normalized = normalized(directory);
        Path current = normalized.getRoot();
        if (current == null) {
            throw new UnsafeStoragePathException("Managed directory has no root");
        }
        for (Path part : normalized) {
            current = current.resolve(part);
            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS)) {
                BasicFileAttributes attributes = Files.readAttributes(
                        current,
                        BasicFileAttributes.class,
                        LinkOption.NOFOLLOW_LINKS);
                if (attributes.isSymbolicLink() || !attributes.isDirectory()) {
                    throw new UnsafeStoragePathException(
                            "Managed directory path contains a non directory entry");
                }
                continue;
            }
            Path parent = current.getParent();
            Files.createDirectory(current);
            if (parent != null) {
                forceDirectory(parent);
            }
        }
    }

    static void forceDirectory(Path directory) throws IOException {
        Path normalized = normalized(directory);
        validateExistingParents(normalized);
        try (FileChannel channel = FileChannel.open(normalized, StandardOpenOption.READ)) {
            channel.force(true);
        } catch (UnsupportedOperationException exception) {
            throw new IOException("Directory durability is unsupported", exception);
        }
    }

    private static void durableCopy(Path source, Path destination) throws IOException {
        Path normalizedSource = requireSafeRegularFile(source);
        Path normalizedDestination = normalized(destination);
        Path parent = requireParent(normalizedDestination);
        createSafeDirectories(parent);
        rejectUnsafeExistingTarget(normalizedDestination);
        Path temporary = Files.createTempFile(
                parent,
                normalizedDestination.getFileName().toString(),
                ".tmp");
        boolean moved = false;
        Set<OpenOption> readOptions = Set.of(
                StandardOpenOption.READ,
                LinkOption.NOFOLLOW_LINKS);
        try {
            try (FileChannel input = FileChannel.open(normalizedSource, readOptions);
                 FileChannel output = FileChannel.open(
                         temporary,
                         StandardOpenOption.WRITE,
                         StandardOpenOption.TRUNCATE_EXISTING)) {
                long position = 0L;
                long size = input.size();
                while (position < size) {
                    long copied = input.transferTo(position, size - position, output);
                    if (copied <= 0L) {
                        ByteBuffer buffer = ByteBuffer.allocate(8_192);
                        input.position(position);
                        int read = input.read(buffer);
                        if (read < 0) {
                            break;
                        }
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            output.write(buffer);
                        }
                        copied = read;
                    }
                    position = Math.addExact(position, copied);
                }
                output.force(true);
            }
            try {
                Files.move(
                        temporary,
                        normalizedDestination,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(
                        temporary,
                        normalizedDestination,
                        StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;
            forceDirectory(parent);
        } finally {
            if (!moved) {
                Files.deleteIfExists(temporary);
            }
        }
    }

    private static void writeAndForce(Path path, byte[] content) throws IOException {
        try (FileChannel channel = FileChannel.open(
                path,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            ByteBuffer buffer = ByteBuffer.wrap(content);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            channel.force(true);
        }
    }

    private static void rejectUnsafeExistingTarget(Path target) throws IOException {
        if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        BasicFileAttributes attributes = Files.readAttributes(
                target,
                BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS);
        if (attributes.isSymbolicLink() || !attributes.isRegularFile()) {
            throw new UnsafeStoragePathException("Storage target is not a regular file");
        }
    }

    private static void validateExistingParents(Path directory) throws IOException {
        Path normalized = normalized(directory);
        Path current = normalized.getRoot();
        if (current == null) {
            throw new UnsafeStoragePathException("Managed path has no root");
        }
        for (Path part : normalized) {
            current = current.resolve(part);
            BasicFileAttributes attributes = Files.readAttributes(
                    current,
                    BasicFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS);
            if (attributes.isSymbolicLink() || !attributes.isDirectory()) {
                throw new UnsafeStoragePathException(
                        "Managed path contains a symbolic link or non directory parent");
            }
        }
    }

    private static Path uniquePath(Path preferred) throws IOException {
        Path normalized = normalized(preferred);
        if (!Files.exists(normalized, LinkOption.NOFOLLOW_LINKS)) {
            return normalized;
        }
        for (int counter = 1; counter < 10_000; counter++) {
            Path candidate =
                    normalized.resolveSibling(normalized.getFileName() + "." + counter);
            if (!Files.exists(candidate, LinkOption.NOFOLLOW_LINKS)) {
                return candidate;
            }
        }
        throw new IOException("Could not allocate unique storage path");
    }

    private static Path normalized(Path path) {
        return path.toAbsolutePath().normalize();
    }

    private static Path requireParent(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent == null) {
            throw new UnsafeStoragePathException("Storage target has no parent");
        }
        return parent;
    }

    static final class DocumentLimitException extends IOException {
        DocumentLimitException(String message) {
            super(message);
        }
    }

    static final class UnsafeStoragePathException extends IOException {
        UnsafeStoragePathException(String message) {
            super(message);
        }
    }
}
