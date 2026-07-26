package com.enviouse.sef.commandlog;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.storage.InstantJsonAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class FileLogSink {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .disableHtmlEscaping()
            .create();
    private static final DateTimeFormatter ARCHIVE_TIME =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH-mm-ss'Z'").withZone(ZoneOffset.UTC);
    private static final int DURABLE_RECENT_LIMIT = 4096;

    private final AtomicLong accepted = new AtomicLong();
    private final AtomicLong written = new AtomicLong();
    private final AtomicLong dropped = new AtomicLong();
    private final AtomicLong failures = new AtomicLong();
    private final AtomicLong rotations = new AtomicLong();
    private final Deque<CommandEventJournal.CommandRecord> durableRecent = new ArrayDeque<>();
    private final Deque<ConnectionRecord> durableConnections = new ArrayDeque<>();
    private volatile Settings settings = Settings.fromConfig();
    private volatile State state = State.DISABLED;
    private volatile String failureDetail = "";
    private volatile boolean accepting;
    private volatile boolean running;
    private volatile boolean rotateRequested;
    private volatile boolean flushRequested;
    private volatile boolean connectionStreamEnabled;
    private volatile Thread writerThread;
    private ArrayBlockingQueue<CommandEventJournal.CommandRecord> queue;
    private ArrayBlockingQueue<ConnectionRecord> connectionQueue;
    private Path serverDirectory;
    private Path root;
    private Path commandDirectory;
    private Path archiveDirectory;
    private Path stateDirectory;
    private Path currentFile;
    private Path textFile;
    private Path connectionDirectory;
    private Path connectionArchiveDirectory;
    private Path connectionFile;
    private UUID sessionId = UUID.randomUUID();
    private Instant activeSince = Instant.EPOCH;
    private int archiveSequence;
    private volatile String commandTextFormat =
            "[{timestamp}] [{source}] [{dimension} {x} {y} {z}] {actor}: {command} {result}";
    private volatile CaptureFilter captureFilter = CaptureFilter.defaults();

    public synchronized boolean startConfigured(Path serverDirectory) {
        this.serverDirectory = Objects.requireNonNull(serverDirectory, "serverDirectory")
                .toAbsolutePath()
                .normalize();
        settings = Settings.fromConfig();
        connectionStreamEnabled = settings.connectionEvents();
        return !settings.enabled() || enable();
    }

    public boolean reload() {
        Settings replacement = Settings.fromConfig();
        final boolean wasRunning;
        final boolean requiresRestart;
        synchronized (this) {
            wasRunning = running;
            requiresRestart = wasRunning && !replacement.equals(settings);
            settings = replacement;
            connectionStreamEnabled = replacement.connectionEvents();
        }
        if (!replacement.enabled()) {
            disable();
            return true;
        }
        if (!wasRunning) {
            return enable();
        }
        if (requiresRestart) {
            disable();
            return enable();
        }
        return true;
    }

    public synchronized boolean enable() {
        if (running) {
            return true;
        }
        if (writerThread != null) {
            failureDetail = "logger shutdown is still in progress";
            state = State.DEGRADED;
            return false;
        }
        if (serverDirectory == null) {
            failureDetail = "server directory is not initialized";
            state = State.FAILED;
            return false;
        }
        try {
            initializePaths();
            queue = new ArrayBlockingQueue<>(settings.queueCapacity());
            connectionQueue = new ArrayBlockingQueue<>(settings.queueCapacity());
            sessionId = UUID.randomUUID();
            activeSince = Instant.now();
            archiveSequence = 0;
            writeSessionMarker();
        } catch (IOException | RuntimeException exception) {
            fail("logger initialization failed", exception);
            return false;
        }
        accepting = true;
        running = true;
        state = State.HEALTHY;
        failureDetail = "";
        writerThread = Thread.ofPlatform()
                .daemon(true)
                .name("sef-file-log")
                .start(this::writerLoop);
        return true;
    }

    public void disable() {
        final Thread thread;
        final long timeoutMillis;
        synchronized (this) {
            accepting = false;
            running = false;
            thread = writerThread;
            if (thread == null) {
                if (state != State.FAILED) {
                    state = State.DISABLED;
                }
                return;
            }
            timeoutMillis = settings.shutdownTimeout().toMillis();
        }
        thread.interrupt();
        try {
            thread.join(timeoutMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        synchronized (this) {
            if (thread.isAlive()) {
                writeIncompleteMarker();
                state = State.DEGRADED;
                failureDetail = "shutdown flush timed out";
                thread.interrupt();
            } else {
                deleteSessionMarker();
                if (state != State.FAILED) {
                    state = State.DISABLED;
                }
            }
            if (!thread.isAlive() && writerThread == thread) {
                writerThread = null;
            }
        }
    }

    public void shutdown() {
        disable();
    }

    public boolean submit(CommandEventJournal.CommandRecord record) {
        if (!accepting || record == null) {
            return false;
        }
        if (!captureFilter.matches(record)) {
            return false;
        }
        byte[] encoded = encode(record).getBytes(StandardCharsets.UTF_8);
        if (encoded.length > settings.maximumRecordBytes()) {
            signalDrop("record exceeds maximum encoded size");
            return false;
        }
        ArrayBlockingQueue<CommandEventJournal.CommandRecord> activeQueue = queue;
        if (activeQueue == null || !activeQueue.offer(record)) {
            signalDrop("queue is full");
            return false;
        }
        accepted.incrementAndGet();
        return true;
    }

    public boolean submitConnection(ConnectionRecord record) {
        if (!accepting || !connectionStreamEnabled || record == null) {
            return false;
        }
        byte[] encoded = encode(record).getBytes(StandardCharsets.UTF_8);
        if (encoded.length > settings.maximumRecordBytes()) {
            signalDrop("connection record exceeds maximum encoded size");
            return false;
        }
        ArrayBlockingQueue<ConnectionRecord> activeQueue = connectionQueue;
        if (activeQueue == null || !activeQueue.offer(record)) {
            signalDrop("connection queue is full");
            return false;
        }
        accepted.incrementAndGet();
        return true;
    }

    public synchronized boolean setConnectionStreamEnabled(boolean enabled) {
        if (enabled && !running) {
            return false;
        }
        connectionStreamEnabled = enabled;
        Thread thread = writerThread;
        if (thread != null) {
            thread.interrupt();
        }
        return true;
    }

    public boolean connectionStreamEnabled() {
        return connectionStreamEnabled && accepting;
    }

    public synchronized List<ConnectionRecord> recentConnections(int maximum) {
        int limit = Math.clamp(maximum, 1, 256);
        List<ConnectionRecord> result = new ArrayList<>(limit);
        var iterator = durableConnections.descendingIterator();
        while (iterator.hasNext() && result.size() < limit) {
            result.add(iterator.next());
        }
        return List.copyOf(result);
    }

    public void requestRotate() {
        rotateRequested = true;
        Thread thread = writerThread;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void requestFlush() {
        flushRequested = true;
        Thread thread = writerThread;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public synchronized Health health() {
        return new Health(
                state,
                accepting,
                (queue == null ? 0 : queue.size())
                        + (connectionQueue == null ? 0 : connectionQueue.size()),
                settings.queueCapacity(),
                accepted.get(),
                written.get(),
                dropped.get(),
                failures.get(),
                rotations.get(),
                sessionId,
                activeSince,
                failureDetail);
    }

    public synchronized List<CommandEventJournal.CommandRecord> recent(int maximum) {
        int limit = Math.clamp(maximum, 1, 256);
        List<CommandEventJournal.CommandRecord> result = new ArrayList<>(limit);
        var iterator = durableRecent.descendingIterator();
        while (iterator.hasNext() && result.size() < limit) {
            result.add(iterator.next());
        }
        return List.copyOf(result);
    }

    public synchronized RetentionPreview retentionPreview() {
        if (archiveDirectory == null || !Files.isDirectory(archiveDirectory, LinkOption.NOFOLLOW_LINKS)) {
            return new RetentionPreview(0, 0L, null, null);
        }
        List<Path> archives = ownedArchives();
        long bytes = archives.stream().mapToLong(FileLogSink::size).sum();
        Instant oldest = archives.stream().map(FileLogSink::modified).min(Comparator.naturalOrder()).orElse(null);
        Instant newest = archives.stream().map(FileLogSink::modified).max(Comparator.naturalOrder()).orElse(null);
        return new RetentionPreview(archives.size(), bytes, oldest, newest);
    }

    public synchronized int runRetention() {
        if (archiveDirectory == null) {
            return 0;
        }
        List<Path> archives = new ArrayList<>(ownedArchives());
        archives.sort(Comparator.comparing(FileLogSink::modified).reversed());
        Instant cutoff = Instant.now().minus(settings.retentionAge());
        long retainedBytes = 0L;
        int deleted = 0;
        for (int index = 0; index < archives.size(); index++) {
            Path candidate = archives.get(index);
            long bytes = size(candidate);
            boolean remove = index >= settings.maximumArchives()
                    || modified(candidate).isBefore(cutoff)
                    || retainedBytes + bytes > settings.maximumTotalBytes();
            if (remove) {
                try {
                    if (isOwnedArchive(candidate) && Files.deleteIfExists(candidate)) {
                        deleted++;
                    }
                } catch (IOException exception) {
                    failures.incrementAndGet();
                    failureDetail = "retention could not delete one owned archive";
                    state = State.DEGRADED;
                }
            } else {
                retainedBytes += bytes;
            }
            if (deleted >= 1000) {
                break;
            }
        }
        return deleted;
    }

    public synchronized int runRetention(RetentionPreview expected) {
        Objects.requireNonNull(expected, "expected");
        if (!retentionPreview().equals(expected)) {
            return -1;
        }
        return runRetention();
    }

    public synchronized Path exportRecent(int maximum) throws IOException {
        if (root == null || !running) {
            throw new IOException("Optional file logging is not active");
        }
        Path exportDirectory = root.resolve("exports");
        verifyChild(exportDirectory);
        Files.createDirectories(exportDirectory);
        if (Files.isSymbolicLink(exportDirectory)) {
            throw new IOException("Logger export directory cannot be a symbolic link");
        }
        Path destination = exportDirectory.resolve(
                "commands-" + ARCHIVE_TIME.format(Instant.now()) + "-" + UUID.randomUUID() + ".jsonl");
        List<CommandEventJournal.CommandRecord> records = recent(Math.clamp(maximum, 1, 4096));
        try (BufferedWriter writer = Files.newBufferedWriter(
                destination,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE)) {
            for (int index = records.size() - 1; index >= 0; index--) {
                writer.write(encode(records.get(index)));
                writer.newLine();
            }
        }
        return destination;
    }

    public synchronized boolean acknowledgeRepair() {
        if (stateDirectory == null) {
            return false;
        }
        Path marker = stateDirectory.resolve("incomplete-session.json");
        try {
            boolean removed = Files.deleteIfExists(marker);
            if (removed && state == State.DEGRADED) {
                state = running ? State.HEALTHY : State.DISABLED;
                failureDetail = "";
            }
            return removed;
        } catch (IOException exception) {
            fail("repair acknowledgement failed", exception);
            return false;
        }
    }

    public CaptureFilter captureFilter() {
        return captureFilter;
    }

    public synchronized void resetCaptureFilter() {
        captureFilter = CaptureFilter.defaults();
    }

    public synchronized boolean setCaptureMode(FilterMode mode) {
        Objects.requireNonNull(mode, "mode");
        if (mode == FilterMode.INCLUDE && !captureFilter.hasIncludes()) {
            return false;
        }
        captureFilter = captureFilter.withMode(mode);
        return true;
    }

    public synchronized boolean addCaptureFilter(
            boolean include,
            FilterKind kind,
            String value
    ) {
        Objects.requireNonNull(kind, "kind");
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.length() > 128) {
            return false;
        }
        try {
            captureFilter = captureFilter.withValue(include, kind, normalized);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public synchronized boolean setCaptureFilterEnabled(
            FilterKind kind,
            String value,
            boolean enabled
    ) {
        Objects.requireNonNull(kind, "kind");
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.length() > 128
                || kind == FilterKind.ROOT || kind == FilterKind.ACTION || kind == FilterKind.PLAYER) {
            return false;
        }
        try {
            captureFilter = captureFilter.withEnabled(kind, normalized, enabled);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public String commandTextFormat() {
        return commandTextFormat;
    }

    public synchronized boolean setCommandTextFormat(String template) {
        if (!validTextFormat(template)) {
            return false;
        }
        commandTextFormat = template;
        return true;
    }

    public synchronized void resetCommandTextFormat() {
        commandTextFormat =
                "[{timestamp}] [{source}] [{dimension} {x} {y} {z}] {actor}: {command} {result}";
    }

    public static boolean validTextFormat(String template) {
        if (template == null || template.isBlank() || template.length() > 512
                || template.codePoints().anyMatch(Character::isISOControl)) {
            return false;
        }
        String remaining = template;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\{([a-z_]+)}").matcher(template);
        while (matcher.find()) {
            if (!Set.of(
                    "timestamp", "source", "dimension", "x", "y", "z", "actor",
                    "command", "root", "action", "origin", "result", "duration").contains(matcher.group(1))) {
                return false;
            }
        }
        return !remaining.contains("${");
    }

    private void writerLoop() {
        BufferedWriter structured = null;
        BufferedWriter text = null;
        BufferedWriter connections = null;
        try {
            structured = openWriter(currentFile);
            if (settings.textMirror()) {
                text = openWriter(textFile);
            }
            List<CommandEventJournal.CommandRecord> batch = new ArrayList<>(settings.batchRecords());
            List<ConnectionRecord> connectionBatch = new ArrayList<>(settings.batchRecords());
            while (running
                    || queue != null && !queue.isEmpty()
                    || connectionQueue != null && !connectionQueue.isEmpty()) {
                if (connectionStreamEnabled && connections == null) {
                    initializeConnectionPaths();
                    connections = openWriter(connectionFile);
                } else if (!connectionStreamEnabled
                        && connections != null
                        && (connectionQueue == null || connectionQueue.isEmpty())) {
                    connections.flush();
                    connections.close();
                    connections = null;
                }
                try {
                    CommandEventJournal.CommandRecord first = queue.poll(
                            settings.flushInterval().toMillis(),
                            TimeUnit.MILLISECONDS);
                    if (first != null) {
                        batch.add(first);
                        queue.drainTo(batch, settings.batchRecords() - 1);
                    }
                    if (connectionQueue != null) {
                        connectionQueue.drainTo(connectionBatch, settings.batchRecords());
                    }
                } catch (InterruptedException exception) {
                    if (!running
                            && (queue == null || queue.isEmpty())
                            && (connectionQueue == null || connectionQueue.isEmpty())) {
                        break;
                    }
                }
                if (!batch.isEmpty()) {
                    for (CommandEventJournal.CommandRecord record : batch) {
                        structured.write(encode(record));
                        structured.newLine();
                        if (text != null) {
                            text.write(text(record));
                            text.newLine();
                        }
                        rememberDurable(record);
                        written.incrementAndGet();
                    }
                    batch.clear();
                }
                if (connections != null && !connectionBatch.isEmpty()) {
                    for (ConnectionRecord record : connectionBatch) {
                        connections.write(encode(record));
                        connections.newLine();
                        rememberDurable(record);
                        written.incrementAndGet();
                    }
                    connectionBatch.clear();
                }
                if (flushRequested || !running) {
                    structured.flush();
                    if (text != null) {
                        text.flush();
                    }
                    if (connections != null) {
                        connections.flush();
                    }
                    flushRequested = false;
                }
                if (rotateRequested || rotationRequired()) {
                    structured.flush();
                    structured.close();
                    if (text != null) {
                        text.flush();
                        text.close();
                    }
                    if (connections != null) {
                        connections.flush();
                        connections.close();
                    }
                    rotate();
                    structured = openWriter(currentFile);
                    text = settings.textMirror() ? openWriter(textFile) : null;
                    connections = connectionStreamEnabled ? openWriter(connectionFile) : null;
                    rotateRequested = false;
                }
            }
            structured.flush();
            if (text != null) {
                text.flush();
            }
            if (connections != null) {
                connections.flush();
            }
        } catch (IOException | RuntimeException exception) {
            fail("logger writer failed", exception);
        } finally {
            closeWriter(structured);
            closeWriter(text);
            closeWriter(connections);
            synchronized (this) {
                if (writerThread == Thread.currentThread() && !running) {
                    writerThread = null;
                }
            }
        }
    }

    private synchronized void initializePaths() throws IOException {
        Path logs = serverDirectory.resolve("logs").normalize();
        root = logs.resolve("sef").normalize();
        verifyChild(root);
        if (Files.exists(logs, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(logs)) {
            throw new IOException("Minecraft logs directory cannot be a symbolic link for SEF file logging");
        }
        if (Files.exists(root, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(root)) {
            throw new IOException("SEF log directory cannot be a symbolic link");
        }
        commandDirectory = root.resolve("commands");
        archiveDirectory = commandDirectory.resolve("archive");
        stateDirectory = root.resolve("state");
        currentFile = commandDirectory.resolve("current.jsonl");
        textFile = root.resolve("text").resolve("current.log");
        for (Path directory : List.of(root, commandDirectory, archiveDirectory, stateDirectory, textFile.getParent())) {
            verifyChild(directory);
            Files.createDirectories(directory);
            if (Files.isSymbolicLink(directory)) {
                throw new IOException("SEF log directory cannot be a symbolic link");
            }
        }
        if (Files.exists(stateDirectory.resolve("incomplete-session.json"), LinkOption.NOFOLLOW_LINKS)) {
            state = State.DEGRADED;
            failureDetail = "an incomplete previous logging session requires acknowledgement";
        }
    }

    private synchronized void initializeConnectionPaths() throws IOException {
        connectionDirectory = root.resolve("connection_events");
        connectionArchiveDirectory = connectionDirectory.resolve("archive");
        connectionFile = connectionDirectory.resolve("current.jsonl");
        for (Path directory : List.of(connectionDirectory, connectionArchiveDirectory)) {
            verifyChild(directory);
            Files.createDirectories(directory);
            if (Files.isSymbolicLink(directory)) {
                throw new IOException("SEF connection log directory cannot be a symbolic link");
            }
        }
    }

    private void rotate() throws IOException {
        if (Files.isRegularFile(currentFile, LinkOption.NOFOLLOW_LINKS) && size(currentFile) > 0L) {
            Path archive = nextArchive("commands", ".jsonl");
            moveForRotation(currentFile, archive);
        }
        if (settings.textMirror()
                && Files.isRegularFile(textFile, LinkOption.NOFOLLOW_LINKS)
                && size(textFile) > 0L) {
            Path textArchiveDirectory = textFile.getParent().resolve("archive");
            Files.createDirectories(textArchiveDirectory);
            if (Files.isSymbolicLink(textArchiveDirectory)) {
                throw new IOException("SEF text archive directory cannot be a symbolic link");
            }
            Path archive = textArchiveDirectory.resolve(
                    "commands-" + ARCHIVE_TIME.format(Instant.now()) + "-" + (++archiveSequence) + ".log");
            moveForRotation(textFile, archive);
        }
        if (connectionFile != null
                && Files.isRegularFile(connectionFile, LinkOption.NOFOLLOW_LINKS)
                && size(connectionFile) > 0L) {
            Path archive = connectionArchiveDirectory.resolve(
                    "connection_events-" + ARCHIVE_TIME.format(Instant.now()) + "-"
                            + String.format(Locale.ROOT, "%04d", ++archiveSequence) + ".jsonl");
            moveForRotation(connectionFile, archive);
        }
        activeSince = Instant.now();
        rotations.incrementAndGet();
        runRetention();
    }

    private Path nextArchive(String stream, String suffix) {
        return archiveDirectory.resolve(
                stream + "-" + ARCHIVE_TIME.format(Instant.now()) + "-"
                        + String.format(Locale.ROOT, "%04d", ++archiveSequence) + suffix);
    }

    private boolean rotationRequired() {
        return size(currentFile) >= settings.maximumFileBytes()
                || connectionFile != null && size(connectionFile) >= settings.maximumFileBytes()
                || Duration.between(activeSince, Instant.now()).compareTo(settings.maximumFileAge()) >= 0;
    }

    private synchronized void rememberDurable(CommandEventJournal.CommandRecord record) {
        durableRecent.addLast(record);
        while (durableRecent.size() > DURABLE_RECENT_LIMIT) {
            durableRecent.removeFirst();
        }
    }

    private synchronized void rememberDurable(ConnectionRecord record) {
        durableConnections.addLast(record);
        while (durableConnections.size() > DURABLE_RECENT_LIMIT) {
            durableConnections.removeFirst();
        }
    }

    private void signalDrop(String reason) {
        long count = dropped.incrementAndGet();
        if (count == 1L || count % 100L == 0L) {
            ServerEssentialsForge.LOGGER.error(
                    "[SEF] Optional file log dropped {} record or records because {}",
                    count,
                    reason);
        }
    }

    private synchronized void fail(String detail, Throwable exception) {
        failures.incrementAndGet();
        state = State.FAILED;
        accepting = false;
        running = false;
        failureDetail = detail;
        ServerEssentialsForge.LOGGER.error("[SEF] {}", detail, exception);
    }

    private void writeSessionMarker() throws IOException {
        JsonObject marker = new JsonObject();
        marker.addProperty("schemaVersion", 1);
        marker.addProperty("sessionId", sessionId.toString());
        marker.addProperty("startedAt", activeSince.toString());
        Files.writeString(
                stateDirectory.resolve("active-session.json"),
                GSON.toJson(marker),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }

    private void writeIncompleteMarker() {
        if (stateDirectory == null) {
            return;
        }
        try {
            JsonObject marker = new JsonObject();
            marker.addProperty("schemaVersion", 1);
            marker.addProperty("sessionId", sessionId.toString());
            marker.addProperty("recordedAt", Instant.now().toString());
            marker.addProperty("accepted", accepted.get());
            marker.addProperty("written", written.get());
            marker.addProperty(
                    "queued",
                    (queue == null ? 0 : queue.size())
                            + (connectionQueue == null ? 0 : connectionQueue.size()));
            Files.writeString(
                    stateDirectory.resolve("incomplete-session.json"),
                    GSON.toJson(marker),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException exception) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to write incomplete logger marker", exception);
        }
    }

    private void deleteSessionMarker() {
        if (stateDirectory == null) {
            return;
        }
        try {
            Files.deleteIfExists(stateDirectory.resolve("active-session.json"));
        } catch (IOException exception) {
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to remove active logger session marker", exception);
        }
    }

    private List<Path> ownedArchives() {
        List<Path> results = new ArrayList<>();
        collectOwnedArchives(
                archiveDirectory,
                "commands-*.jsonl",
                path -> isOwnedArchive(path, archiveDirectory, "commands-"),
                results);
        collectOwnedArchives(
                connectionArchiveDirectory,
                "connection_events-*.jsonl",
                path -> isOwnedArchive(path, connectionArchiveDirectory, "connection_events-"),
                results);
        return List.copyOf(results);
    }

    private void collectOwnedArchives(
            Path directory,
            String glob,
            java.util.function.Predicate<Path> ownership,
            List<Path> results
    ) {
        if (directory == null || Files.isSymbolicLink(directory)) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, glob)) {
            for (Path path : stream) {
                if (ownership.test(path)) {
                    results.add(path);
                }
                if (results.size() >= 10000) {
                    break;
                }
            }
        } catch (IOException exception) {
            failures.incrementAndGet();
            state = State.DEGRADED;
            failureDetail = "archive listing failed";
        }
    }

    private boolean isOwnedArchive(Path path) {
        return isOwnedArchive(path, archiveDirectory, "commands-")
                || isOwnedArchive(path, connectionArchiveDirectory, "connection_events-");
    }

    private boolean isOwnedArchive(Path path, Path directory, String prefix) {
        if (directory == null) {
            return false;
        }
        Path normalized = path.toAbsolutePath().normalize();
        return normalized.getParent().equals(directory.toAbsolutePath().normalize())
                && normalized.getFileName().toString().matches(
                java.util.regex.Pattern.quote(prefix) + "[0-9TZ-]+-[0-9]{4}\\.jsonl")
                && Files.isRegularFile(normalized, LinkOption.NOFOLLOW_LINKS)
                && !Files.isSymbolicLink(normalized);
    }

    private void verifyChild(Path path) throws IOException {
        Path normalized = path.toAbsolutePath().normalize();
        if (!normalized.startsWith(serverDirectory)) {
            throw new IOException("SEF logger path escaped the server directory");
        }
    }

    private static BufferedWriter openWriter(Path path) throws IOException {
        if (Files.isSymbolicLink(path)) {
            throw new IOException("SEF log file cannot be a symbolic link");
        }
        return Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE);
    }

    private void closeWriter(BufferedWriter writer) {
        if (writer == null) {
            return;
        }
        try {
            writer.close();
        } catch (IOException exception) {
            failures.incrementAndGet();
            state = State.DEGRADED;
            failureDetail = "logger writer could not close cleanly";
            ServerEssentialsForge.LOGGER.error("[SEF] Optional file logger close failed", exception);
        }
    }

    private static void moveForRotation(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, destination);
        }
    }

    private static long size(Path path) {
        try {
            return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path)
                    ? Files.size(path)
                    : 0L;
        } catch (IOException exception) {
            return 0L;
        }
    }

    private static Instant modified(Path path) {
        try {
            return Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toInstant();
        } catch (IOException exception) {
            return Instant.EPOCH;
        }
    }

    static String encode(CommandEventJournal.CommandRecord record) {
        JsonObject object = new JsonObject();
        object.addProperty("schemaVersion", record.schemaVersion());
        object.addProperty("eventId", record.eventId().toString());
        if (record.parentEventId() != null) {
            object.addProperty("parentEventId", record.parentEventId().toString());
        }
        object.addProperty("serverSessionId", record.serverSessionId().toString());
        object.addProperty("timestamp", record.timestamp().toString());
        object.addProperty("stage", record.stage().name().toLowerCase(Locale.ROOT));
        if (record.initiatorId() != null) {
            object.addProperty("initiatorId", record.initiatorId().toString());
        }
        if (record.effectiveActorId() != null) {
            object.addProperty("effectiveActorId", record.effectiveActorId().toString());
        }
        object.addProperty("actorName", record.actorName());
        object.addProperty("source", record.sourceType().name().toLowerCase(Locale.ROOT));
        object.addProperty("dimension", record.dimensionId());
        object.addProperty("x", record.x());
        object.addProperty("y", record.y());
        object.addProperty("z", record.z());
        object.addProperty("root", record.root());
        object.addProperty("actionId", record.actionId());
        object.addProperty("command", record.commandDisplay());
        object.addProperty("redactionClass", record.redactionClass().name().toLowerCase(Locale.ROOT));
        JsonArray rules = new JsonArray();
        record.redactionRuleIds().stream().sorted().forEach(rules::add);
        object.add("redactionRules", rules);
        object.addProperty("origin", record.origin());
        object.addProperty("feedbackSuppressed", record.feedbackSuppressed());
        if (record.resultCode() != null) {
            object.addProperty("resultCode", record.resultCode());
        }
        object.addProperty("durationMillis", record.durationMillis());
        object.addProperty("detail", record.detail());
        return GSON.toJson(object);
    }

    static String encode(ConnectionRecord record) {
        JsonObject object = new JsonObject();
        object.addProperty("schemaVersion", record.schemaVersion());
        object.addProperty("eventId", record.eventId().toString());
        object.addProperty("serverSessionId", record.serverSessionId().toString());
        object.addProperty("timestamp", record.timestamp().toString());
        object.addProperty("type", record.type().name().toLowerCase(Locale.ROOT));
        object.addProperty("playerId", record.playerId().toString());
        object.addProperty("playerName", record.playerName());
        object.addProperty("addressFingerprint", record.addressFingerprint());
        object.addProperty("address", record.redactedAddress());
        return GSON.toJson(object);
    }

    private String text(CommandEventJournal.CommandRecord record) {
        return commandTextFormat
                .replace("{timestamp}", record.timestamp().toString())
                .replace("{source}", record.sourceType().name().toLowerCase(Locale.ROOT))
                .replace("{dimension}", record.dimensionId())
                .replace("{x}", Integer.toString(record.x()))
                .replace("{y}", Integer.toString(record.y()))
                .replace("{z}", Integer.toString(record.z()))
                .replace("{actor}", record.actorName())
                .replace("{command}", record.commandDisplay())
                .replace("{root}", record.root())
                .replace("{action}", record.actionId())
                .replace("{origin}", record.origin())
                .replace("{result}", record.stage().name().toLowerCase(Locale.ROOT))
                .replace("{duration}", Long.toString(record.durationMillis()));
    }

    public record Settings(
            boolean enabled,
            boolean connectionEvents,
            boolean textMirror,
            int queueCapacity,
            int batchRecords,
            Duration flushInterval,
            int maximumRecordBytes,
            long maximumFileBytes,
            Duration maximumFileAge,
            Duration retentionAge,
            int maximumArchives,
            long maximumTotalBytes,
            Duration shutdownTimeout
    ) {
        public Settings {
            Objects.requireNonNull(flushInterval, "flushInterval");
            Objects.requireNonNull(maximumFileAge, "maximumFileAge");
            Objects.requireNonNull(retentionAge, "retentionAge");
            Objects.requireNonNull(shutdownTimeout, "shutdownTimeout");
            if (queueCapacity < 128 || queueCapacity > 65536
                    || batchRecords < 1 || batchRecords > 1024
                    || maximumRecordBytes < 1024 || maximumRecordBytes > 1048576
                    || maximumFileBytes < 1048576L || maximumFileBytes > 1073741824L
                    || maximumArchives < 1 || maximumArchives > 10000
                    || maximumTotalBytes < 1048576L) {
                throw new IllegalArgumentException("File logging settings are outside hard bounds");
            }
        }

        public static Settings fromConfig() {
            return new Settings(
                    ConfigHandler.config.fileLoggingEnabled.get(),
                    ConfigHandler.config.fileLoggingConnectionEvents.get(),
                    ConfigHandler.config.fileLoggingTextMirror.get(),
                    ConfigHandler.config.fileLoggingQueueCapacity.get(),
                    ConfigHandler.config.fileLoggingBatchRecords.get(),
                    Duration.ofMillis(ConfigHandler.config.fileLoggingFlushIntervalMillis.get()),
                    ConfigHandler.config.fileLoggingMaximumRecordBytes.get(),
                    ConfigHandler.config.fileLoggingMaximumFileMiB.get() * 1024L * 1024L,
                    Duration.ofHours(ConfigHandler.config.fileLoggingMaximumFileAgeHours.get()),
                    Duration.ofDays(ConfigHandler.config.fileLoggingRetentionDays.get()),
                    ConfigHandler.config.fileLoggingMaximumArchives.get(),
                    ConfigHandler.config.fileLoggingMaximumTotalMiB.get() * 1024L * 1024L,
                    Duration.ofSeconds(ConfigHandler.config.fileLoggingShutdownTimeoutSeconds.get()));
        }
    }

    public record Health(
            State state,
            boolean accepting,
            int queueDepth,
            int queueCapacity,
            long accepted,
            long written,
            long dropped,
            long failures,
            long rotations,
            UUID sessionId,
            Instant activeSince,
            String detail
    ) {
    }

    public record RetentionPreview(int archives, long bytes, Instant oldest, Instant newest) {
    }

    public record ConnectionRecord(
            int schemaVersion,
            UUID eventId,
            UUID serverSessionId,
            Instant timestamp,
            ConnectionType type,
            UUID playerId,
            String playerName,
            String addressFingerprint,
            String redactedAddress
    ) {
        public ConnectionRecord {
            if (schemaVersion != 1) {
                throw new IllegalArgumentException("Unsupported connection log schema");
            }
            Objects.requireNonNull(eventId, "eventId");
            Objects.requireNonNull(serverSessionId, "serverSessionId");
            Objects.requireNonNull(timestamp, "timestamp");
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(playerId, "playerId");
            playerName = boundedLogField(playerName, 64);
            addressFingerprint = boundedLogField(addressFingerprint, 128);
            redactedAddress = boundedLogField(redactedAddress, 128);
        }
    }

    public enum ConnectionType {
        JOIN,
        LEAVE
    }

    public enum State {
        DISABLED,
        HEALTHY,
        DEGRADED,
        FAILED
    }

    private static String boundedLogField(String value, int maximumLength) {
        String sanitized = value == null
                ? ""
                : value.replace("\r", "\\r").replace("\n", "\\n").strip();
        return sanitized.length() <= maximumLength ? sanitized : sanitized.substring(0, maximumLength);
    }

    public record CaptureFilter(
            FilterMode mode,
            Set<String> includedRoots,
            Set<String> excludedRoots,
            Set<String> includedActions,
            Set<String> excludedActions,
            Map<FilterKind, Set<String>> includedTyped,
            Map<FilterKind, Set<String>> excludedTyped
    ) {
        public CaptureFilter {
            Objects.requireNonNull(mode, "mode");
            includedRoots = Set.copyOf(includedRoots);
            excludedRoots = Set.copyOf(excludedRoots);
            includedActions = Set.copyOf(includedActions);
            excludedActions = Set.copyOf(excludedActions);
            includedTyped = copyTyped(includedTyped);
            excludedTyped = copyTyped(excludedTyped);
            if (includedRoots.size() + excludedRoots.size()
                    + includedActions.size() + excludedActions.size()
                    + typedSize(includedTyped) + typedSize(excludedTyped) > 128) {
                throw new IllegalArgumentException("Capture filter count exceeds hard bound");
            }
        }

        public static CaptureFilter defaults() {
            return new CaptureFilter(
                    FilterMode.ALL,
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    Map.of(),
                    Map.of());
        }

        public boolean matches(CommandEventJournal.CommandRecord record) {
            if (record.actionId().startsWith("sef:logging.")
                    || Set.of("run", "silent", "sudo").contains(record.root())) {
                return true;
            }
            if (excludedRoots.contains(record.root())
                    || excludedActions.contains(record.actionId())
                    || typedMatches(excludedTyped, record)) {
                return false;
            }
            if (mode == FilterMode.ALL) {
                return true;
            }
            return includedRoots.contains(record.root())
                    || includedActions.contains(record.actionId())
                    || typedMatches(includedTyped, record);
        }

        public boolean hasIncludes() {
            return !includedRoots.isEmpty() || !includedActions.isEmpty() || typedSize(includedTyped) > 0;
        }

        public Set<String> included(FilterKind kind) {
            return includedTyped.getOrDefault(kind, Set.of());
        }

        public Set<String> excluded(FilterKind kind) {
            return excludedTyped.getOrDefault(kind, Set.of());
        }

        CaptureFilter withMode(FilterMode replacement) {
            return new CaptureFilter(
                    replacement,
                    includedRoots,
                    excludedRoots,
                    includedActions,
                    excludedActions,
                    includedTyped,
                    excludedTyped);
        }

        CaptureFilter withValue(boolean include, FilterKind kind, String value) {
            java.util.Set<String> includeRoots = new java.util.HashSet<>(includedRoots);
            java.util.Set<String> excludeRoots = new java.util.HashSet<>(excludedRoots);
            java.util.Set<String> includeActions = new java.util.HashSet<>(includedActions);
            java.util.Set<String> excludeActions = new java.util.HashSet<>(excludedActions);
            Map<FilterKind, Set<String>> includeTyped = mutableTyped(includedTyped);
            Map<FilterKind, Set<String>> excludeTyped = mutableTyped(excludedTyped);
            Set<String> destination = switch (kind) {
                case ROOT -> include ? includeRoots : excludeRoots;
                case ACTION -> include ? includeActions : excludeActions;
                default -> typed(include ? includeTyped : excludeTyped, kind);
            };
            destination.add(value);
            return new CaptureFilter(
                    mode,
                    includeRoots,
                    excludeRoots,
                    includeActions,
                    excludeActions,
                    includeTyped,
                    excludeTyped);
        }

        CaptureFilter withEnabled(FilterKind kind, String value, boolean enabled) {
            Map<FilterKind, Set<String>> includeTyped = mutableTyped(includedTyped);
            Map<FilterKind, Set<String>> excludeTyped = mutableTyped(excludedTyped);
            typed(includeTyped, kind).remove(value);
            Set<String> exclusions = typed(excludeTyped, kind);
            if (enabled) {
                exclusions.remove(value);
            } else {
                exclusions.add(value);
            }
            return new CaptureFilter(
                    mode,
                    includedRoots,
                    excludedRoots,
                    includedActions,
                    excludedActions,
                    includeTyped,
                    excludeTyped);
        }

        private static boolean typedMatches(
                Map<FilterKind, Set<String>> filters,
                CommandEventJournal.CommandRecord record
        ) {
            if (filters.getOrDefault(FilterKind.SOURCE, Set.of())
                    .contains(record.sourceType().name().toLowerCase(Locale.ROOT))
                    || filters.getOrDefault(FilterKind.RESULT, Set.of())
                    .contains(record.stage().name().toLowerCase(Locale.ROOT))
                    || filters.getOrDefault(FilterKind.WORLD, Set.of())
                    .contains(record.dimensionId().toLowerCase(Locale.ROOT))
                    || filters.getOrDefault(FilterKind.ORIGIN, Set.of()).contains(record.origin())) {
                return true;
            }
            Set<String> players = filters.getOrDefault(FilterKind.PLAYER, Set.of());
            return record.initiatorId() != null && players.contains(record.initiatorId().toString())
                    || record.effectiveActorId() != null && players.contains(record.effectiveActorId().toString());
        }

        private static Map<FilterKind, Set<String>> copyTyped(Map<FilterKind, Set<String>> values) {
            if (values == null || values.isEmpty()) {
                return Map.of();
            }
            Map<FilterKind, Set<String>> copy = new java.util.EnumMap<>(FilterKind.class);
            values.forEach((kind, entries) -> {
                if (kind != null && kind != FilterKind.ROOT && kind != FilterKind.ACTION
                        && entries != null && !entries.isEmpty()) {
                    copy.put(kind, Set.copyOf(entries));
                }
            });
            return Map.copyOf(copy);
        }

        private static Map<FilterKind, Set<String>> mutableTyped(Map<FilterKind, Set<String>> values) {
            Map<FilterKind, Set<String>> copy = new java.util.EnumMap<>(FilterKind.class);
            values.forEach((kind, entries) -> copy.put(kind, new java.util.HashSet<>(entries)));
            return copy;
        }

        private static Set<String> typed(Map<FilterKind, Set<String>> values, FilterKind kind) {
            return values.computeIfAbsent(kind, ignored -> new java.util.HashSet<>());
        }

        private static int typedSize(Map<FilterKind, Set<String>> values) {
            return values.values().stream().mapToInt(Set::size).sum();
        }
    }

    public enum FilterMode {
        ALL,
        INCLUDE
    }

    public enum FilterKind {
        ROOT,
        ACTION,
        SOURCE,
        PLAYER,
        RESULT,
        WORLD,
        ORIGIN
    }
}
