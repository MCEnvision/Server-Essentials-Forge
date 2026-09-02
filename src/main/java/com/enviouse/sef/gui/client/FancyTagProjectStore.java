package com.enviouse.sef.gui.client;

import com.enviouse.sef.fancytags.FancyTagProjectArchive;
import com.enviouse.sef.kernel.ActionResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class FancyTagProjectStore {
    public static final int MAXIMUM_PROJECTS = 256;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private FancyTagProjectStore() {
    }

    public static Path root(Minecraft minecraft) {
        return Objects.requireNonNull(minecraft, "minecraft")
                .gameDirectory
                .toPath()
                .toAbsolutePath()
                .normalize()
                .resolve("config")
                .resolve("sef-client")
                .resolve("fancy-tags")
                .resolve("projects")
                .normalize();
    }

    public static List<ProjectFile> list(Minecraft minecraft) {
        Path root = root(minecraft);
        List<ProjectFile> result = new ArrayList<>();
        try {
            createRoot(root);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, "*.seftagproject")) {
                for (Path path : stream) {
                    if (result.size() >= MAXIMUM_PROJECTS
                            || Files.isSymbolicLink(path)
                            || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                            || Files.size(path) < 1
                            || Files.size(path) > FancyTagProjectArchive.HARD_MAXIMUM_ARCHIVE_BYTES) {
                        continue;
                    }
                    UUID id = id(path.getFileName().toString());
                    if (id == null) {
                        continue;
                    }
                    Path autosave = autosavePath(root, id);
                    boolean recovery = Files.isRegularFile(autosave, LinkOption.NOFOLLOW_LINKS)
                            && !Files.isSymbolicLink(autosave)
                            && Files.getLastModifiedTime(autosave).compareTo(Files.getLastModifiedTime(path)) > 0;
                    result.add(new ProjectFile(
                            id,
                            path,
                            Files.getLastModifiedTime(path).toInstant(),
                            recovery));
                }
            }
        } catch (IOException | RuntimeException ignored) {
            return List.of();
        }
        result.sort(Comparator.comparing(ProjectFile::modifiedAt).reversed());
        return List.copyOf(result);
    }

    public static ActionResult<Path> save(
            Minecraft minecraft,
            FancyTagProject project,
            boolean autosave
    ) {
        Objects.requireNonNull(project, "project");
        Path root = root(minecraft);
        Path destination = autosave
                ? autosavePath(root, project.id())
                : projectPath(root, project.id());
        Path temporary = null;
        try {
            createRoot(root);
            byte[] archive = encode(project);
            ActionResult<FancyTagProjectArchive.ArchiveFacts> validation =
                    FancyTagProjectArchive.validate(archive);
            if (!validation.successful()) {
                return ActionResult.failure(validation.reason(), validation.detail());
            }
            temporary = Files.createTempFile(root, "project-", ".tmp");
            Files.write(temporary, archive);
            try {
                Files.move(
                        temporary,
                        destination,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            temporary = null;
            if (!autosave) {
                project.markSaved();
                Files.deleteIfExists(autosavePath(root, project.id()));
            }
            return ActionResult.success(destination);
        } catch (IOException | RuntimeException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "local tag project save failed");
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static ActionResult<FancyTagProject> load(
            Minecraft minecraft,
            UUID projectId,
            boolean recoverAutosave
    ) {
        Path root = root(minecraft);
        Path source = recoverAutosave
                ? autosavePath(root, projectId)
                : projectPath(root, projectId);
        if (!source.startsWith(root)
                || Files.isSymbolicLink(source)
                || !Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "local tag project is unavailable");
        }
        try {
            byte[] archive = Files.readAllBytes(source);
            ActionResult<FancyTagProjectArchive.ArchiveFacts> validation =
                    FancyTagProjectArchive.validate(archive);
            if (!validation.successful()) {
                return ActionResult.failure(validation.reason(), validation.detail());
            }
            Map<String, byte[]> entries = entries(archive);
            FancyTagProjectArchive.ProjectManifest manifest = GSON.fromJson(
                    new String(entries.get("manifest.json"), StandardCharsets.UTF_8),
                    FancyTagProjectArchive.ProjectManifest.class);
            EditorState editor = GSON.fromJson(
                    new String(entries.getOrDefault("editor.json", new byte[0]), StandardCharsets.UTF_8),
                    EditorState.class);
            if (editor == null || !editor.projectId().equals(projectId)) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "local tag project metadata is invalid");
            }
            FancyTagProject project = new FancyTagProject(
                    editor.projectId(),
                    editor.name(),
                    manifest.width(),
                    manifest.height());
            List<FancyTagProject.LoadedLayer> layers = new ArrayList<>();
            for (FancyTagProjectArchive.Layer layer : manifest.layers()) {
                byte[] imageBytes = entries.get(layer.file());
                try (NativeImage image = NativeImage.read(imageBytes)) {
                    String displayName = editor.layerNames().getOrDefault(layer.id(), layer.id());
                    layers.add(new FancyTagProject.LoadedLayer(
                            layer.id(),
                            displayName,
                            layer.visible(),
                            layer.opacity(),
                            image.getPixelsRGBA()));
                }
            }
            project.replaceLayers(layers, editor.activeLayer());
            for (int index = 0; index < Math.min(
                    FancyTagProject.MAXIMUM_PALETTE_COLORS,
                    editor.palette().size()); index++) {
                project.setPaletteColor(index, editor.palette().get(index));
            }
            project.markSaved();
            return ActionResult.success(project);
        } catch (IOException | RuntimeException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "local tag project could not be loaded");
        }
    }

    public static ActionResult<Path> exportPng(Minecraft minecraft, FancyTagProject project) {
        Path exports = root(minecraft).getParent().resolve("exports").normalize();
        Path destination = exports.resolve(project.id() + ".png").normalize();
        if (!destination.startsWith(exports)) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "local tag export path is invalid");
        }
        Path temporary = null;
        try (NativeImage image = project.flatten()) {
            Files.createDirectories(exports);
            temporary = Files.createTempFile(exports, "tag-", ".tmp");
            image.writeToFile(temporary);
            try {
                Files.move(
                        temporary,
                        destination,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            temporary = null;
            return ActionResult.success(destination);
        } catch (IOException | RuntimeException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "local tag png export failed");
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static byte[] encode(FancyTagProject project) throws IOException {
        List<FancyTagProjectArchive.Layer> manifestLayers = new ArrayList<>();
        Map<String, String> layerNames = new LinkedHashMap<>();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream output = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            for (FancyTagProject.LayerView layer : project.layers()) {
                String file = "layers/" + layer.id() + ".png";
                manifestLayers.add(new FancyTagProjectArchive.Layer(
                        layer.id(),
                        file,
                        layer.visible(),
                        layer.opacity()));
                layerNames.put(layer.id(), layer.name());
                output.putNextEntry(new ZipEntry(file));
                try (NativeImage image = image(
                        project.width(),
                        project.height(),
                        project.layerPixels(layer.index()))) {
                    output.write(image.asByteArray());
                }
                output.closeEntry();
            }
            FancyTagProjectArchive.ProjectManifest manifest =
                    new FancyTagProjectArchive.ProjectManifest(
                            FancyTagProjectArchive.SCHEMA_VERSION,
                            project.width(),
                            project.height(),
                            FancyTagProjectArchive.HARD_MAXIMUM_HISTORY,
                            manifestLayers,
                            List.of());
            write(output, "manifest.json", GSON.toJson(manifest).getBytes(StandardCharsets.UTF_8));
            EditorState editor = new EditorState(
                    project.id(),
                    project.name(),
                    project.activeLayer(),
                    project.palette(),
                    layerNames,
                    project.revision(),
                    System.currentTimeMillis());
            write(output, "editor.json", GSON.toJson(editor).getBytes(StandardCharsets.UTF_8));
            try (NativeImage flattened = project.flatten()) {
                write(output, "flattened-preview.png", flattened.asByteArray());
            }
        }
        byte[] archive = bytes.toByteArray();
        if (archive.length > FancyTagProjectArchive.HARD_MAXIMUM_ARCHIVE_BYTES) {
            throw new IOException("local tag project archive is too large");
        }
        return archive;
    }

    private static Map<String, byte[]> entries(byte[] archive) throws IOException {
        Map<String, byte[]> result = new LinkedHashMap<>();
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(archive), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                result.put(entry.getName(), input.readAllBytes());
                input.closeEntry();
            }
        }
        return Map.copyOf(result);
    }

    private static NativeImage image(int width, int height, int[] pixels) {
        NativeImage image = new NativeImage(width, height, true);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setPixelRGBA(x, y, pixels[y * width + x]);
            }
        }
        return image;
    }

    private static void write(ZipOutputStream output, String name, byte[] bytes) throws IOException {
        output.putNextEntry(new ZipEntry(name));
        output.write(bytes);
        output.closeEntry();
    }

    private static void createRoot(Path root) throws IOException {
        if (Files.exists(root, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(root)) {
            throw new IOException("local tag project root cannot be a symbolic link");
        }
        Files.createDirectories(root);
    }

    private static Path projectPath(Path root, UUID id) {
        return checked(root, root.resolve(id + ".seftagproject").normalize());
    }

    private static Path autosavePath(Path root, UUID id) {
        return checked(root, root.resolve(id + ".autosave.seftagproject").normalize());
    }

    private static Path checked(Path root, Path path) {
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("local tag project path escapes its root");
        }
        return path;
    }

    private static UUID id(String fileName) {
        try {
            return UUID.fromString(fileName.substring(0, fileName.length() - ".seftagproject".length()));
        } catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
            return null;
        }
    }

    public record ProjectFile(UUID id, Path path, Instant modifiedAt, boolean recoveryAvailable) {
    }

    private record EditorState(
            UUID projectId,
            String name,
            int activeLayer,
            List<Integer> palette,
            Map<String, String> layerNames,
            long projectRevision,
            long savedAtEpochMillis
    ) {
        private EditorState {
            Objects.requireNonNull(projectId, "projectId");
            Objects.requireNonNull(name, "name");
            palette = palette == null ? List.of() : List.copyOf(palette);
            layerNames = layerNames == null ? Map.of() : Map.copyOf(layerNames);
            if (projectRevision < 1L || savedAtEpochMillis < 1L) {
                throw new IllegalArgumentException("local tag editor metadata is invalid");
            }
        }
    }
}
