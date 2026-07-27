package com.enviouse.sef.fancytags;

import com.enviouse.sef.kernel.ActionResult;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class FancyTagProjectArchive {
    public static final int SCHEMA_VERSION = 1;
    public static final int HARD_MAXIMUM_ARCHIVE_BYTES = 8 * 1024 * 1024;
    public static final int HARD_MAXIMUM_ENTRIES = 256;
    public static final int HARD_MAXIMUM_ENTRY_BYTES = 2 * 1024 * 1024;
    public static final int HARD_MAXIMUM_EXPANDED_BYTES = 32 * 1024 * 1024;
    public static final int HARD_MAXIMUM_PATH_DEPTH = 4;
    public static final int HARD_MAXIMUM_COMPRESSION_RATIO = 100;
    public static final int HARD_MAXIMUM_LAYERS = 16;
    public static final int HARD_MAXIMUM_HISTORY = 128;
    private static final Gson GSON = new Gson();

    private FancyTagProjectArchive() {
    }

    public static ActionResult<ArchiveFacts> validate(byte[] archive) {
        Objects.requireNonNull(archive, "archive");
        if (archive.length < 1 || archive.length > HARD_MAXIMUM_ARCHIVE_BYTES) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "tag project archive size is invalid");
        }
        int entries = 0;
        long expanded = 0L;
        Set<String> names = new HashSet<>();
        Map<String, byte[]> contents = new LinkedHashMap<>();
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(archive), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = input.getNextEntry()) != null) {
                if (++entries > HARD_MAXIMUM_ENTRIES
                        || entry.isDirectory()
                        || entry.getName() == null
                        || !safeName(entry.getName())
                        || !names.add(entry.getName().toLowerCase(Locale.ROOT))) {
                    return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "tag project archive entry is invalid");
                }
                long entryBytes = 0L;
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                int count;
                while ((count = input.read(buffer)) >= 0) {
                    entryBytes = Math.addExact(entryBytes, count);
                    expanded = Math.addExact(expanded, count);
                    if (entryBytes > HARD_MAXIMUM_ENTRY_BYTES || expanded > HARD_MAXIMUM_EXPANDED_BYTES) {
                        return ActionResult.failure(
                                ActionResult.ReasonCode.INVALID_INPUT,
                                "tag project archive expansion is outside bounds");
                    }
                    output.write(buffer, 0, count);
                }
                long compressed = entry.getCompressedSize();
                if (compressed > 0L && entryBytes / compressed > HARD_MAXIMUM_COMPRESSION_RATIO) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.INVALID_INPUT,
                            "tag project archive compression ratio is outside bounds");
                }
                contents.put(entry.getName(), output.toByteArray());
                input.closeEntry();
            }
        } catch (IOException | ArithmeticException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "tag project archive is malformed");
        }
        byte[] manifestBytes = contents.get("manifest.json");
        byte[] previewBytes = contents.get("flattened-preview.png");
        if (manifestBytes == null || previewBytes == null) {
            return ActionResult.failure(
                    ActionResult.ReasonCode.INVALID_DEFINITION,
                    "tag project archive is missing required entries");
        }
        try {
            ProjectManifest manifest = GSON.fromJson(
                    new String(manifestBytes, StandardCharsets.UTF_8),
                    ProjectManifest.class);
            validateManifest(manifest, contents);
            validatePng(previewBytes, manifest.width(), manifest.height());
            return ActionResult.success(new ArchiveFacts(
                    entries,
                    expanded,
                    Set.copyOf(names),
                    manifest.width(),
                    manifest.height(),
                    manifest.layers().size()));
        } catch (IllegalArgumentException | JsonParseException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "tag project schema is invalid");
        }
    }

    private static boolean safeName(String name) {
        if (name.isBlank()
                || name.length() > 192
                || name.startsWith("/")
                || name.startsWith("\\")
                || name.contains("\\")
                || name.contains(":")
                || name.indexOf('\0') >= 0) {
            return false;
        }
        String[] parts = name.split("/");
        if (parts.length > HARD_MAXIMUM_PATH_DEPTH) {
            return false;
        }
        for (String part : parts) {
            if (part.isBlank() || part.equals(".") || part.equals("..")) {
                return false;
            }
        }
        return name.equals("manifest.json")
                || name.equals("flattened-preview.png")
                || name.equals("palettes.json")
                || name.equals("editor.json")
                || name.matches("layers/[a-z0-9][a-z0-9_-]{0,63}\\.png")
                || name.matches("frames/[a-z0-9][a-z0-9_-]{0,63}/[a-z0-9][a-z0-9_-]{0,63}\\.png");
    }

    private static void validateManifest(ProjectManifest manifest, Map<String, byte[]> contents) {
        if (manifest == null
                || manifest.schemaVersion() != SCHEMA_VERSION
                || manifest.width() < 1
                || manifest.height() < 1
                || manifest.width() > FancyTagObjectStore.HARD_MAXIMUM_WIDTH
                || manifest.height() > FancyTagObjectStore.HARD_MAXIMUM_HEIGHT
                || (long) manifest.width() * manifest.height() > FancyTagObjectStore.HARD_MAXIMUM_PIXELS
                || manifest.layers().isEmpty()
                || manifest.layers().size() > HARD_MAXIMUM_LAYERS
                || !manifest.frames().isEmpty()
                || manifest.historyLimit() < 1
                || manifest.historyLimit() > HARD_MAXIMUM_HISTORY) {
            throw new IllegalArgumentException("tag project manifest is outside bounds");
        }
        Set<String> ids = new HashSet<>();
        Set<String> referenced = new HashSet<>();
        for (Layer layer : manifest.layers()) {
            if (layer == null
                    || layer.id() == null
                    || !layer.id().matches("[a-z0-9][a-z0-9_-]{0,63}")
                    || !ids.add(layer.id())
                    || layer.file() == null
                    || !layer.file().equals("layers/" + layer.id() + ".png")
                    || !referenced.add(layer.file())
                    || layer.opacity() < 0
                    || layer.opacity() > 255) {
                throw new IllegalArgumentException("tag project layer is invalid");
            }
            byte[] image = contents.get(layer.file());
            if (image == null) {
                throw new IllegalArgumentException("tag project layer is missing");
            }
            validatePng(image, manifest.width(), manifest.height());
        }
        for (String name : contents.keySet()) {
            if (name.startsWith("layers/") && !referenced.contains(name)
                    || name.startsWith("frames/")) {
                throw new IllegalArgumentException("tag project contains unreferenced images");
            }
        }
    }

    private static void validatePng(byte[] bytes, int expectedWidth, int expectedHeight) {
        if (bytes == null
                || bytes.length < 24
                || (bytes[0] & 0xff) != 0x89
                || bytes[1] != 'P'
                || bytes[2] != 'N'
                || bytes[3] != 'G'
                || unsignedInt(bytes, 16) != expectedWidth
                || unsignedInt(bytes, 20) != expectedHeight) {
            throw new IllegalArgumentException("tag project image is invalid");
        }
    }

    private static long unsignedInt(byte[] bytes, int offset) {
        return (long) (bytes[offset] & 0xff) << 24
                | (long) (bytes[offset + 1] & 0xff) << 16
                | (long) (bytes[offset + 2] & 0xff) << 8
                | bytes[offset + 3] & 0xffL;
    }

    public record ArchiveFacts(
            int entries,
            long expandedBytes,
            Set<String> normalizedNames,
            int width,
            int height,
            int layers
    ) {
    }

    public record ProjectManifest(
            int schemaVersion,
            int width,
            int height,
            int historyLimit,
            List<Layer> layers,
            List<Frame> frames
    ) {
        public ProjectManifest {
            layers = layers == null ? List.of() : List.copyOf(layers);
            frames = frames == null ? List.of() : List.copyOf(frames);
        }
    }

    public record Layer(String id, String file, boolean visible, int opacity) {
    }

    public record Frame(String id, int durationMillis, List<String> layerIds) {
        public Frame {
            layerIds = layerIds == null ? List.of() : List.copyOf(layerIds);
        }
    }
}
