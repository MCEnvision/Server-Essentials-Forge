package com.enviouse.sef.gui.client;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.gui.protocol.ClientProtocolState;
import com.enviouse.sef.gui.protocol.SefPayloads;
import com.enviouse.sef.gui.protocol.SefProtocol;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;

public final class FancyTagClientCache {
    private static final int MAXIMUM_DIMENSION = 512;
    private static String handledManifestHash = "";
    private static String failedHash = "";
    private static ResourceLocation texture;
    private static int textureWidth;
    private static int textureHeight;

    private FancyTagClientCache() {
    }

    public static void tick(Minecraft minecraft) {
        SefPayloads.TagManifest manifest = ClientProtocolState.manifest().orElse(null);
        if (manifest != null && !manifest.hash().equals(handledManifestHash)) {
            handledManifestHash = manifest.hash();
            failedHash = "";
            Optional<byte[]> cached = readCached(minecraft, manifest);
            if (cached.isPresent() && install(minecraft, manifest.hash(), cached.get())) {
                return;
            }
            SefClientTransport.requestTag(manifest.hash());
        }
        ClientProtocolState.takeTagContent().ifPresent(content -> {
            SefPayloads.TagManifest current = ClientProtocolState.manifest().orElse(null);
            if (current == null
                    || !current.hash().equals(content.hash())
                    || current.byteLength() != content.content().length
                    || !validHash(content.content(), content.hash())) {
                failedHash = content.hash();
                return;
            }
            if (install(minecraft, content.hash(), content.content())) {
                writeCached(minecraft, current, content.content());
            } else {
                failedHash = content.hash();
            }
        });
    }

    public static Optional<ResourceLocation> texture() {
        return Optional.ofNullable(texture);
    }

    public static int textureWidth() {
        return textureWidth;
    }

    public static int textureHeight() {
        return textureHeight;
    }

    public static String serverKey(String address) {
        return sha256(Objects.requireNonNullElse(address, "integrated").getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public static Path cachePath(Path gameDirectory, String serverAddress, String contentHash) {
        if (contentHash == null || !contentHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Invalid tag content hash");
        }
        Path root = Objects.requireNonNull(gameDirectory, "gameDirectory")
                .toAbsolutePath()
                .normalize()
                .resolve("sef")
                .resolve("cache")
                .resolve("tags")
                .resolve(serverKey(serverAddress))
                .normalize();
        Path result = root.resolve(contentHash + ".png").normalize();
        if (!result.startsWith(root)) {
            throw new IllegalArgumentException("Invalid tag cache path");
        }
        return result;
    }

    public static void close(Minecraft minecraft) {
        if (texture != null) {
            minecraft.getTextureManager().release(texture);
        }
        texture = null;
        textureWidth = 0;
        textureHeight = 0;
        handledManifestHash = "";
        failedHash = "";
    }

    private static Optional<byte[]> readCached(
            Minecraft minecraft,
            SefPayloads.TagManifest manifest
    ) {
        try {
            Path path = path(minecraft, manifest.hash());
            if (!Files.isRegularFile(path)
                    || Files.size(path) != manifest.byteLength()
                    || Files.size(path) > SefProtocol.MAXIMUM_TAG_BYTES) {
                return Optional.empty();
            }
            byte[] bytes = Files.readAllBytes(path);
            return validHash(bytes, manifest.hash()) ? Optional.of(bytes) : Optional.empty();
        } catch (IOException | RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static void writeCached(
            Minecraft minecraft,
            SefPayloads.TagManifest manifest,
            byte[] bytes
    ) {
        if (bytes.length > SefProtocol.MAXIMUM_TAG_BYTES
                || bytes.length != manifest.byteLength()
                || !validHash(bytes, manifest.hash())) {
            return;
        }
        Path temporary = null;
        try {
            Path destination = path(minecraft, manifest.hash());
            Files.createDirectories(destination.getParent());
            temporary = Files.createTempFile(destination.getParent(), "sef-tag-", ".tmp");
            Files.write(temporary, bytes);
            try {
                Files.move(
                        temporary,
                        destination,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | RuntimeException exception) {
            ServerEssentialsForge.LOGGER.debug("[SEF] Fancy tag cache write was skipped");
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static boolean install(Minecraft minecraft, String hash, byte[] bytes) {
        if (!validHash(bytes, hash)
                || bytes.length > SefProtocol.MAXIMUM_TAG_BYTES
                || !safePngDimensions(bytes)) {
            return false;
        }
        try {
            NativeImage image = NativeImage.read(bytes);
            if (image.getWidth() < 1
                    || image.getHeight() < 1
                    || image.getWidth() > MAXIMUM_DIMENSION
                    || image.getHeight() > MAXIMUM_DIMENSION
                    || (long) image.getWidth() * image.getHeight()
                    > (long) MAXIMUM_DIMENSION * MAXIMUM_DIMENSION) {
                image.close();
                return false;
            }
            if (texture != null) {
                minecraft.getTextureManager().release(texture);
            }
            textureWidth = image.getWidth();
            textureHeight = image.getHeight();
            texture = minecraft.getTextureManager().register(
                    "sef_fancy_tag_" + hash.substring(0, 16),
                    new DynamicTexture(image));
            return true;
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    static boolean safePngDimensions(byte[] bytes) {
        if (bytes == null
                || bytes.length < 33
                || (bytes[0] & 0xff) != 0x89
                || bytes[1] != 'P'
                || bytes[2] != 'N'
                || bytes[3] != 'G'
                || (bytes[4] & 0xff) != 0x0d
                || (bytes[5] & 0xff) != 0x0a
                || (bytes[6] & 0xff) != 0x1a
                || (bytes[7] & 0xff) != 0x0a
                || readUnsignedInt(bytes, 8) != 13L
                || bytes[12] != 'I'
                || bytes[13] != 'H'
                || bytes[14] != 'D'
                || bytes[15] != 'R') {
            return false;
        }
        long width = readUnsignedInt(bytes, 16);
        long height = readUnsignedInt(bytes, 20);
        return width >= 1
                && height >= 1
                && width <= MAXIMUM_DIMENSION
                && height <= MAXIMUM_DIMENSION
                && width * height <= (long) MAXIMUM_DIMENSION * MAXIMUM_DIMENSION;
    }

    private static long readUnsignedInt(byte[] bytes, int offset) {
        return (long) (bytes[offset] & 0xff) << 24
                | (long) (bytes[offset + 1] & 0xff) << 16
                | (long) (bytes[offset + 2] & 0xff) << 8
                | bytes[offset + 3] & 0xffL;
    }

    private static Path path(Minecraft minecraft, String contentHash) {
        String serverAddress = minecraft.getCurrentServer() == null
                ? "integrated"
                : minecraft.getCurrentServer().ip;
        return cachePath(minecraft.gameDirectory.toPath(), serverAddress, contentHash);
    }

    private static boolean validHash(byte[] bytes, String hash) {
        return hash != null && hash.equals(sha256(bytes));
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
