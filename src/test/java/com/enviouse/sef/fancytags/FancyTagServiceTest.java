package com.enviouse.sef.fancytags;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FancyTagServiceTest {
    @TempDir
    Path directory;

    @Test
    void canonicalizesPublishesAssignsAndReloadsImmutableArtwork() throws Exception {
        FancyTagService service = new FancyTagService(settings(true, Duration.ZERO));
        service.load(directory);
        UUID actor = UUID.randomUUID();
        UUID player = UUID.randomUUID();

        var draft = service.createDraft("founder", "Founder", actor);
        assertTrue(draft.successful());
        byte[] source = png(16, 8, 0x80ff0000);
        var artwork = service.importArtwork(
                "founder",
                source,
                actor,
                draft.value().recordRevision());
        assertTrue(artwork.successful());
        assertEquals(16, artwork.value().width());
        assertEquals(8, artwork.value().height());
        assertEquals(128, artwork.value().pixels());
        assertTrue(artwork.value().contentHash().matches("[0-9a-f]{64}"));

        var imported = service.find("founder").orElseThrow();
        var published = service.changeStatus(
                "founder",
                FancyTagService.TagStatus.PUBLISHED,
                actor,
                imported.recordRevision());
        assertTrue(published.successful());
        var assignment = service.assign(
                "founder",
                FancyTagService.TargetType.PLAYER,
                player.toString(),
                FancyTagService.TagSlot.HUD,
                100,
                null,
                actor);
        assertTrue(assignment.successful());

        var resolved = service.resolve(
                new FancyTagService.ViewerContext(
                        player,
                        player,
                        Set.of(),
                        "",
                        false),
                FancyTagService.RenderContext.HUD,
                ignored -> true);
        assertEquals(1, resolved.size());
        assertEquals(artwork.value().contentHash(), resolved.getFirst().artwork().contentHash());
        assertEquals(source.length > 0, service.readArtwork(artwork.value().contentHash()).length > 0);

        service.flush();
        FancyTagService reloaded = new FancyTagService(settings(true, Duration.ZERO));
        assertEquals(
                FancyTagService.RepositoryState.READY,
                reloaded.load(directory).state());
        assertEquals(1, reloaded.tags().size());
        assertEquals(1, reloaded.assignments().size());
        assertTrue(reloaded.integrity().missing().isEmpty());
        assertTrue(reloaded.integrity().corrupt().isEmpty());
    }

    @Test
    void rejectsUnsafeImagesAndArchiveEntries() throws Exception {
        FancyTagObjectStore store = new FancyTagObjectStore(
                new FancyTagObjectStore.Limits(32, 16, 512, 32_768, 8_192, 1_048_576, 8));
        store.initialize(directory);
        assertFalse(store.canonicalizeAndStore(png(33, 1, 0xffffffff)).successful());
        assertFalse(store.canonicalizeAndStore(new byte[16]).successful());
        assertThrows(IllegalArgumentException.class, () -> store.read("../escape"));

        byte[] unsafe = zip("../escape", "bad", "manifest.json", "{}", "flattened-preview.png", "png");
        assertFalse(FancyTagProjectArchive.validate(unsafe).successful());
        byte[] layer = png(8, 8, 0xffffffff);
        byte[] safe = projectZip(
                """
                {"schemaVersion":1,"width":8,"height":8,"historyLimit":64,
                "layers":[{"id":"base","file":"layers/base.png","visible":true,"opacity":255}],
                "frames":[]}
                """,
                layer,
                layer);
        assertTrue(FancyTagProjectArchive.validate(safe).successful());
    }

    @Test
    void rejectsOversizedExistingObjectBeforeReadingIt() throws Exception {
        FancyTagObjectStore.Limits limits = new FancyTagObjectStore.Limits(
                32, 16, 512, 32_768, 8_192, 1_048_576, 8);
        FancyTagObjectStore store = new FancyTagObjectStore(limits);
        store.initialize(directory);
        byte[] image = png(8, 8, 0xffabcdef);
        String hash = sha256(image);
        Path object = directory.resolve("fancy-tags").resolve("objects").resolve("sha256")
                .resolve(hash.substring(0, 2)).resolve(hash + ".png");
        Files.createDirectories(object.getParent());
        Files.write(object, new byte[limits.maximumEncodedBytes() + 1]);

        assertFalse(store.canonicalizeAndStore(image).successful());
    }

    @Test
    void importInboxRequiresAnUnchangedReviewedCandidate() throws Exception {
        FancyTagService service = new FancyTagService(settings(true, Duration.ZERO));
        service.load(directory);
        UUID actor = UUID.randomUUID();
        var draft = service.createDraft("inbox", "Inbox", actor).value();
        Path inbox = directory.resolve("fancy-tags").resolve("import-inbox");
        Files.write(inbox.resolve("badge.png"), png(8, 8, 0xff00ff00));

        var candidates = service.scanImportInbox();
        assertEquals(1, candidates.size());
        var result = service.approveImport(
                candidates.getFirst().candidateId(),
                "inbox",
                actor,
                draft.recordRevision());
        assertTrue(result.successful());
        assertEquals(1, service.find("inbox").orElseThrow().revisions().size());
    }

    @Test
    void importInboxRejectsSameSizeRewriteWithPreservedTimestamp() throws Exception {
        FancyTagService service = new FancyTagService(settings(true, Duration.ZERO));
        service.load(directory);
        UUID actor = UUID.randomUUID();
        var draft = service.createDraft("rewrite", "Rewrite", actor).value();
        Path inbox = directory.resolve("fancy-tags").resolve("import-inbox");
        Path candidatePath = inbox.resolve("rewrite.png");
        Files.write(candidatePath, png(8, 8, 0xff010203));

        var candidate = service.scanImportInbox().getFirst();
        byte[] changed = Files.readAllBytes(candidatePath);
        changed[changed.length - 1] ^= 1;
        Files.write(candidatePath, changed);
        Files.setLastModifiedTime(
                candidatePath,
                java.nio.file.attribute.FileTime.from(candidate.modifiedAt()));

        assertFalse(service.approveImport(
                candidate.candidateId(),
                "rewrite",
                actor,
                draft.recordRevision()).successful());
        assertTrue(service.find("rewrite").orElseThrow().revisions().isEmpty());
    }

    @Test
    void visibilityAndVanishFailClosed() throws Exception {
        FancyTagService service = new FancyTagService(settings(true, Duration.ZERO));
        service.load(directory);
        UUID actor = UUID.randomUUID();
        UUID subject = UUID.randomUUID();
        var draft = service.createDraft("private", "Private", actor).value();
        service.importArtwork("private", png(8, 8, 0xffffffff), actor, draft.recordRevision());
        var tag = service.find("private").orElseThrow();
        service.changeStatus("private", FancyTagService.TagStatus.PUBLISHED, actor, tag.recordRevision());
        service.assign(
                "private",
                FancyTagService.TargetType.DEFAULT,
                "default",
                FancyTagService.TagSlot.BADGE,
                0,
                Instant.now().plusSeconds(60),
                actor);

        assertEquals(1, service.resolve(
                new FancyTagService.ViewerContext(actor, subject, Set.of(), "", false),
                FancyTagService.RenderContext.GUI,
                ignored -> true).size());
        assertTrue(service.resolve(
                new FancyTagService.ViewerContext(actor, subject, Set.of(), "", true),
                FancyTagService.RenderContext.GUI,
                ignored -> true).isEmpty());
    }

    @Test
    void uploadRequiresOrderedChunksMatchingHashAndCurrentLease() throws Exception {
        FancyTagService service = new FancyTagService(settings(true, Duration.ZERO));
        service.load(directory);
        UUID actor = UUID.randomUUID();
        var draft = service.createDraft("upload", "Upload", actor).value();
        var lease = service.acquireLease("upload", actor, draft.recordRevision(), false).value();
        byte[] image = png(8, 8, 0xff112233);
        String hash = sha256(image);
        Instant now = Instant.parse("2026-07-26T12:00:00Z");
        var upload = service.transfers().begin(
                actor,
                draft.id(),
                lease.leaseId(),
                draft.recordRevision(),
                image.length,
                hash,
                now);
        assertTrue(upload.successful());
        int midpoint = image.length / 2;
        assertFalse(service.transfers().acceptChunk(
                actor,
                upload.value().uploadId(),
                1,
                java.util.Arrays.copyOfRange(image, 0, midpoint),
                now).successful());
        assertTrue(service.transfers().acceptChunk(
                actor,
                upload.value().uploadId(),
                0,
                java.util.Arrays.copyOfRange(image, 0, midpoint),
                now).successful());
        assertFalse(service.transfers().acceptChunk(
                actor,
                upload.value().uploadId(),
                0,
                java.util.Arrays.copyOfRange(image, 0, midpoint),
                now).successful());
        assertTrue(service.transfers().acceptChunk(
                actor,
                upload.value().uploadId(),
                1,
                java.util.Arrays.copyOfRange(image, midpoint, image.length),
                now).successful());
        var completed = service.transfers().finish(actor, upload.value().uploadId(), now);
        assertTrue(completed.successful());
        assertTrue(service.completeUpload(completed.value(), actor).successful());
        assertEquals(1, service.find("upload").orElseThrow().revisions().size());
        assertTrue(service.leases().isEmpty());
    }

    @Test
    void onlyOwnerCanFinishAndIncompleteFinishKeepsUploadActive() throws Exception {
        FancyTagService service = new FancyTagService(settings(true, Duration.ZERO));
        service.load(directory);
        UUID owner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();
        var draft = service.createDraft("owned_upload", "Owned Upload", owner).value();
        var lease = service.acquireLease(
                "owned_upload",
                owner,
                draft.recordRevision(),
                false).value();
        byte[] image = png(8, 8, 0xff445566);
        Instant now = Instant.parse("2026-07-26T12:00:00Z");
        var upload = service.transfers().begin(
                owner,
                draft.id(),
                lease.leaseId(),
                draft.recordRevision(),
                image.length,
                sha256(image),
                now).value();

        assertFalse(service.transfers().finish(attacker, upload.uploadId(), now).successful());
        assertEquals(1, service.transfers().active(now).size());
        assertFalse(service.transfers().finish(owner, upload.uploadId(), now).successful());
        assertEquals(1, service.transfers().active(now).size());
        assertTrue(service.transfers().acceptChunk(
                owner,
                upload.uploadId(),
                0,
                image,
                now).successful());
        assertTrue(service.transfers().finish(owner, upload.uploadId(), now).successful());
        assertFalse(service.transfers().finish(owner, upload.uploadId(), now).successful());
        assertTrue(service.transfers().active(now).isEmpty());
    }

    @Test
    void duplicateRestoreExportAndDeleteKeepImmutableHistory() throws Exception {
        FancyTagService service = new FancyTagService(settings(true, Duration.ZERO));
        service.load(directory);
        UUID actor = UUID.randomUUID();
        var draft = service.createDraft("source", "Source", actor).value();
        service.importArtwork("source", png(8, 8, 0xff000001), actor, draft.recordRevision());
        var first = service.find("source").orElseThrow();
        service.importArtwork("source", png(8, 8, 0xff000002), actor, first.recordRevision());
        var second = service.find("source").orElseThrow();
        var restored = service.restoreRevision("source", 1L, actor, second.recordRevision());
        assertTrue(restored.successful());
        assertEquals(3L, restored.value().revision());
        assertEquals(
                second.revisions().getFirst().contentHash(),
                restored.value().contentHash());
        var copy = service.duplicate("source", "copy", "Copy", actor);
        assertTrue(copy.successful());
        assertEquals(3, copy.value().revisions().size());
        assertTrue(service.exportArtwork("copy", 1L, "copy", actor).successful());
        var manifest = service.exportManifest("copy", 1L, "copy", actor);
        assertTrue(manifest.successful());
        JsonObject exportedManifest =
                JsonParser.parseString(Files.readString(manifest.value())).getAsJsonObject();
        assertEquals("copy", exportedManifest.get("resourceKey").getAsString());
        assertEquals(1L, exportedManifest.get("artworkRevision").getAsLong());
        assertFalse(exportedManifest.has("createdBy"));
        assertFalse(exportedManifest.has("modifiedBy"));
        var project = service.exportProject("copy", 1L, "copy", actor);
        assertTrue(project.successful());
        assertTrue(FancyTagProjectArchive.validate(Files.readAllBytes(project.value())).successful());
        var pending = service.changeStatus(
                "copy",
                FancyTagService.TagStatus.PENDING_DELETE,
                actor,
                copy.value().recordRevision());
        assertTrue(pending.successful());
        assertTrue(service.deletePending(
                "copy",
                actor,
                pending.value().recordRevision()).successful());
        assertTrue(service.find("copy").isEmpty());
    }

    @Test
    void backupRestoreStagesCanonicalObjectsAndAtomicallyRestoresRegistry() throws Exception {
        FancyTagService service = new FancyTagService(settings(true, Duration.ZERO));
        service.load(directory);
        UUID actor = UUID.randomUUID();
        var draft = service.createDraft("backup", "Before", actor).value();
        assertTrue(service.importArtwork(
                "backup",
                png(8, 8, 0xff123456),
                actor,
                draft.recordRevision()).successful());
        var backup = service.createBackup(actor);
        assertTrue(backup.successful());

        var current = service.find("backup").orElseThrow();
        assertTrue(service.updateMetadata(
                "backup",
                "After",
                current.description(),
                "",
                current.visibilityPermission(),
                current.renderContexts(),
                current.alternativeText(),
                actor,
                current.recordRevision()).successful());
        assertEquals("After", service.find("backup").orElseThrow().displayName());

        assertTrue(service.restoreBackup(backup.value().getFileName().toString(), actor).successful());
        assertEquals("Before", service.find("backup").orElseThrow().displayName());
        assertTrue(service.integrity().missing().isEmpty());
        assertTrue(service.integrity().corrupt().isEmpty());
        assertFalse(Files.exists(directory.resolve("fancy-tags").resolve("restore-staging")
                .resolve(backup.value().getFileName().toString())));
    }

    @Test
    void restoreRejectsUnknownArchiveEntriesWithoutChangingRegistry() throws Exception {
        FancyTagService service = new FancyTagService(settings(true, Duration.ZERO));
        service.load(directory);
        UUID actor = UUID.randomUUID();
        service.createDraft("safe", "Safe", actor);
        Path backupRoot = directory.resolve("fancy-tags").resolve("backup-manifests");
        String name = "fancy-tags-1-00000000-0000-0000-0000-000000000000.seftagsbackup";
        Files.write(backupRoot.resolve(name), zip("unknown.txt", "bad"));

        assertFalse(service.restoreBackup(name, actor).successful());
        assertEquals("Safe", service.find("safe").orElseThrow().displayName());
    }

    private static FancyTagService.Settings settings(boolean enabled, Duration settle) {
        return new FancyTagService.Settings(
                enabled,
                64,
                16,
                256,
                16,
                10,
                16,
                3,
                16,
                128,
                Duration.ofSeconds(120),
                Duration.ofSeconds(30),
                settle,
                new FancyTagObjectStore.Limits(
                        64,
                        32,
                        2_048,
                        65_536,
                        262_144,
                        8_388_608,
                        16));
    }

    private static byte[] png(int width, int height, int argb) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, argb);
            }
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private static byte[] zip(String... values) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(output)) {
            for (int index = 0; index < values.length; index += 2) {
                zip.putNextEntry(new java.util.zip.ZipEntry(values[index]));
                zip.write(values[index + 1].getBytes(java.nio.charset.StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return output.toByteArray();
    }

    private static byte[] projectZip(String manifest, byte[] preview, byte[] layer) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(output)) {
            zip.putNextEntry(new java.util.zip.ZipEntry("manifest.json"));
            zip.write(manifest.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new java.util.zip.ZipEntry("flattened-preview.png"));
            zip.write(preview);
            zip.closeEntry();
            zip.putNextEntry(new java.util.zip.ZipEntry("layers/base.png"));
            zip.write(layer);
            zip.closeEntry();
        }
        return output.toByteArray();
    }

    private static String sha256(byte[] bytes) throws Exception {
        return java.util.HexFormat.of().formatHex(
                java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
