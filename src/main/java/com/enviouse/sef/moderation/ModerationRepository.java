package com.enviouse.sef.moderation;

import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.InstantJsonAdapter;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.enviouse.sef.teleport.SavedLocation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ModerationRepository implements StorageRepository {
    private static final int SCHEMA_VERSION = 1;
    private static final int MAXIMUM_JAILS = 256;
    private static final int MAXIMUM_SENTENCES = 10000;
    private static final int MAXIMUM_CONTROLS = 40000;
    private static final int MAXIMUM_WARNINGS = 100000;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .disableHtmlEscaping()
            .create();

    private final Map<String, Jail> jails = new LinkedHashMap<>();
    private final Map<UUID, Sentence> sentences = new LinkedHashMap<>();
    private final Map<ControlKey, Control> controls = new LinkedHashMap<>();
    private final Map<UUID, List<Warning>> warnings = new LinkedHashMap<>();
    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private long revision;
    private long flushedRevision;

    @Override
    public String id() {
        return "sef:moderation";
    }

    @Override
    public String domain() {
        return "moderation";
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public synchronized Path path() {
        return path;
    }

    @Override
    public synchronized LoadResult load(Path managedRoot) {
        path = managedRoot.resolve("moderation.json").toAbsolutePath().normalize();
        jails.clear();
        sentences.clear();
        controls.clear();
        warnings.clear();
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = Files.exists(path) ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, state == RepositoryState.MISSING ? "new repository" : "storage unavailable");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null || snapshot.jails().size() > MAXIMUM_JAILS
                    || snapshot.sentences().size() > MAXIMUM_SENTENCES
                    || snapshot.controls().size() > MAXIMUM_CONTROLS
                    || snapshot.warnings().size() > MAXIMUM_WARNINGS) {
                throw new IllegalStateException("moderation collection is outside bounds");
            }
            for (Jail jail : snapshot.jails()) {
                validate(jail);
                if (jails.putIfAbsent(jail.normalizedName(), jail) != null) {
                    throw new IllegalStateException("duplicate jail");
                }
            }
            for (Sentence sentence : snapshot.sentences()) {
                validate(sentence);
                if (sentences.putIfAbsent(sentence.playerId(), sentence) != null) {
                    throw new IllegalStateException("duplicate jail sentence");
                }
            }
            for (Control control : snapshot.controls()) {
                validate(control);
                ControlKey key = new ControlKey(control.playerId(), control.type());
                if (controls.putIfAbsent(key, control) != null) {
                    throw new IllegalStateException("duplicate moderation control");
                }
            }
            for (Warning warning : snapshot.warnings()) {
                validate(warning);
                warnings.computeIfAbsent(warning.playerId(), ignored -> new java.util.ArrayList<>()).add(warning);
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            return new LoadResult(state, "loaded " + jails.size() + " jails and "
                    + sentences.size() + " sentences");
        } catch (RuntimeException exception) {
            jails.clear();
            sentences.clear();
            controls.clear();
            warnings.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized Jail setJail(String name, SavedLocation location, UUID actorId) {
        writable();
        String normalized = normalizeName(name);
        Jail jail = new Jail(normalized, name.strip(), location, actorId, Instant.now(), 1);
        validate(jail);
        if (!jails.containsKey(normalized) && jails.size() >= MAXIMUM_JAILS) {
            throw new IllegalStateException("Jail limit reached");
        }
        jails.put(normalized, jail);
        revision++;
        return jail;
    }

    public synchronized boolean deleteJail(String name) {
        writable();
        String normalized = normalizeName(name);
        if (sentences.values().stream().anyMatch(sentence -> sentence.jailName().equals(normalized))) {
            throw new IllegalStateException("Jail still has active occupants");
        }
        boolean removed = jails.remove(normalized) != null;
        if (removed) {
            revision++;
        }
        return removed;
    }

    public synchronized Optional<Jail> jail(String name) {
        return Optional.ofNullable(jails.get(normalizeName(name)));
    }

    public synchronized List<Jail> jails() {
        return jails.values().stream().sorted(Comparator.comparing(Jail::normalizedName)).toList();
    }

    public synchronized Sentence sentence(
            UUID playerId,
            String jailName,
            Instant expiresAt,
            String reason,
            UUID actorId,
            SavedLocation releaseLocation
    ) {
        writable();
        String normalized = normalizeName(jailName);
        if (!jails.containsKey(normalized)) {
            throw new IllegalArgumentException("Jail does not exist");
        }
        if (!sentences.containsKey(playerId) && sentences.size() >= MAXIMUM_SENTENCES) {
            throw new IllegalStateException("Jail sentence limit reached");
        }
        Sentence sentence = new Sentence(
                playerId,
                normalized,
                Instant.now(),
                expiresAt,
                bounded(reason, 512),
                actorId,
                releaseLocation,
                1);
        validate(sentence);
        sentences.put(playerId, sentence);
        revision++;
        return sentence;
    }

    public synchronized Optional<Sentence> sentence(UUID playerId) {
        Sentence sentence = sentences.get(playerId);
        if (sentence == null) {
            return Optional.empty();
        }
        if (sentence.expired(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(sentence);
    }

    public synchronized Optional<Sentence> release(UUID playerId) {
        writable();
        Sentence removed = sentences.remove(playerId);
        if (removed != null) {
            revision++;
        }
        return Optional.ofNullable(removed);
    }

    public synchronized List<Sentence> sentences() {
        Instant now = Instant.now();
        return sentences.values().stream()
                .filter(sentence -> !sentence.expired(now))
                .sorted(Comparator.comparing(Sentence::createdAt))
                .toList();
    }

    public synchronized List<Sentence> takeExpiredSentences(Instant now) {
        writable();
        List<Sentence> expired = sentences.values().stream()
                .filter(sentence -> sentence.expired(now))
                .toList();
        if (!expired.isEmpty()) {
            expired.forEach(sentence -> sentences.remove(sentence.playerId()));
            revision++;
        }
        return expired;
    }

    public synchronized int purgeExpired(Instant now) {
        int before = controls.size();
        controls.values().removeIf(control -> control.expired(now));
        int removed = before - controls.size();
        if (removed > 0) {
            revision++;
        }
        return removed;
    }

    public synchronized Control applyControl(
            UUID playerId,
            ControlType type,
            Instant expiresAt,
            String reason,
            UUID actorId
    ) {
        writable();
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(actorId, "actorId");
        purgeExpired(Instant.now());
        ControlKey key = new ControlKey(playerId, type);
        if (!controls.containsKey(key) && controls.size() >= MAXIMUM_CONTROLS) {
            throw new IllegalStateException("Moderation control limit reached");
        }
        Control control = new Control(
                playerId,
                type,
                Instant.now(),
                expiresAt,
                bounded(reason, 512),
                actorId,
                controls.containsKey(key) ? controls.get(key).revision() + 1 : 1);
        validate(control);
        controls.put(key, control);
        revision++;
        return control;
    }

    public synchronized Optional<Control> control(UUID playerId, ControlType type) {
        ControlKey key = new ControlKey(playerId, type);
        Control control = controls.get(key);
        if (control != null && control.expired(Instant.now())) {
            controls.remove(key);
            revision++;
            control = null;
        }
        return Optional.ofNullable(control);
    }

    public synchronized Optional<Control> removeControl(UUID playerId, ControlType type) {
        writable();
        Control removed = controls.remove(new ControlKey(playerId, type));
        if (removed != null) {
            revision++;
        }
        return Optional.ofNullable(removed);
    }

    public synchronized List<Control> controls(ControlType type) {
        purgeExpired(Instant.now());
        return controls.values().stream()
                .filter(control -> type == null || control.type() == type)
                .sorted(Comparator.comparing(Control::createdAt))
                .toList();
    }

    public synchronized Warning warn(UUID playerId, String reason, UUID actorId) {
        writable();
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(actorId, "actorId");
        int count = warnings.values().stream().mapToInt(List::size).sum();
        if (count >= MAXIMUM_WARNINGS) {
            throw new IllegalStateException("Warning limit reached");
        }
        Warning warning = new Warning(
                UUID.randomUUID(),
                playerId,
                Instant.now(),
                bounded(reason, 512),
                actorId,
                1);
        validate(warning);
        warnings.computeIfAbsent(playerId, ignored -> new java.util.ArrayList<>()).add(warning);
        revision++;
        return warning;
    }

    public synchronized List<Warning> warnings(UUID playerId) {
        return List.copyOf(warnings.getOrDefault(playerId, List.of()));
    }

    public synchronized int clearWarnings(UUID playerId) {
        writable();
        List<Warning> removed = warnings.remove(playerId);
        if (removed == null) {
            return 0;
        }
        revision++;
        return removed.size();
    }

    @Override
    public void flush() throws IOException {
        final Snapshot snapshot;
        final Path destination;
        final StorageService.Document previous;
        final long snapshotRevision;
        synchronized (this) {
            if (path == null || !dirty()) {
                return;
            }
            writable();
            snapshot = new Snapshot(
                    jails(),
                    sentences(),
                    controls(null),
                    warnings.values().stream().flatMap(List::stream).toList());
            destination = path;
            previous = document;
            snapshotRevision = revision;
        }
        StorageService.write(
                destination,
                domain(),
                SCHEMA_VERSION,
                GSON.toJsonTree(snapshot),
                previous,
                Set.of("/jails", "/sentences", "/controls", "/warnings"));
        synchronized (this) {
            document = StorageService.read(destination, domain(), SCHEMA_VERSION).orElse(previous);
            flushedRevision = Math.max(flushedRevision, snapshotRevision);
            state = RepositoryState.READY;
        }
    }

    @Override
    public synchronized boolean dirty() {
        return revision > flushedRevision;
    }

    @Override
    public synchronized RepositoryState state() {
        return state;
    }

    private static String normalizeName(String name) {
        String normalized = Objects.requireNonNull(name, "name").strip().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9_]{1,32}")) {
            throw new IllegalArgumentException("Jail name must use one to thirty two letters, digits, or underscores");
        }
        return normalized;
    }

    private static String bounded(String value, int maximum) {
        String normalized = value == null ? "" : value.strip();
        if (normalized.codePoints().anyMatch(Character::isISOControl) || normalized.length() > maximum) {
            throw new IllegalArgumentException("Text is outside bounds");
        }
        return normalized;
    }

    private static void validate(Jail jail) {
        Objects.requireNonNull(jail, "jail");
        normalizeName(jail.normalizedName());
        if (!normalizeName(jail.displayName()).equals(jail.normalizedName())
                || jail.revision() < 1) {
            throw new IllegalArgumentException("Jail record is invalid");
        }
    }

    private static void validate(Sentence sentence) {
        Objects.requireNonNull(sentence, "sentence");
        normalizeName(sentence.jailName());
        bounded(sentence.reason(), 512);
        if (sentence.expiresAt() != null && !sentence.expiresAt().isAfter(sentence.createdAt())
                || sentence.revision() < 1) {
            throw new IllegalArgumentException("Jail sentence is invalid");
        }
    }

    private static void validate(Control control) {
        Objects.requireNonNull(control, "control");
        Objects.requireNonNull(control.playerId(), "playerId");
        Objects.requireNonNull(control.type(), "type");
        Objects.requireNonNull(control.actorId(), "actorId");
        bounded(control.reason(), 512);
        if (control.expiresAt() != null && !control.expiresAt().isAfter(control.createdAt())
                || control.revision() < 1) {
            throw new IllegalArgumentException("Moderation control is invalid");
        }
    }

    private static void validate(Warning warning) {
        Objects.requireNonNull(warning, "warning");
        Objects.requireNonNull(warning.id(), "id");
        Objects.requireNonNull(warning.playerId(), "playerId");
        Objects.requireNonNull(warning.actorId(), "actorId");
        bounded(warning.reason(), 512);
        if (warning.revision() < 1) {
            throw new IllegalArgumentException("Warning is invalid");
        }
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("Moderation repository is not writable in " + state + " state");
        }
    }

    public record Snapshot(
            List<Jail> jails,
            List<Sentence> sentences,
            List<Control> controls,
            List<Warning> warnings
    ) {
        public Snapshot {
            jails = List.copyOf(jails == null ? List.of() : jails);
            sentences = List.copyOf(sentences == null ? List.of() : sentences);
            controls = List.copyOf(controls == null ? List.of() : controls);
            warnings = List.copyOf(warnings == null ? List.of() : warnings);
        }
    }

    public record Jail(
            String normalizedName,
            String displayName,
            SavedLocation location,
            UUID createdBy,
            Instant createdAt,
            long revision
    ) {
    }

    public record Sentence(
            UUID playerId,
            String jailName,
            Instant createdAt,
            Instant expiresAt,
            String reason,
            UUID actorId,
            SavedLocation releaseLocation,
            long revision
    ) {
        public boolean expired(Instant now) {
            return expiresAt != null && !expiresAt.isAfter(now);
        }
    }

    public record Control(
            UUID playerId,
            ControlType type,
            Instant createdAt,
            Instant expiresAt,
            String reason,
            UUID actorId,
            long revision
    ) {
        public boolean expired(Instant now) {
            return expiresAt != null && !expiresAt.isAfter(now);
        }
    }

    public record Warning(
            UUID id,
            UUID playerId,
            Instant createdAt,
            String reason,
            UUID actorId,
            long revision
    ) {
    }

    public enum ControlType {
        MUTE,
        FREEZE,
        INVENTORY_LOCK,
        BUILD_LOCK
    }

    private record ControlKey(UUID playerId, ControlType type) {
    }
}
