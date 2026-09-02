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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class FancyTagClientCache {
    private static final int MAXIMUM_DIMENSION = 512;
    private static final int MAXIMUM_TEXTURES = 128;
    private static final int MAXIMUM_CONCURRENT_DOWNLOADS = 2;
    private static final long MAXIMUM_DOWNLOAD_BYTES = 2L * SefProtocol.MAXIMUM_TAG_BYTES;
    private static final long MAXIMUM_GPU_BYTES = 128L * 1024L * 1024L;
    private static final Map<String, TextureEntry> TEXTURES = new LinkedHashMap<>(16, 0.75F, true);
    private static final Map<String, Download> DOWNLOADS = new LinkedHashMap<>();
    private static final Set<String> REQUESTED = new LinkedHashSet<>();
    private static final Set<String> FAILED = new LinkedHashSet<>();

    private FancyTagClientCache() {
    }

    public static void tick(Minecraft minecraft) {
        expireDownloads();
        List<ManifestView> manifests = manifests();
        Set<String> authorized = manifests.stream()
                .map(ManifestView::hash)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        evictUnauthorized(minecraft, authorized);
        for (String hash : ClientProtocolState.takeTagInvalidations()) {
            evict(minecraft, hash, true);
        }
        for (ManifestView manifest : manifests) {
            if (TEXTURES.containsKey(manifest.hash())
                    || REQUESTED.contains(manifest.hash())
                    || FAILED.contains(manifest.hash())) {
                continue;
            }
            Optional<byte[]> cached = readCached(minecraft, manifest);
            if (cached.isPresent() && install(minecraft, manifest.hash(), cached.get())) {
                continue;
            }
            long activeBytes = DOWNLOADS.values().stream()
                    .mapToLong(download -> download.bytes.length)
                    .sum();
            if (DOWNLOADS.size() >= MAXIMUM_CONCURRENT_DOWNLOADS
                    || activeBytes + manifest.byteLength() > MAXIMUM_DOWNLOAD_BYTES) {
                break;
            }
            REQUESTED.add(manifest.hash());
            DOWNLOADS.put(manifest.hash(), new Download(manifest.byteLength()));
            SefClientTransport.requestTag(manifest.hash());
            break;
        }
        ClientProtocolState.takeTagContent().ifPresent(content -> {
            ManifestView current = manifests.stream()
                    .filter(value -> value.hash().equals(content.hash()))
                    .findFirst()
                    .orElse(null);
            REQUESTED.remove(content.hash());
            if (current == null
                    || current.byteLength() != content.content().length
                    || !validHash(content.content(), content.hash())) {
                FAILED.add(content.hash());
                return;
            }
            if (install(minecraft, content.hash(), content.content())) {
                writeCached(minecraft, current, content.content());
            } else {
                FAILED.add(content.hash());
            }
        });
        ClientProtocolState.takeTagContentChunk().ifPresent(chunk ->
                acceptChunk(minecraft, manifests, chunk));
        enforceGpuLimit(minecraft);
    }

    private static void acceptChunk(
            Minecraft minecraft,
            List<ManifestView> manifests,
            SefPayloads.TagContentChunk chunk
    ) {
        ManifestView manifest = manifests.stream()
                .filter(value -> value.hash().equals(chunk.hash()))
                .findFirst()
                .orElse(null);
        Download download = DOWNLOADS.get(chunk.hash());
        if (manifest == null
                || download == null
                || manifest.byteLength() != chunk.totalBytes()
                || download.bytes.length != chunk.totalBytes()
                || download.offset != chunk.offset()) {
            rejectDownload(chunk.hash());
            return;
        }
        System.arraycopy(chunk.content(), 0, download.bytes, chunk.offset(), chunk.content().length);
        download.offset += chunk.content().length;
        if (download.offset < download.bytes.length) {
            SefClientTransport.requestTag(chunk.hash(), download.offset);
            return;
        }
        DOWNLOADS.remove(chunk.hash());
        REQUESTED.remove(chunk.hash());
        if (!validHash(download.bytes, chunk.hash())) {
            FAILED.add(chunk.hash());
            return;
        }
        if (install(minecraft, chunk.hash(), download.bytes)) {
            writeCached(minecraft, manifest, download.bytes);
        } else {
            FAILED.add(chunk.hash());
        }
    }

    private static void rejectDownload(String hash) {
        DOWNLOADS.remove(hash);
        REQUESTED.remove(hash);
        FAILED.add(hash);
    }

    private static void expireDownloads() {
        long now = System.nanoTime();
        List<String> expired = DOWNLOADS.entrySet().stream()
                .filter(entry -> now - entry.getValue().startedNanos > java.time.Duration
                        .ofSeconds(30)
                        .toNanos())
                .map(Map.Entry::getKey)
                .toList();
        expired.forEach(FancyTagClientCache::rejectDownload);
    }

    public static Optional<ResourceLocation> texture() {
        return TEXTURES.values().stream().findFirst().map(TextureEntry::location);
    }

    public static Optional<ResourceLocation> texture(UUID tagId) {
        return ClientProtocolState.tagManifests().stream()
                .filter(entry -> entry.tagId().equals(tagId))
                .findFirst()
                .map(SefPayloads.TagManifestEntry::hash)
                .flatMap(FancyTagClientCache::textureByHash);
    }

    public static Optional<ResourceLocation> textureByHash(String hash) {
        TextureEntry entry = TEXTURES.get(hash);
        return Optional.ofNullable(entry == null ? null : entry.location());
    }

    public static int textureWidth() {
        return TEXTURES.values().stream().findFirst().map(TextureEntry::width).orElse(0);
    }

    public static int textureHeight() {
        return TEXTURES.values().stream().findFirst().map(TextureEntry::height).orElse(0);
    }

    public static Optional<TextureFacts> facts(UUID tagId) {
        return ClientProtocolState.tagManifests().stream()
                .filter(entry -> entry.tagId().equals(tagId))
                .findFirst()
                .map(SefPayloads.TagManifestEntry::hash)
                .map(TEXTURES::get)
                .filter(Objects::nonNull)
                .map(entry -> new TextureFacts(entry.width(), entry.height()));
    }

    public static String serverKey(String address) {
        return sha256(Objects.requireNonNullElse(address, "integrated")
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public static Path cachePath(Path gameDirectory, String serverAddress, String contentHash) {
        if (contentHash == null || !contentHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Invalid tag content hash");
        }
        Path root = Objects.requireNonNull(gameDirectory, "gameDirectory")
                .toAbsolutePath()
                .normalize()
                .resolve("config")
                .resolve("sef-client")
                .resolve("fancy-tags")
                .resolve("cache")
                .resolve(serverKey(serverAddress))
                .resolve("objects")
                .normalize();
        Path result = root.resolve(contentHash + ".png").normalize();
        if (!result.startsWith(root)) {
            throw new IllegalArgumentException("Invalid tag cache path");
        }
        return result;
    }

    public static void close(Minecraft minecraft) {
        for (TextureEntry entry : TEXTURES.values()) {
            minecraft.getTextureManager().release(entry.location());
        }
        TEXTURES.clear();
        DOWNLOADS.clear();
        REQUESTED.clear();
        FAILED.clear();
    }

    private static List<ManifestView> manifests() {
        List<ManifestView> result = new ArrayList<>();
        for (SefPayloads.TagManifestEntry entry : ClientProtocolState.tagManifests()) {
            result.add(new ManifestView(
                    entry.tagId(),
                    entry.hash(),
                    entry.byteLength(),
                    entry.width(),
                    entry.height()));
        }
        if (result.isEmpty()) {
            ClientProtocolState.manifest().ifPresent(entry -> result.add(new ManifestView(
                    entry.tagId(),
                    entry.hash(),
                    entry.byteLength(),
                    MAXIMUM_DIMENSION,
                    MAXIMUM_DIMENSION)));
        }
        result.sort(Comparator.comparing(value -> value.tagId().toString()));
        return List.copyOf(result);
    }

    private static Optional<byte[]> readCached(Minecraft minecraft, ManifestView manifest) {
        try {
            Path path = path(minecraft, manifest.hash());
            if (!Files.isRegularFile(path)
                    || Files.isSymbolicLink(path)
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

    private static void writeCached(Minecraft minecraft, ManifestView manifest, byte[] bytes) {
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
            TextureEntry previous = TEXTURES.remove(hash);
            if (previous != null) {
                minecraft.getTextureManager().release(previous.location());
            }
            int width = image.getWidth();
            int height = image.getHeight();
            ResourceLocation location = minecraft.getTextureManager().register(
                    "sef_fancy_tag_" + hash.substring(0, 16),
                    new DynamicTexture(image));
            TEXTURES.put(hash, new TextureEntry(location, width, height, (long) width * height * 4L));
            FAILED.remove(hash);
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

    private static void evictUnauthorized(Minecraft minecraft, Set<String> authorized) {
        for (String hash : List.copyOf(TEXTURES.keySet())) {
            if (!authorized.contains(hash)) {
                evict(minecraft, hash, false);
            }
        }
        REQUESTED.removeIf(hash -> !authorized.contains(hash));
        DOWNLOADS.keySet().removeIf(hash -> !authorized.contains(hash));
        FAILED.removeIf(hash -> !authorized.contains(hash));
    }

    private static void enforceGpuLimit(Minecraft minecraft) {
        long bytes = TEXTURES.values().stream().mapToLong(TextureEntry::gpuBytes).sum();
        while ((TEXTURES.size() > MAXIMUM_TEXTURES || bytes > MAXIMUM_GPU_BYTES)
                && !TEXTURES.isEmpty()) {
            String oldest = TEXTURES.keySet().iterator().next();
            TextureEntry removed = TEXTURES.get(oldest);
            bytes -= removed == null ? 0L : removed.gpuBytes();
            evict(minecraft, oldest, false);
        }
    }

    private static void evict(Minecraft minecraft, String hash, boolean deleteDisk) {
        TextureEntry removed = TEXTURES.remove(hash);
        if (removed != null) {
            minecraft.getTextureManager().release(removed.location());
        }
        REQUESTED.remove(hash);
        DOWNLOADS.remove(hash);
        FAILED.remove(hash);
        if (deleteDisk && hash != null && hash.matches("[0-9a-f]{64}")) {
            try {
                Files.deleteIfExists(path(minecraft, hash));
            } catch (IOException ignored) {
            }
        }
    }

    public record TextureFacts(int width, int height) {
    }

    private record ManifestView(UUID tagId, String hash, int byteLength, int width, int height) {
    }

    private record TextureEntry(ResourceLocation location, int width, int height, long gpuBytes) {
    }

    private static final class Download {
        private final byte[] bytes;
        private final long startedNanos;
        private int offset;

        private Download(int totalBytes) {
            if (totalBytes < 1 || totalBytes > SefProtocol.MAXIMUM_TAG_BYTES) {
                throw new IllegalArgumentException("tag download size is invalid");
            }
            this.bytes = new byte[totalBytes];
            this.startedNanos = System.nanoTime();
        }
    }
}
