package com.enviouse.sef.fancytags;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.AtomicFileStore;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class FancyTagObjectStore {
    public static final int HARD_MAXIMUM_WIDTH = 512;
    public static final int HARD_MAXIMUM_HEIGHT = 256;
    public static final int HARD_MAXIMUM_PIXELS = 65_536;
    public static final int HARD_MAXIMUM_ENCODED_BYTES = 1_048_576;
    public static final int HARD_MAXIMUM_DECODED_BYTES = 4_194_304;
    public static final long HARD_MAXIMUM_STORE_BYTES = 8L * 1024L * 1024L * 1024L;
    public static final int HARD_MAXIMUM_IMPORT_CANDIDATES = 512;
    public static final int HARD_MAXIMUM_BACKUP_OBJECTS = 65_536;
    public static final long HARD_MAXIMUM_BACKUP_MANIFEST_BYTES = 16L * 1024L * 1024L;

    private static final String HASH_PATTERN = "[0-9a-f]{64}";
    private static final String IMPORT_NAME_PATTERN = "[A-Za-z0-9][A-Za-z0-9._ ]{0,127}";

    private final Limits limits;
    private final Map<Path, Observation> importObservations = new LinkedHashMap<>();
    private Path root;
    private Path objectsRoot;
    private Path temporaryRoot;
    private Path inboxRoot;
    private Path exportsRoot;
    private Path backupsRoot;
    private Path restoreStagingRoot;

    public FancyTagObjectStore(Limits limits) {
        this.limits = Objects.requireNonNull(limits, "limits");
    }

    public synchronized void initialize(Path managedRoot) throws IOException {
        Path base = Objects.requireNonNull(managedRoot, "managedRoot").toAbsolutePath().normalize();
        root = base.resolve("fancy-tags").normalize();
        requireInside(base, root);
        objectsRoot = root.resolve("objects").resolve("sha256").normalize();
        temporaryRoot = root.resolve("temp").normalize();
        inboxRoot = root.resolve("import-inbox").normalize();
        exportsRoot = root.resolve("exports").normalize();
        backupsRoot = root.resolve("backup-manifests").normalize();
        restoreStagingRoot = root.resolve("restore-staging").normalize();
        for (Path directory : List.of(
                root,
                objectsRoot,
                temporaryRoot,
                inboxRoot,
                exportsRoot,
                backupsRoot,
                restoreStagingRoot)) {
            requireInside(root, directory);
            if (Files.exists(directory, LinkOption.NOFOLLOW_LINKS)
                    && Files.isSymbolicLink(directory)) {
                throw new IOException("Fancy Tags storage directory cannot be a symbolic link");
            }
            AtomicFileStore.createSafeDirectories(directory);
        }
    }

    public synchronized ActionResult<StoredObject> canonicalizeAndStore(byte[] encoded) {
        try {
            ensureInitialized();
            byte[] canonical = canonicalize(encoded);
            String hash = sha256(canonical);
            Path destination = objectPath(hash);
            if (Files.isRegularFile(destination, LinkOption.NOFOLLOW_LINKS)) {
                byte[] existing = AtomicFileStore.readBounded(
                        destination,
                        limits.maximumEncodedBytes());
                if (!MessageDigest.isEqual(existing, canonical) || !hash.equals(sha256(existing))) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.STORAGE_ERROR,
                            "existing tag object failed integrity verification");
                }
            } else {
                long projected = Math.addExact(storeBytes(), canonical.length);
                if (projected > limits.maximumStoreBytes()) {
                    return ActionResult.failure(
                            ActionResult.ReasonCode.QUOTA_EXCEEDED,
                            "tag object store quota exceeded");
                }
                publish(destination, canonical);
            }
            ImageFacts facts = imageFacts(canonical);
            return ActionResult.success(new StoredObject(
                    hash,
                    canonical.length,
                    facts.width(),
                    facts.height(),
                    Math.multiplyExact(facts.width(), facts.height()),
                    "png"));
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        } catch (IOException | ArithmeticException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "tag object publication failed");
        }
    }

    public synchronized byte[] read(String hash) throws IOException {
        Path path = objectPath(hash);
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(path)) {
            throw new IOException("tag object is unavailable");
        }
        long length = Files.size(path);
        if (length < 1L || length > limits.maximumEncodedBytes()) {
            throw new IOException("tag object length is invalid");
        }
        byte[] bytes = AtomicFileStore.readBounded(path, limits.maximumEncodedBytes());
        if (!hash.equals(sha256(bytes))) {
            throw new IOException("tag object hash mismatch");
        }
        return bytes;
    }

    public synchronized List<ImportCandidate> scanImports(Instant now, Duration settleInterval) {
        ensureInitialized();
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(settleInterval, "settleInterval");
        Map<Path, Observation> next = new LinkedHashMap<>();
        List<ImportCandidate> candidates = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inboxRoot)) {
            for (Path path : stream) {
                if (next.size() >= limits.maximumImportCandidates()) {
                    break;
                }
                Path normalized = path.toAbsolutePath().normalize();
                if (!normalized.startsWith(inboxRoot)
                        || Files.isSymbolicLink(normalized)
                        || !Files.isRegularFile(normalized, LinkOption.NOFOLLOW_LINKS)
                        || !normalized.getFileName().toString().matches(IMPORT_NAME_PATTERN)) {
                    continue;
                }
                long size = Files.size(normalized);
                Instant modified = Files.getLastModifiedTime(normalized, LinkOption.NOFOLLOW_LINKS).toInstant();
                if (size < 1L || size > limits.maximumEncodedBytes()) {
                    continue;
                }
                Observation previous = importObservations.get(normalized);
                Observation current = previous != null
                        && previous.size() == size
                        && previous.modifiedAt().equals(modified)
                        ? previous
                        : new Observation(size, modified, now);
                next.put(normalized, current);
                if (!current.firstObservedAt().plus(settleInterval).isAfter(now)) {
                    byte[] bytes = AtomicFileStore.readBounded(
                            normalized,
                            limits.maximumEncodedBytes());
                    if (bytes.length != size
                            || Files.size(normalized) != size
                            || !Files.getLastModifiedTime(
                            normalized,
                            LinkOption.NOFOLLOW_LINKS).toInstant().equals(modified)
                            || Files.isSymbolicLink(normalized)) {
                        continue;
                    }
                    String contentHash = sha256(bytes);
                    String candidateId = candidateId(
                            normalized.getFileName().toString(),
                            size,
                            modified,
                            contentHash);
                    candidates.add(new ImportCandidate(
                            candidateId,
                            normalized.getFileName().toString(),
                            size,
                            modified,
                            contentHash));
                }
            }
        } catch (IOException ignored) {
            return List.of();
        }
        importObservations.clear();
        importObservations.putAll(next);
        return List.copyOf(candidates);
    }

    public synchronized ActionResult<byte[]> readImportCandidate(
            ImportCandidate candidate,
            Instant now,
            Duration settleInterval
    ) {
        ensureInitialized();
        Objects.requireNonNull(candidate, "candidate");
        Path source = inboxRoot.resolve(candidate.fileName()).toAbsolutePath().normalize();
        if (!source.startsWith(inboxRoot)
                || Files.isSymbolicLink(source)
                || !Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "import candidate is unavailable");
        }
        try {
            long size = Files.size(source);
            Instant modified = Files.getLastModifiedTime(source, LinkOption.NOFOLLOW_LINKS).toInstant();
            Observation observed = importObservations.get(source);
            if (size != candidate.encodedBytes()
                    || !modified.equals(candidate.modifiedAt())
                    || observed == null
                    || observed.firstObservedAt().plus(settleInterval).isAfter(now)
                    || !candidate.candidateId().equals(candidateId(
                    candidate.fileName(),
                    size,
                    modified,
                    candidate.contentHash()))) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "import candidate changed after review");
            }
            byte[] bytes = AtomicFileStore.readBounded(
                    source,
                    limits.maximumEncodedBytes());
            if (bytes.length != size
                    || !candidate.contentHash().equals(sha256(bytes))
                    || Files.size(source) != size
                    || !Files.getLastModifiedTime(
                    source,
                    LinkOption.NOFOLLOW_LINKS).toInstant().equals(modified)
                    || Files.isSymbolicLink(source)) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "import candidate changed while reading");
            }
            return ActionResult.success(bytes);
        } catch (IOException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "import candidate read failed");
        }
    }

    public synchronized ActionResult<Void> rejectImportCandidate(ImportCandidate candidate) {
        ensureInitialized();
        Objects.requireNonNull(candidate, "candidate");
        Path source = inboxRoot.resolve(candidate.fileName()).toAbsolutePath().normalize();
        if (!source.startsWith(inboxRoot) || Files.isSymbolicLink(source)) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "invalid import candidate");
        }
        try {
            byte[] bytes = AtomicFileStore.readBounded(
                    source,
                    limits.maximumEncodedBytes());
            if (Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)
                    && !Files.isSymbolicLink(source)
                    && candidate.contentHash().equals(sha256(bytes))
                    && bytes.length == candidate.encodedBytes()
                    && candidate.modifiedAt().equals(Files.getLastModifiedTime(
                    source,
                    LinkOption.NOFOLLOW_LINKS).toInstant())
                    && candidate.candidateId().equals(candidateId(
                    candidate.fileName(),
                    candidate.encodedBytes(),
                    candidate.modifiedAt(),
                    candidate.contentHash()))) {
                Files.delete(source);
                importObservations.remove(source);
                return ActionResult.success(null);
            }
            return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "import candidate changed after review");
        } catch (IOException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "import candidate rejection failed");
        }
    }

    public synchronized IntegrityReport inspect(Set<String> referencedHashes) {
        ensureInitialized();
        Set<String> missing = new LinkedHashSet<>();
        Set<String> corrupt = new LinkedHashSet<>();
        Set<String> present = objectHashes();
        for (String hash : referencedHashes) {
            if (!present.contains(hash)) {
                missing.add(hash);
                continue;
            }
            try {
                read(hash);
            } catch (IOException exception) {
                corrupt.add(hash);
            }
        }
        Set<String> orphaned = new LinkedHashSet<>(present);
        orphaned.removeAll(referencedHashes);
        return new IntegrityReport(missing, corrupt, orphaned, storeBytes());
    }

    public synchronized GarbageCollectionResult collect(Set<String> referencedHashes, boolean execute) {
        IntegrityReport report = inspect(referencedHashes);
        long bytes = 0L;
        int deleted = 0;
        for (String hash : report.orphaned()) {
            try {
                Path path = objectPath(hash);
                bytes = Math.addExact(bytes, Files.size(path));
                if (execute && Files.deleteIfExists(path)) {
                    deleted++;
                }
            } catch (IOException | ArithmeticException ignored) {
            }
        }
        return new GarbageCollectionResult(
                report.orphaned().size(),
                bytes,
                execute ? deleted : 0,
                execute);
    }

    public synchronized Path createBackupManifest(String manifestJson) throws IOException {
        ensureInitialized();
        String name = "fancy-tags-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + ".json";
        Path destination = backupsRoot.resolve(name).normalize();
        requireInside(backupsRoot, destination);
        publish(destination, manifestJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return destination;
    }

    public synchronized ActionResult<Path> createBackup(String snapshotJson, Set<String> referencedHashes) {
        ensureInitialized();
        Objects.requireNonNull(snapshotJson, "snapshotJson");
        Objects.requireNonNull(referencedHashes, "referencedHashes");
        byte[] manifest = snapshotJson.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (manifest.length < 1L
                || manifest.length > HARD_MAXIMUM_BACKUP_MANIFEST_BYTES
                || referencedHashes.size() > HARD_MAXIMUM_BACKUP_OBJECTS
                || referencedHashes.stream().anyMatch(hash -> hash == null || !hash.matches(HASH_PATTERN))) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "tag backup input is outside bounds");
        }
        String name = "fancy-tags-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + ".seftagsbackup";
        Path destination = backupsRoot.resolve(name).normalize();
        Path temporary = null;
        try {
            requireInside(backupsRoot, destination);
            temporary = Files.createTempFile(temporaryRoot, "backup-", ".tmp");
            try (OutputStream file = Files.newOutputStream(temporary);
                 ZipOutputStream output = new ZipOutputStream(file, java.nio.charset.StandardCharsets.UTF_8)) {
                output.putNextEntry(new ZipEntry("manifest.json"));
                output.write(manifest);
                output.closeEntry();
                for (String hash : referencedHashes.stream().sorted().toList()) {
                    output.putNextEntry(new ZipEntry("objects/" + hash + ".png"));
                    output.write(read(hash));
                    output.closeEntry();
                }
            }
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, destination);
            }
            temporary = null;
            return ActionResult.success(destination);
        } catch (IOException | RuntimeException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "tag backup creation failed");
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public synchronized ActionResult<StagedRestore> stageRestore(String backupName) {
        ensureInitialized();
        String name = Objects.requireNonNullElse(backupName, "").trim();
        if (!name.matches("fancy-tags-[0-9]+-[0-9a-fA-F-]{36}\\.seftagsbackup")) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "tag backup name is invalid");
        }
        Path source = backupsRoot.resolve(name).normalize();
        if (!source.startsWith(backupsRoot)
                || Files.isSymbolicLink(source)
                || !Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag backup is unavailable");
        }
        Path staging = restoreStagingRoot.resolve(UUID.randomUUID().toString()).normalize();
        try {
            requireInside(restoreStagingRoot, staging);
            Files.createDirectory(staging);
            byte[] manifest = null;
            Set<String> hashes = new LinkedHashSet<>();
            long expanded = 0L;
            int entries = 0;
            try (InputStream file = Files.newInputStream(source);
                 ZipInputStream input = new ZipInputStream(file, java.nio.charset.StandardCharsets.UTF_8)) {
                ZipEntry entry;
                byte[] buffer = new byte[8192];
                while ((entry = input.getNextEntry()) != null) {
                    if (++entries > HARD_MAXIMUM_BACKUP_OBJECTS + 1
                            || entry.isDirectory()
                            || entry.getName() == null) {
                        throw new IOException("tag backup entry is invalid");
                    }
                    if (entry.getName().equals("manifest.json")) {
                        if (manifest != null) {
                            throw new IOException("tag backup manifest is duplicated");
                        }
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        int count;
                        while ((count = input.read(buffer)) >= 0) {
                            expanded = Math.addExact(expanded, count);
                            if (output.size() + count > HARD_MAXIMUM_BACKUP_MANIFEST_BYTES) {
                                throw new IOException("tag backup manifest is too large");
                            }
                            output.write(buffer, 0, count);
                        }
                        manifest = output.toByteArray();
                    } else if (entry.getName().matches("objects/[0-9a-f]{64}\\.png")) {
                        String hash = entry.getName().substring(8, 72);
                        if (!hashes.add(hash)) {
                            throw new IOException("tag backup object is duplicated");
                        }
                        Path stagedObject = staging.resolve(hash + ".png").normalize();
                        requireInside(staging, stagedObject);
                        long objectBytes = 0L;
                        try (OutputStream output = Files.newOutputStream(stagedObject)) {
                            int count;
                            while ((count = input.read(buffer)) >= 0) {
                                objectBytes = Math.addExact(objectBytes, count);
                                expanded = Math.addExact(expanded, count);
                                if (objectBytes > limits.maximumEncodedBytes()
                                        || expanded > limits.maximumStoreBytes()
                                        + HARD_MAXIMUM_BACKUP_MANIFEST_BYTES) {
                                    throw new IOException("tag backup expansion is outside bounds");
                                }
                                output.write(buffer, 0, count);
                            }
                        }
                        byte[] bytes = AtomicFileStore.readBounded(
                                stagedObject,
                                limits.maximumEncodedBytes());
                        byte[] canonical = canonicalize(bytes);
                        if (!hash.equals(sha256(bytes)) || !Arrays.equals(bytes, canonical)) {
                            throw new IOException("tag backup object is not canonical");
                        }
                    } else {
                        throw new IOException("tag backup contains an unknown entry");
                    }
                    input.closeEntry();
                }
            }
            if (manifest == null || manifest.length == 0) {
                throw new IOException("tag backup manifest is missing");
            }
            return ActionResult.success(new StagedRestore(
                    staging,
                    new String(manifest, java.nio.charset.StandardCharsets.UTF_8),
                    Set.copyOf(hashes)));
        } catch (IOException | RuntimeException exception) {
            deleteTree(staging);
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "tag backup validation failed");
        }
    }

    public synchronized ActionResult<Void> commitRestore(StagedRestore staged) {
        ensureInitialized();
        Objects.requireNonNull(staged, "staged");
        if (!staged.root().startsWith(restoreStagingRoot)
                || !Files.isDirectory(staged.root(), LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(staged.root())) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "tag restore staging is invalid");
        }
        try {
            long projected = storeBytes();
            for (String hash : staged.hashes()) {
                Path source = staged.root().resolve(hash + ".png").normalize();
                requireInside(staged.root(), source);
                byte[] bytes = AtomicFileStore.readBounded(
                        source,
                        limits.maximumEncodedBytes());
                if (!hash.equals(sha256(bytes)) || !Arrays.equals(bytes, canonicalize(bytes))) {
                    return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "staged tag object changed");
                }
                Path destination = objectPath(hash);
                if (!Files.isRegularFile(destination, LinkOption.NOFOLLOW_LINKS)) {
                    projected = Math.addExact(projected, bytes.length);
                    if (projected > limits.maximumStoreBytes()) {
                        return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "tag object store quota exceeded");
                    }
                }
            }
            for (String hash : staged.hashes()) {
                Path destination = objectPath(hash);
                if (!Files.isRegularFile(destination, LinkOption.NOFOLLOW_LINKS)) {
                    publish(destination, AtomicFileStore.readBounded(
                            staged.root().resolve(hash + ".png"),
                            limits.maximumEncodedBytes()));
                } else {
                    read(hash);
                }
            }
            return ActionResult.success(null);
        } catch (IOException | RuntimeException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "tag restore publication failed");
        }
    }

    public synchronized void discardRestore(StagedRestore staged) {
        if (staged != null && restoreStagingRoot != null && staged.root().startsWith(restoreStagingRoot)) {
            deleteTree(staged.root());
        }
    }

    public synchronized List<String> backups() {
        ensureInitialized();
        List<String> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupsRoot, "*.seftagsbackup")) {
            for (Path path : stream) {
                if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path)) {
                    result.add(path.getFileName().toString());
                }
            }
        } catch (IOException ignored) {
        }
        result.sort(String::compareTo);
        return List.copyOf(result);
    }

    public synchronized ActionResult<Path> exportObject(String hash, String requestedName) {
        ensureInitialized();
        String name = Objects.requireNonNullElse(requestedName, "").trim().toLowerCase(java.util.Locale.ROOT);
        if (!name.matches("[a-z0-9][a-z0-9_.-]{0,63}")) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "tag export name is invalid");
        }
        try {
            byte[] bytes = read(hash);
            Path destination = exportsRoot.resolve(name + "-" + hash.substring(0, 12) + ".png").normalize();
            requireInside(exportsRoot, destination);
            publish(destination, bytes);
            return ActionResult.success(destination);
        } catch (IOException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "tag export failed");
        }
    }

    public synchronized ActionResult<Path> exportArtifact(
            byte[] bytes,
            String requestedName,
            String extension
    ) {
        ensureInitialized();
        Objects.requireNonNull(bytes, "bytes");
        String name = Objects.requireNonNullElse(requestedName, "")
                .trim()
                .toLowerCase(Locale.ROOT);
        String suffix = Objects.requireNonNullElse(extension, "")
                .trim()
                .toLowerCase(Locale.ROOT);
        if (!name.matches("[a-z0-9][a-z0-9_.-]{0,63}")
                || !Set.of("json", "seftagproject").contains(suffix)
                || bytes.length < 1
                || bytes.length > FancyTagProjectArchive.HARD_MAXIMUM_ARCHIVE_BYTES) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "tag export artifact is invalid");
        }
        try {
            String digest = sha256(bytes);
            Path destination = exportsRoot.resolve(
                    name + "-" + digest.substring(0, 12) + "." + suffix).normalize();
            requireInside(exportsRoot, destination);
            publish(destination, bytes);
            return ActionResult.success(destination);
        } catch (IOException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.STORAGE_ERROR, "tag export failed");
        }
    }

    public synchronized Path root() {
        ensureInitialized();
        return root;
    }

    public Limits limits() {
        return limits;
    }

    private byte[] canonicalize(byte[] input) throws IOException {
        Objects.requireNonNull(input, "input");
        if (input.length < 1 || input.length > limits.maximumEncodedBytes()) {
            throw new IllegalArgumentException("tag image encoded length is outside bounds");
        }
        ImageFacts declared = imageFacts(input);
        long pixels = Math.multiplyExact((long) declared.width(), declared.height());
        long decodedBytes = Math.multiplyExact(pixels, 4L);
        if (declared.width() > limits.maximumWidth()
                || declared.height() > limits.maximumHeight()
                || pixels > limits.maximumPixels()
                || decodedBytes > limits.maximumDecodedBytes()) {
            throw new IllegalArgumentException("tag image dimensions are outside bounds");
        }
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(input));
        if (decoded == null
                || decoded.getWidth() != declared.width()
                || decoded.getHeight() != declared.height()) {
            throw new IllegalArgumentException("tag image could not be decoded safely");
        }
        BufferedImage normalized = new BufferedImage(
                decoded.getWidth(),
                decoded.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        var graphics = normalized.createGraphics();
        try {
            graphics.setComposite(java.awt.AlphaComposite.Src);
            graphics.drawImage(decoded, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length);
        if (!ImageIO.write(normalized, "png", output)) {
            throw new IOException("canonical png encoder is unavailable");
        }
        byte[] result = output.toByteArray();
        if (result.length < 1 || result.length > limits.maximumEncodedBytes()) {
            throw new IllegalArgumentException("canonical tag image is outside encoded bounds");
        }
        return result;
    }

    private ImageFacts imageFacts(byte[] encoded) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(encoded))) {
            if (input == null) {
                throw new IllegalArgumentException("tag image stream is invalid");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("tag image format is unsupported");
            }
            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!format.equals("png") && !format.equals("jpeg") && !format.equals("jpg")) {
                    throw new IllegalArgumentException("tag image format is unsupported");
                }
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1) {
                    throw new IllegalArgumentException("tag image dimensions are invalid");
                }
                return new ImageFacts(width, height);
            } finally {
                reader.dispose();
            }
        }
    }

    private Set<String> objectHashes() {
        Set<String> result = new LinkedHashSet<>();
        try (DirectoryStream<Path> prefixes = Files.newDirectoryStream(objectsRoot)) {
            for (Path prefix : prefixes) {
                if (Files.isSymbolicLink(prefix) || !Files.isDirectory(prefix, LinkOption.NOFOLLOW_LINKS)) {
                    continue;
                }
                try (DirectoryStream<Path> files = Files.newDirectoryStream(prefix, "*.png")) {
                    for (Path file : files) {
                        String name = file.getFileName().toString();
                        String hash = name.substring(0, name.length() - 4);
                        if (hash.matches(HASH_PATTERN)
                                && Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)
                                && !Files.isSymbolicLink(file)) {
                            result.add(hash);
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return Set.copyOf(result);
    }

    private long storeBytes() {
        long total = 0L;
        for (String hash : objectHashes()) {
            try {
                total = Math.addExact(total, Files.size(objectPath(hash)));
            } catch (IOException | ArithmeticException exception) {
                return Long.MAX_VALUE;
            }
        }
        return total;
    }

    private Path objectPath(String hash) {
        ensureInitialized();
        if (hash == null || !hash.matches(HASH_PATTERN)) {
            throw new IllegalArgumentException("invalid tag object hash");
        }
        Path result = objectsRoot
                .resolve(hash.substring(0, 2))
                .resolve(hash + ".png")
                .normalize();
        requireInside(objectsRoot, result);
        return result;
    }

    private void publish(Path destination, byte[] bytes) throws IOException {
        requireInside(root, destination);
        AtomicFileStore.createSafeDirectories(destination.getParent());
        Path temporary = Files.createTempFile(temporaryRoot, "object-", ".tmp");
        try {
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
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void deleteTree(Path root) {
        if (root == null || !Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    private static String candidateId(String name, long size, Instant modified, String contentHash) {
        return sha256((name + "\n" + size + "\n" + modified.toEpochMilli() + "\n" + contentHash)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .substring(0, 24);
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("sha256 is unavailable", exception);
        }
    }

    private static void requireInside(Path root, Path path) {
        if (!path.toAbsolutePath().normalize().startsWith(root.toAbsolutePath().normalize())) {
            throw new IllegalArgumentException("path escapes the Fancy Tags owned root");
        }
    }

    private void ensureInitialized() {
        if (root == null) {
            throw new IllegalStateException("Fancy Tags object store is not initialized");
        }
    }

    public record Limits(
            int maximumWidth,
            int maximumHeight,
            int maximumPixels,
            int maximumEncodedBytes,
            int maximumDecodedBytes,
            long maximumStoreBytes,
            int maximumImportCandidates
    ) {
        public Limits {
            if (maximumWidth < 1 || maximumWidth > HARD_MAXIMUM_WIDTH
                    || maximumHeight < 1 || maximumHeight > HARD_MAXIMUM_HEIGHT
                    || maximumPixels < 1 || maximumPixels > HARD_MAXIMUM_PIXELS
                    || maximumEncodedBytes < 1 || maximumEncodedBytes > HARD_MAXIMUM_ENCODED_BYTES
                    || maximumDecodedBytes < 4 || maximumDecodedBytes > HARD_MAXIMUM_DECODED_BYTES
                    || maximumStoreBytes < maximumEncodedBytes
                    || maximumStoreBytes > HARD_MAXIMUM_STORE_BYTES
                    || maximumImportCandidates < 1
                    || maximumImportCandidates > HARD_MAXIMUM_IMPORT_CANDIDATES) {
                throw new IllegalArgumentException("Fancy Tags object limits are outside hard bounds");
            }
        }

        public static Limits defaults() {
            return new Limits(256, 64, 16_384, 262_144, 1_048_576, 1_073_741_824L, 128);
        }
    }

    public record StoredObject(
            String hash,
            int encodedBytes,
            int width,
            int height,
            int pixels,
            String format
    ) {
    }

    public record ImportCandidate(
            String candidateId,
            String fileName,
            long encodedBytes,
            Instant modifiedAt,
            String contentHash
    ) {
        public ImportCandidate {
            Objects.requireNonNull(candidateId, "candidateId");
            Objects.requireNonNull(fileName, "fileName");
            Objects.requireNonNull(modifiedAt, "modifiedAt");
            Objects.requireNonNull(contentHash, "contentHash");
            if (!candidateId.matches("[0-9a-f]{24}")
                    || !fileName.matches(IMPORT_NAME_PATTERN)
                    || encodedBytes < 1L
                    || encodedBytes > HARD_MAXIMUM_ENCODED_BYTES
                    || !contentHash.matches(HASH_PATTERN)) {
                throw new IllegalArgumentException("invalid import candidate");
            }
        }
    }

    public record IntegrityReport(
            Set<String> missing,
            Set<String> corrupt,
            Set<String> orphaned,
            long storedBytes
    ) {
        public IntegrityReport {
            missing = Set.copyOf(missing);
            corrupt = Set.copyOf(corrupt);
            orphaned = Set.copyOf(orphaned);
        }
    }

    public record GarbageCollectionResult(
            int candidates,
            long candidateBytes,
            int deleted,
            boolean executed
    ) {
    }

    public record StagedRestore(Path root, String snapshotJson, Set<String> hashes) {
        public StagedRestore {
            Objects.requireNonNull(root, "root");
            Objects.requireNonNull(snapshotJson, "snapshotJson");
            hashes = Set.copyOf(hashes);
        }
    }

    private record Observation(long size, Instant modifiedAt, Instant firstObservedAt) {
    }

    private record ImageFacts(int width, int height) {
    }
}
