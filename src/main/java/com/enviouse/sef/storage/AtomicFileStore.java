package com.enviouse.sef.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.format.DateTimeFormatter;

public final class AtomicFileStore {
    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("uuuuMMddHHmmssSSS").withZone(java.time.ZoneOffset.UTC);

    private AtomicFileStore() {
    }

    public static void write(Path target, byte[] content) throws IOException {
        Path parent = target.toAbsolutePath().normalize().getParent();
        if (parent == null) {
            throw new IOException("Storage target has no parent");
        }
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, target.getFileName().toString(), ".tmp");
        boolean moved = false;
        try {
            try (FileChannel channel = FileChannel.open(
                    temporary,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer buffer = ByteBuffer.wrap(content);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
            try {
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;
        } finally {
            if (!moved) {
                Files.deleteIfExists(temporary);
            }
        }
    }

    public static Path backup(Path source, Path backupDirectory, String suffix, Clock clock) throws IOException {
        Files.createDirectories(backupDirectory);
        String timestamp = FILE_TIMESTAMP.format(clock.instant());
        Path backup = uniquePath(
                backupDirectory.resolve(source.getFileName() + "." + timestamp + "." + suffix));
        Files.copy(source, backup, StandardCopyOption.COPY_ATTRIBUTES);
        return backup;
    }

    public static Path quarantine(Path source, Path quarantineDirectory, Clock clock) throws IOException {
        Files.createDirectories(quarantineDirectory);
        String timestamp = FILE_TIMESTAMP.format(clock.instant());
        Path quarantine = uniquePath(
                quarantineDirectory.resolve(source.getFileName() + "." + timestamp + ".corrupt"));
        Files.move(source, quarantine);
        return quarantine;
    }

    private static Path uniquePath(Path preferred) {
        if (!Files.exists(preferred)) {
            return preferred;
        }
        for (int counter = 1; counter < 10_000; counter++) {
            Path candidate = preferred.resolveSibling(preferred.getFileName() + "." + counter);
            if (!Files.exists(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not allocate unique storage path");
    }
}
