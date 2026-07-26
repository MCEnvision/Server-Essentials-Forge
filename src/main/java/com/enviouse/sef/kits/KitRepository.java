package com.enviouse.sef.kits;

import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.InstantJsonAdapter;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
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

public final class KitRepository implements StorageRepository {
    private static final int SCHEMA_VERSION = 1;
    private static final int MAXIMUM_USES = 100000;
    private static final int MAXIMUM_ENCODED_ITEM_LENGTH = 65536;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantJsonAdapter())
            .disableHtmlEscaping()
            .create();

    private final int maximumKits;
    private final int maximumItems;
    private final int maximumUsesPerPlayer;
    private final Map<String, Kit> kits = new LinkedHashMap<>();
    private final Map<UseKey, KitUse> uses = new LinkedHashMap<>();
    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private long revision;
    private long flushedRevision;

    public KitRepository(int maximumKits, int maximumItems) {
        this(maximumKits, maximumItems, 256);
    }

    public KitRepository(int maximumKits, int maximumItems, int maximumUsesPerPlayer) {
        if (maximumKits < 1 || maximumKits > 1024
                || maximumItems < 1 || maximumItems > 1024
                || maximumUsesPerPlayer < 1 || maximumUsesPerPlayer > 1024) {
            throw new IllegalArgumentException("Kit repository limits are outside hard bounds");
        }
        this.maximumKits = maximumKits;
        this.maximumItems = maximumItems;
        this.maximumUsesPerPlayer = maximumUsesPerPlayer;
    }

    @Override
    public String id() {
        return "sef:kits";
    }

    @Override
    public String domain() {
        return "kits";
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
        path = managedRoot.resolve("kits.json").toAbsolutePath().normalize();
        kits.clear();
        uses.clear();
        document = StorageService.read(path, domain(), SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = Files.exists(path) ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, state == RepositoryState.MISSING ? "new repository" : "storage unavailable");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null || snapshot.kits().size() > maximumKits
                    || snapshot.uses().size() > MAXIMUM_USES) {
                throw new IllegalStateException("Kit repository collections are outside bounds");
            }
            for (Kit kit : snapshot.kits()) {
                validate(kit);
                if (kits.putIfAbsent(kit.id(), kit) != null) {
                    throw new IllegalStateException("Duplicate kit id");
                }
            }
            for (KitUse use : snapshot.uses()) {
                validate(use);
                if (uses.putIfAbsent(new UseKey(use.playerId(), use.kitId()), use) != null) {
                    throw new IllegalStateException("Duplicate kit use");
                }
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            return new LoadResult(state, "loaded " + kits.size() + " kits and " + uses.size() + " uses");
        } catch (RuntimeException exception) {
            kits.clear();
            uses.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized Kit put(
            String id,
            List<String> encodedItems,
            Duration cooldown,
            boolean oneTime,
            UUID actorId
    ) {
        writable();
        String normalized = normalizeId(id);
        Kit current = kits.get(normalized);
        if (current == null && kits.size() >= maximumKits) {
            throw new IllegalStateException("Kit limit reached");
        }
        Kit kit = new Kit(
                normalized,
                id.strip(),
                "",
                List.copyOf(encodedItems),
                cooldown == null ? 0 : cooldown.toSeconds(),
                oneTime,
                "sef.kit." + normalized,
                actorId,
                Instant.now(),
                current == null ? 1 : current.revision() + 1);
        validate(kit);
        kits.put(normalized, kit);
        revision++;
        return kit;
    }

    public synchronized Optional<Kit> kit(String id) {
        return Optional.ofNullable(kits.get(normalizeId(id)));
    }

    public synchronized List<Kit> kits() {
        return kits.values().stream().sorted(Comparator.comparing(Kit::id)).toList();
    }

    public synchronized boolean delete(String id) {
        writable();
        String normalized = normalizeId(id);
        boolean removed = kits.remove(normalized) != null;
        int before = uses.size();
        uses.entrySet().removeIf(entry -> entry.getKey().kitId().equals(normalized));
        if (removed || before != uses.size()) {
            revision++;
        }
        return removed;
    }

    public synchronized Kit updatePolicy(
            String id,
            Long cooldownSeconds,
            Boolean oneTime,
            String permission,
            String displayName
    ) {
        writable();
        String normalized = normalizeId(id);
        Kit current = kits.get(normalized);
        if (current == null) {
            throw new IllegalArgumentException("Kit not found");
        }
        Kit replacement = new Kit(
                current.id(),
                displayName == null ? current.displayName() : boundedText(displayName, 64),
                current.description(),
                current.items(),
                cooldownSeconds == null ? current.cooldownSeconds() : cooldownSeconds,
                oneTime == null ? current.oneTime() : oneTime,
                permission == null ? current.permission() : normalizePermission(permission, normalized),
                current.createdBy(),
                current.createdAt(),
                current.revision() + 1);
        validate(replacement);
        kits.put(normalized, replacement);
        revision++;
        return replacement;
    }

    public synchronized Availability availability(UUID playerId, Kit kit, Instant now) {
        KitUse use = uses.get(new UseKey(playerId, kit.id()));
        if (use == null) {
            return new Availability(true, null, false);
        }
        if (kit.oneTime()) {
            return new Availability(false, null, true);
        }
        Instant next = use.claimedAt().plusSeconds(kit.cooldownSeconds());
        return new Availability(!next.isAfter(now), next, false);
    }

    public synchronized void recordUse(UUID playerId, Kit kit, Instant now) {
        writable();
        UseKey key = new UseKey(playerId, kit.id());
        long playerUses = uses.keySet().stream()
                .filter(existing -> existing.playerId().equals(playerId))
                .count();
        if (!uses.containsKey(key) && playerUses >= maximumUsesPerPlayer) {
            throw new IllegalStateException("Player kit use record limit reached");
        }
        if (!uses.containsKey(key) && uses.size() >= MAXIMUM_USES) {
            throw new IllegalStateException("Kit use record limit reached");
        }
        KitUse previous = uses.get(key);
        uses.put(key, new KitUse(
                playerId,
                kit.id(),
                now,
                previous == null ? 1 : Math.addExact(previous.claimCount(), 1),
                previous == null ? 1 : previous.revision() + 1));
        revision++;
    }

    public synchronized boolean reset(UUID playerId, String kitId) {
        writable();
        boolean removed = uses.remove(new UseKey(playerId, normalizeId(kitId))) != null;
        if (removed) {
            revision++;
        }
        return removed;
    }

    public synchronized Validation validateAll() {
        int invalid = 0;
        for (Kit kit : kits.values()) {
            try {
                validate(kit);
            } catch (RuntimeException exception) {
                invalid++;
            }
        }
        return new Validation(kits.size(), invalid, uses.size());
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
            snapshot = new Snapshot(kits(), List.copyOf(uses.values()));
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
                Set.of("/kits", "/uses"));
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

    private void validate(Kit kit) {
        Objects.requireNonNull(kit, "kit");
        if (!normalizeId(kit.id()).equals(kit.id())
                || kit.items().size() > maximumItems
                || kit.cooldownSeconds() < 0
                || kit.cooldownSeconds() > 315_576_000L
                || !normalizePermission(kit.permission(), kit.id()).equals(kit.permission())
                || kit.revision() < 1
                || kit.items().stream().anyMatch(item -> item == null
                || item.isBlank()
                || item.length() > MAXIMUM_ENCODED_ITEM_LENGTH)) {
            throw new IllegalArgumentException("Kit definition is invalid");
        }
    }

    private static void validate(KitUse use) {
        Objects.requireNonNull(use, "use");
        normalizeId(use.kitId());
        if (use.claimCount() < 1 || use.claimCount() > 1_000_000 || use.revision() < 1) {
            throw new IllegalArgumentException("Kit use record is invalid");
        }
    }

    private static String normalizeId(String value) {
        String normalized = Objects.requireNonNull(value, "value").strip().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9_]{1,32}")) {
            throw new IllegalArgumentException("Kit id must use one to thirty two letters, digits, or underscores");
        }
        return normalized;
    }

    private static String normalizePermission(String value, String kitId) {
        String normalized = value == null || value.isBlank()
                ? "sef.kit." + normalizeId(kitId)
                : value.strip().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9_.-]{1,128}")) {
            throw new IllegalArgumentException("Kit permission is invalid");
        }
        return normalized;
    }

    private static String boundedText(String value, int maximum) {
        String normalized = Objects.requireNonNull(value, "value").strip();
        if (normalized.isBlank()
                || normalized.length() > maximum
                || normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Kit text is invalid");
        }
        return normalized;
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("Kit repository is not writable in " + state + " state");
        }
    }

    public record Snapshot(List<Kit> kits, List<KitUse> uses) {
        public Snapshot {
            kits = List.copyOf(kits == null ? List.of() : kits);
            uses = List.copyOf(uses == null ? List.of() : uses);
        }
    }

    public record Kit(
            String id,
            String displayName,
            String description,
            List<String> items,
            long cooldownSeconds,
            boolean oneTime,
            String permission,
            UUID createdBy,
            Instant createdAt,
            long revision
    ) {
        public Kit {
            items = List.copyOf(items == null ? List.of() : items);
        }
    }

    public record KitUse(UUID playerId, String kitId, Instant claimedAt, int claimCount, long revision) {
    }

    public record Availability(boolean available, Instant nextUseAt, boolean alreadyClaimed) {
    }

    public record Validation(int kits, int invalidKits, int useRecords) {
    }

    private record UseKey(UUID playerId, String kitId) {
    }
}
