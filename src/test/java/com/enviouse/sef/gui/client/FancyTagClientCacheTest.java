package com.enviouse.sef.gui.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FancyTagClientCacheTest {
    @TempDir
    Path directory;

    @Test
    void isolatesServersAndConstrainsContentPaths() {
        String hash = "a".repeat(64);
        Path first = FancyTagClientCache.cachePath(directory, "one.example:25565", hash);
        Path second = FancyTagClientCache.cachePath(directory, "two.example:25565", hash);
        assertFalse(first.equals(second));
        assertEquals(hash + ".png", first.getFileName().toString());
        assertTrue(first.normalize().startsWith(directory.toAbsolutePath().normalize()));
        assertThrows(
                IllegalArgumentException.class,
                () -> FancyTagClientCache.cachePath(directory, "server", "../escape"));
    }

    @Test
    void rejectsImagesWithUnsafePngHeadersBeforeDecoding() {
        assertTrue(FancyTagClientCache.safePngDimensions(pngHeader(16, 16)));
        assertTrue(FancyTagClientCache.safePngDimensions(pngHeader(512, 512)));
        assertFalse(FancyTagClientCache.safePngDimensions(pngHeader(513, 1)));
        assertFalse(FancyTagClientCache.safePngDimensions(pngHeader(1, 513)));
        assertFalse(FancyTagClientCache.safePngDimensions(pngHeader(Integer.MAX_VALUE, Integer.MAX_VALUE)));
        assertFalse(FancyTagClientCache.safePngDimensions(new byte[32]));

        byte[] invalidSignature = pngHeader(16, 16);
        invalidSignature[0] = 0;
        assertFalse(FancyTagClientCache.safePngDimensions(invalidSignature));

        byte[] invalidHeader = pngHeader(16, 16);
        invalidHeader[12] = 'B';
        assertFalse(FancyTagClientCache.safePngDimensions(invalidHeader));
    }

    private static byte[] pngHeader(int width, int height) {
        byte[] bytes = new byte[33];
        bytes[0] = (byte) 0x89;
        bytes[1] = 'P';
        bytes[2] = 'N';
        bytes[3] = 'G';
        bytes[4] = 0x0d;
        bytes[5] = 0x0a;
        bytes[6] = 0x1a;
        bytes[7] = 0x0a;
        bytes[11] = 13;
        bytes[12] = 'I';
        bytes[13] = 'H';
        bytes[14] = 'D';
        bytes[15] = 'R';
        writeInt(bytes, 16, width);
        writeInt(bytes, 20, height);
        return bytes;
    }

    private static void writeInt(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) (value >>> 24);
        bytes[offset + 1] = (byte) (value >>> 16);
        bytes[offset + 2] = (byte) (value >>> 8);
        bytes[offset + 3] = (byte) value;
    }
}
