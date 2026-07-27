package com.enviouse.sef.gui.client;

import com.enviouse.sef.gui.protocol.ClientProtocolState;
import com.enviouse.sef.gui.protocol.SefPayloads;
import com.enviouse.sef.gui.protocol.SefProtocol;
import com.enviouse.sef.kernel.ActionResult;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class FancyTagStudioScreen extends Screen {
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 244;
    private static final int AUTOSAVE_TICKS = 200;
    private final Screen parent;
    private final Section section;
    private FancyTagProject project;
    private Tool tool = Tool.PENCIL;
    private int color = 0xffffffff;
    private int canvasX;
    private int canvasY;
    private int canvasWidth;
    private int canvasHeight;
    private int scale;
    private int dragStartX = -1;
    private int dragStartY = -1;
    private int autosaveTicks;
    private EditBox text;
    private String status = "local projects never leave this client without an explicit upload";
    private SefPayloads.TagManifestEntry selectedServerTag;
    private SefPayloads.TagManagerEntry selectedManagerEntry;
    private SefPayloads.TagManagerSnapshot managerSnapshot;
    private int managerPage = 1;
    private UploadStage uploadStage = UploadStage.IDLE;
    private byte[] uploadBytes;
    private UUID leaseId;
    private UUID uploadId;
    private int uploadOffset;
    private int uploadChunk;

    public FancyTagStudioScreen(Screen parent) {
        this(parent, Section.GALLERY, null);
    }

    public static FancyTagStudioScreen open(Screen parent, String section) {
        if ("local".equals(section)) {
            return new FancyTagStudioScreen(parent, Section.GALLERY, null);
        }
        try {
            return new FancyTagStudioScreen(
                    parent,
                    Section.valueOf(section.toUpperCase(java.util.Locale.ROOT)),
                    null);
        } catch (IllegalArgumentException exception) {
            return new FancyTagStudioScreen(parent);
        }
    }

    private FancyTagStudioScreen(Screen parent, Section section, FancyTagProject project) {
        super(Component.literal("Fancy Tags studio"));
        this.parent = parent instanceof FancyTagStudioScreen studio ? studio.parent : parent;
        this.section = section;
        this.project = project;
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        addTabs(left, top);
        switch (section) {
            case GALLERY -> initGallery(left, top);
            case EDITOR -> initEditor(left, top);
            case MANAGER, DETAIL, ASSIGNMENTS, REVISIONS, IMPORT, TRANSFER,
                    CACHE, INTEGRITY, AUDIT, SETTINGS -> initServerSection(left, top);
        }
        addRenderableWidget(Button.builder(
                        Component.literal("Done"),
                        ignored -> onClose())
                .bounds(left + PANEL_WIDTH - 58, top + PANEL_HEIGHT - 22, 50, 16)
                .build());
    }

    private void addTabs(int left, int top) {
        Section[] tabs = Section.values();
        for (int index = 0; index < tabs.length; index++) {
            Section tab = tabs[index];
            int row = index / 6;
            int column = index % 6;
            Button button = Button.builder(
                            Component.literal(tab.label()),
                            ignored -> open(tab, tab == Section.EDITOR ? project : null))
                    .bounds(left + 8 + column * 58, top + 22 + row * 18, 55, 16)
                    .tooltip(Tooltip.create(Component.literal(tab.description())))
                    .build();
            button.active = tab != section && (tab.local() || managerAvailable());
            addRenderableWidget(button);
        }
    }

    private void initGallery(int left, int top) {
        List<FancyTagProjectStore.ProjectFile> projects = FancyTagProjectStore.list(Minecraft.getInstance());
        addRenderableWidget(Button.builder(
                        Component.literal("New 32 by 16"),
                        ignored -> {
                            FancyTagProject created = new FancyTagProject(
                                    UUID.randomUUID(),
                                    "New tag",
                                    32,
                                    16);
                            open(Section.EDITOR, created);
                        })
                .bounds(left + 10, top + 64, 100, 18)
                .build());
        int shown = Math.min(8, projects.size());
        for (int index = 0; index < shown; index++) {
            FancyTagProjectStore.ProjectFile file = projects.get(index);
            int column = index % 2;
            int row = index / 2;
            Button button = Button.builder(
                            Component.literal(file.id().toString().substring(0, 8)
                                    + (file.recoveryAvailable() ? " recovery" : "")),
                            ignored -> {
                                ActionResult<FancyTagProject> loaded = FancyTagProjectStore.load(
                                        Minecraft.getInstance(),
                                        file.id(),
                                        file.recoveryAvailable());
                                if (loaded.successful()) {
                                    open(Section.EDITOR, loaded.value());
                                } else {
                                    status = loaded.detail();
                                }
                            })
                    .bounds(left + 10 + column * 170, top + 88 + row * 22, 164, 18)
                    .tooltip(Tooltip.create(Component.literal(file.path().getFileName().toString())))
                    .build();
            addRenderableWidget(button);
        }
    }

    private void initEditor(int left, int top) {
        if (project == null) {
            project = new FancyTagProject(UUID.randomUUID(), "New tag", 32, 16);
        }
        int availableWidth = 230;
        int availableHeight = 122;
        scale = Math.max(1, Math.min(
                8,
                Math.min(availableWidth / project.width(), availableHeight / project.height())));
        canvasWidth = project.width() * scale;
        canvasHeight = project.height() * scale;
        canvasX = left + 8;
        canvasY = top + 82;
        int controlsX = left + 248;
        addRenderableWidget(Button.builder(
                        Component.literal(tool.label()),
                        ignored -> {
                            tool = tool.next();
                            ignored.setMessage(Component.literal(tool.label()));
                        })
                .bounds(controlsX, top + 64, 100, 18)
                .tooltip(Tooltip.create(Component.literal("Pencil, eraser, fill, line, rectangle, or text")))
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal("Undo"),
                        ignored -> project.undo())
                .bounds(controlsX, top + 86, 48, 18)
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal("Redo"),
                        ignored -> project.redo())
                .bounds(controlsX + 52, top + 86, 48, 18)
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal("Save"),
                        ignored -> show(FancyTagProjectStore.save(Minecraft.getInstance(), project, false)))
                .bounds(controlsX, top + 108, 48, 18)
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal("PNG"),
                        ignored -> show(FancyTagProjectStore.exportPng(Minecraft.getInstance(), project)))
                .bounds(controlsX + 52, top + 108, 48, 18)
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal("Add layer"),
                        ignored -> {
                            if (!project.addLayer("Layer " + (project.layers().size() + 1))) {
                                status = "layer limit reached";
                            }
                        })
                .bounds(controlsX, top + 130, 100, 18)
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal("Remove layer"),
                        ignored -> {
                            if (!project.removeActiveLayer()) {
                                status = "the base layer cannot be removed";
                            }
                        })
                .bounds(controlsX, top + 152, 100, 18)
                .build());
        addRenderableWidget(Button.builder(
                        Component.literal("Local preview"),
                        ignored -> {
                            FancyTagLocalOverlay.set(project);
                            status = "local preview enabled with visible local provenance";
                        })
                .bounds(controlsX, top + 174, 100, 18)
                .build());
        Button upload = Button.builder(
                        Component.literal("Upload"),
                        ignored -> beginUpload())
                .bounds(controlsX, top + 196, 100, 18)
                .tooltip(Tooltip.create(Component.literal("Requires a selected server tag and an edit lease")))
                .build();
        upload.active = managerAvailable() && selectedTagId() != null && uploadStage == UploadStage.IDLE;
        addRenderableWidget(upload);
        text = new EditBox(font, left + 8, top + 210, 170, 18, Component.literal("Pixel text"));
        text.setMaxLength(64);
        text.setHint(Component.literal("Text tool value"));
        addRenderableWidget(text);
        for (int index = 0; index < Math.min(8, project.palette().size()); index++) {
            int paletteColor = project.palette().get(index);
            int paletteIndex = index;
            Button palette = Button.builder(
                            Component.literal(" "),
                            ignored -> {
                                color = paletteColor;
                                status = "palette color " + (paletteIndex + 1) + " selected";
                            })
                    .bounds(left + 184 + index * 8, top + 218, 8, 8)
                    .build();
            addRenderableWidget(palette);
        }
    }

    private void initServerSection(int left, int top) {
        String sectionId = sectionId();
        if (managerSnapshot == null || !managerSnapshot.section().equals(sectionId)) {
            SefClientTransport.queryTagManager(sectionId, managerPage, "");
        }
        List<SefPayloads.TagManagerEntry> entries = managerSnapshot == null
                || !managerSnapshot.section().equals(sectionId)
                ? List.of()
                : managerSnapshot.entries();
        int shown = Math.min(8, entries.size());
        for (int index = 0; index < shown; index++) {
            SefPayloads.TagManagerEntry entry = entries.get(index);
            int column = index % 2;
            int row = index / 2;
            Button button = Button.builder(
                            Component.literal(fit(entry.title(), 140)),
                            ignored -> {
                                selectedManagerEntry = entry;
                                if (entry.tagId() != null) {
                                    selectedServerTag = ClientProtocolState.tagManifests().stream()
                                            .filter(manifest -> manifest.tagId().equals(entry.tagId()))
                                            .findFirst()
                                            .orElse(null);
                                }
                                status = entry.resourceKey() + ", " + entry.status()
                                        + ", revision " + entry.revision();
                            })
                    .bounds(left + 10 + column * 170, top + 64 + row * 22, 164, 18)
                    .tooltip(Tooltip.create(Component.literal(entry.subtitle())))
                    .build();
            addRenderableWidget(button);
        }
        if (managerSnapshot != null && managerSnapshot.section().equals(sectionId)) {
            Button previous = Button.builder(
                            Component.literal("<"),
                            ignored -> {
                                managerPage = Math.max(1, managerSnapshot.page() - 1);
                                SefClientTransport.queryTagManager(sectionId, managerPage, "");
                            })
                    .bounds(left + 10, top + 154, 24, 16)
                    .build();
            previous.active = managerSnapshot.page() > 1;
            addRenderableWidget(previous);
            Button next = Button.builder(
                            Component.literal(">"),
                            ignored -> {
                                managerPage = Math.min(managerSnapshot.pages(), managerSnapshot.page() + 1);
                                SefClientTransport.queryTagManager(sectionId, managerPage, "");
                            })
                    .bounds(left + 38, top + 154, 24, 16)
                    .build();
            next.active = managerSnapshot.page() < managerSnapshot.pages();
            addRenderableWidget(next);
            addRenderableWidget(Button.builder(
                            Component.literal("Refresh"),
                            ignored -> SefClientTransport.queryTagManager(sectionId, managerPage, ""))
                    .bounds(left + 66, top + 154, 60, 16)
                    .build());
        }
        if (selectedTagId() != null) {
            String[] operations = {"publish", "hide", "archive", "restore", "lease_acquire"};
            for (int index = 0; index < operations.length; index++) {
                String operation = operations[index];
                addRenderableWidget(Button.builder(
                                Component.literal(operation.replace('_', ' ')),
                                ignored -> SefClientTransport.mutateTag(
                                        operation,
                                        selectedTagReference(),
                                        selectedTagRevision(),
                                        ""))
                        .bounds(left + 10 + index * 68, top + 164, 64, 18)
                        .build());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (project != null && project.dirty() && ++autosaveTicks >= AUTOSAVE_TICKS) {
            autosaveTicks = 0;
            ActionResult<?> result = FancyTagProjectStore.save(Minecraft.getInstance(), project, true);
            if (!result.successful()) {
                status = result.detail();
            }
        }
        ClientProtocolState.takeTagOperationResult().ifPresent(this::handleOperationResult);
        ClientProtocolState.takeTagManagerSnapshot().ifPresent(snapshot -> {
            managerSnapshot = snapshot;
            managerPage = snapshot.page();
            status = snapshot.section() + ", page " + snapshot.page()
                    + " of " + snapshot.pages()
                    + ", registry revision " + snapshot.registryRevision();
            rebuildWidgets();
        });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        SefScreenBackground.render(this, graphics, mouseX, mouseY, partialTick);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        SefVanillaTheme.panel(
                graphics,
                left,
                top,
                PANEL_WIDTH,
                PANEL_HEIGHT,
                SefPayloads.PanelView.CATEGORY);
        graphics.drawCenteredString(font, title, width / 2, top + 8, SefVanillaTheme.TEXT);
        if (section == Section.EDITOR && project != null) {
            renderCanvas(graphics);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(
                font,
                fit(status, PANEL_WIDTH - 76),
                left + 8,
                top + PANEL_HEIGHT - 18,
                SefVanillaTheme.MUTED_TEXT,
                false);
    }

    private void renderCanvas(GuiGraphics graphics) {
        graphics.fill(
                canvasX - 2,
                canvasY - 2,
                canvasX + canvasWidth + 2,
                canvasY + canvasHeight + 2,
                0xff373737);
        int[] pixels = project.flattenedPixels();
        for (int y = 0; y < project.height(); y++) {
            for (int x = 0; x < project.width(); x++) {
                int checker = (x + y & 1) == 0 ? 0xffa0a0a0 : 0xff707070;
                int pixel = pixels[y * project.width() + x];
                int rendered = pixel >>> 24 == 0 ? checker : pixel;
                graphics.fill(
                        canvasX + x * scale,
                        canvasY + y * scale,
                        canvasX + (x + 1) * scale,
                        canvasY + (y + 1) * scale,
                        rendered);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (section == Section.EDITOR && project != null && button == 0 && insideCanvas(mouseX, mouseY)) {
            int x = pixelX(mouseX);
            int y = pixelY(mouseY);
            dragStartX = x;
            dragStartY = y;
            switch (tool) {
                case PENCIL -> {
                    project.beginEdit();
                    project.setPixel(x, y, color);
                }
                case ERASER -> {
                    project.beginEdit();
                    project.setPixel(x, y, 0);
                }
                case FILL -> project.fill(x, y, color);
                case TEXT -> project.rasterizeText(x, y, text == null ? "" : text.getValue(), color);
                case LINE, RECTANGLE -> {
                }
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (section == Section.EDITOR
                && project != null
                && button == 0
                && insideCanvas(mouseX, mouseY)
                && (tool == Tool.PENCIL || tool == Tool.ERASER)) {
            project.setPixel(pixelX(mouseX), pixelY(mouseY), tool == Tool.ERASER ? 0 : color);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (section == Section.EDITOR && project != null && button == 0 && dragStartX >= 0) {
            int endX = Math.clamp(pixelX(mouseX), 0, project.width() - 1);
            int endY = Math.clamp(pixelY(mouseY), 0, project.height() - 1);
            if (tool == Tool.LINE) {
                project.line(dragStartX, dragStartY, endX, endY, color);
            } else if (tool == Tool.RECTANGLE) {
                project.rectangle(dragStartX, dragStartY, endX, endY, color, false);
            } else {
                project.endEdit();
            }
            dragStartX = -1;
            dragStartY = -1;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        if (uploadId != null && uploadStage != UploadStage.COMPLETE) {
            SefClientTransport.cancelTagUpload(uploadId);
        }
        if (leaseId != null && uploadStage != UploadStage.COMPLETE && selectedTagId() != null) {
            SefClientTransport.mutateTag(
                    "lease_release",
                    selectedTagReference(),
                    selectedTagRevision(),
                    leaseId.toString());
        }
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public Component getNarrationMessage() {
        return Component.literal("Fancy Tags studio. " + section.label() + ". " + status + ".");
    }

    private void beginUpload() {
        if (project == null || selectedTagId() == null || !managerAvailable()) {
            status = "select a server tag before uploading";
            return;
        }
        try (NativeImage image = project.flatten()) {
            uploadBytes = image.asByteArray();
        } catch (IOException exception) {
            status = "could not encode the project preview";
            return;
        }
        uploadStage = UploadStage.WAITING_FOR_LEASE;
        SefClientTransport.mutateTag(
                "lease_acquire",
                selectedTagReference(),
                selectedTagRevision(),
                "");
        status = "requesting an edit lease";
    }

    private void handleOperationResult(SefPayloads.TagOperationResult result) {
        if (!result.successful()) {
            status = result.reason() + ", " + result.detail();
            uploadStage = UploadStage.IDLE;
            return;
        }
        switch (uploadStage) {
            case WAITING_FOR_LEASE -> {
                leaseId = result.operationId();
                if (leaseId == null || uploadBytes == null) {
                    uploadStage = UploadStage.IDLE;
                    status = "the server did not return an edit lease";
                    return;
                }
                uploadStage = UploadStage.WAITING_FOR_BEGIN;
                SefClientTransport.beginTagUpload(
                        selectedTagId(),
                        leaseId,
                        selectedTagRevision(),
                        uploadBytes);
                status = "starting bounded upload";
            }
            case WAITING_FOR_BEGIN -> {
                uploadId = result.operationId();
                uploadOffset = 0;
                uploadChunk = 0;
                if (uploadId == null) {
                    uploadStage = UploadStage.IDLE;
                    status = "the server did not allocate an upload";
                    return;
                }
                sendNextChunk();
            }
            case WAITING_FOR_CHUNK -> sendNextChunk();
            case WAITING_FOR_FINISH -> {
                uploadStage = UploadStage.COMPLETE;
                uploadBytes = null;
                uploadId = null;
                leaseId = null;
                status = "server tag artwork uploaded";
            }
            default -> status = result.detail().isBlank() ? result.reason() : result.detail();
        }
    }

    private void sendNextChunk() {
        if (uploadBytes == null || uploadId == null) {
            uploadStage = UploadStage.IDLE;
            return;
        }
        if (uploadOffset >= uploadBytes.length) {
            uploadStage = UploadStage.WAITING_FOR_FINISH;
            SefClientTransport.finishTagUpload(uploadId);
            status = "publishing uploaded artwork";
            return;
        }
        int end = Math.min(
                uploadBytes.length,
                uploadOffset + SefProtocol.MAXIMUM_TAG_UPLOAD_CHUNK_BYTES);
        byte[] chunk = Arrays.copyOfRange(uploadBytes, uploadOffset, end);
        SefClientTransport.sendTagUploadChunk(uploadId, uploadChunk++, chunk);
        uploadOffset = end;
        uploadStage = UploadStage.WAITING_FOR_CHUNK;
        status = "uploading " + uploadOffset + " of " + uploadBytes.length + " bytes";
    }

    private void open(Section next, FancyTagProject selectedProject) {
        if (minecraft != null) {
            FancyTagStudioScreen replacement = new FancyTagStudioScreen(parent, next, selectedProject);
            replacement.selectedServerTag = selectedServerTag;
            replacement.selectedManagerEntry = selectedManagerEntry;
            replacement.managerSnapshot = managerSnapshot;
            replacement.managerPage = managerPage;
            minecraft.setScreen(replacement);
        }
    }

    private boolean managerAvailable() {
        return ClientProtocolState.negotiated(SefProtocol.Feature.FANCY_TAGS_MANAGER);
    }

    private UUID selectedTagId() {
        if (selectedManagerEntry != null && selectedManagerEntry.tagId() != null) {
            return selectedManagerEntry.tagId();
        }
        return selectedServerTag == null ? null : selectedServerTag.tagId();
    }

    private String selectedTagReference() {
        if (selectedManagerEntry != null && selectedManagerEntry.tagId() != null) {
            return selectedManagerEntry.resourceKey();
        }
        return selectedServerTag == null ? "" : selectedServerTag.resourceKey();
    }

    private long selectedTagRevision() {
        if (selectedManagerEntry != null && selectedManagerEntry.tagId() != null) {
            return selectedManagerEntry.revision();
        }
        return selectedServerTag == null ? 0L : selectedServerTag.tagRevision();
    }

    private String sectionId() {
        return switch (section) {
            case MANAGER -> "manager";
            case DETAIL -> "detail";
            case ASSIGNMENTS -> "assign";
            case REVISIONS -> "history";
            case IMPORT -> "import";
            case TRANSFER -> "transfer";
            case CACHE -> "cache";
            case INTEGRITY -> "integrity";
            case AUDIT -> "audit";
            case SETTINGS -> "settings";
            default -> "gallery";
        };
    }

    private boolean insideCanvas(double x, double y) {
        return x >= canvasX
                && y >= canvasY
                && x < canvasX + canvasWidth
                && y < canvasY + canvasHeight;
    }

    private int pixelX(double mouseX) {
        return (int) (mouseX - canvasX) / Math.max(1, scale);
    }

    private int pixelY(double mouseY) {
        return (int) (mouseY - canvasY) / Math.max(1, scale);
    }

    private void show(ActionResult<?> result) {
        status = result.successful()
                ? "operation completed"
                : result.reason() + ", " + result.detail();
    }

    private String fit(String value, int pixels) {
        if (font.width(value) <= pixels) {
            return value;
        }
        return font.plainSubstrByWidth(value, Math.max(0, pixels - font.width("..."))) + "...";
    }

    private enum Tool {
        PENCIL("Pencil"),
        ERASER("Eraser"),
        FILL("Fill"),
        LINE("Line"),
        RECTANGLE("Rectangle"),
        TEXT("Text");

        private final String label;

        Tool(String label) {
            this.label = label;
        }

        private String label() {
            return label;
        }

        private Tool next() {
            Tool[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private enum UploadStage {
        IDLE,
        WAITING_FOR_LEASE,
        WAITING_FOR_BEGIN,
        WAITING_FOR_CHUNK,
        WAITING_FOR_FINISH,
        COMPLETE
    }

    private enum Section {
        GALLERY("Gallery", "Local only project gallery", true),
        EDITOR("Editor", "Bounded local pixel editor", true),
        MANAGER("Manager", "Server tag manager", false),
        DETAIL("Detail", "Selected server tag details", false),
        ASSIGNMENTS("Assign", "Server tag assignments", false),
        REVISIONS("History", "Immutable artwork revisions", false),
        IMPORT("Import", "Bounded import and upload", false),
        TRANSFER("Transfer", "Transfer progress and cancellation", false),
        CACHE("Cache", "Client texture cache", false),
        INTEGRITY("Integrity", "Server artwork integrity", false),
        AUDIT("Audit", "Tag audit events", false),
        SETTINGS("Settings", "Fancy Tags presentation settings", true);

        private final String label;
        private final String description;
        private final boolean local;

        Section(String label, String description, boolean local) {
            this.label = label;
            this.description = description;
            this.local = local;
        }

        private String label() {
            return label;
        }

        private String description() {
            return description;
        }

        private boolean local() {
            return local;
        }
    }
}
