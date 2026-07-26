package com.enviouse.sef.commandlog;

import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class CommandSpyRepository implements StorageRepository {
    private static final int SCHEMA_VERSION = 1;
    private static final String DOMAIN = "command spy";
    private static final int MAXIMUM_OBSERVERS = 10000;
    private static final int MAXIMUM_FILTERS = 64;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final int maximumSelected;
    private final Map<UUID, Profile> profiles = new LinkedHashMap<>();
    private Path path;
    private StorageService.Document document;
    private RepositoryState state = RepositoryState.NEW;
    private long revision;
    private long flushedRevision;

    public CommandSpyRepository(int maximumSelected) {
        if (maximumSelected < 1 || maximumSelected > 256) {
            throw new IllegalArgumentException("Command spy selected limit is outside hard bounds");
        }
        this.maximumSelected = maximumSelected;
    }

    @Override
    public String id() {
        return "sef:command_spy";
    }

    @Override
    public String domain() {
        return DOMAIN;
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
        path = managedRoot.resolve("command-spy.json").toAbsolutePath().normalize();
        profiles.clear();
        document = StorageService.read(path, DOMAIN, SCHEMA_VERSION).orElse(null);
        if (document == null) {
            state = Files.exists(path) ? RepositoryState.RECOVERY : RepositoryState.MISSING;
            return new LoadResult(state, state == RepositoryState.MISSING ? "new repository" : "storage unavailable");
        }
        try {
            Snapshot snapshot = GSON.fromJson(document.data(), Snapshot.class);
            if (snapshot == null || snapshot.profiles() == null || snapshot.profiles().size() > MAXIMUM_OBSERVERS) {
                throw new IllegalStateException("command spy profile collection is invalid");
            }
            for (Profile profile : snapshot.profiles()) {
                validate(profile);
                if (profiles.putIfAbsent(profile.observerId(), profile) != null) {
                    throw new IllegalStateException("duplicate command spy observer");
                }
            }
            state = RepositoryState.READY;
            if (document.migrated()) {
                revision++;
            }
            return new LoadResult(state, "loaded " + profiles.size() + " command spy profiles");
        } catch (RuntimeException exception) {
            profiles.clear();
            state = RepositoryState.RECOVERY;
            return new LoadResult(state, exception.getClass().getSimpleName());
        }
    }

    public synchronized Profile profile(UUID observerId) {
        Objects.requireNonNull(observerId, "observerId");
        return profiles.getOrDefault(observerId, Profile.defaults(observerId));
    }

    public synchronized void put(Profile profile) {
        writable();
        validate(profile);
        if (!profiles.containsKey(profile.observerId()) && profiles.size() >= MAXIMUM_OBSERVERS) {
            throw new IllegalStateException("Command spy observer limit reached");
        }
        profiles.put(profile.observerId(), profile);
        revision++;
    }

    public synchronized void remove(UUID observerId) {
        writable();
        if (profiles.remove(observerId) != null) {
            revision++;
        }
    }

    public synchronized List<Profile> profiles() {
        return profiles.values().stream()
                .sorted(Comparator.comparing(profile -> profile.observerId().toString()))
                .toList();
    }

    @Override
    public void flush() throws IOException {
        final Snapshot snapshot;
        final long snapshotRevision;
        final StorageService.Document previous;
        final Path destination;
        synchronized (this) {
            if (path == null || !dirty()) {
                return;
            }
            writable();
            snapshot = new Snapshot(profiles());
            snapshotRevision = revision;
            previous = document;
            destination = path;
        }
        StorageService.write(
                destination,
                DOMAIN,
                SCHEMA_VERSION,
                GSON.toJsonTree(snapshot),
                previous,
                Set.of("/profiles"));
        synchronized (this) {
            document = StorageService.read(destination, DOMAIN, SCHEMA_VERSION).orElse(previous);
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

    private void validate(Profile profile) {
        Objects.requireNonNull(profile, "profile");
        if (profile.selectedPlayerIds().size() > maximumSelected) {
            throw new IllegalArgumentException("Command spy selected player limit exceeded");
        }
        if (profile.includedRoots().size() + profile.excludedRoots().size() > MAXIMUM_FILTERS
                || profile.includedActions().size() + profile.excludedActions().size() > MAXIMUM_FILTERS
                || profile.typedFilters().size() > MAXIMUM_FILTERS) {
            throw new IllegalArgumentException("Command spy filter limit exceeded");
        }
    }

    private void writable() {
        if (state == RepositoryState.RECOVERY
                || state == RepositoryState.UNSUPPORTED
                || state == RepositoryState.ERROR
                || state == RepositoryState.CLOSED) {
            throw new IllegalStateException("Command spy repository is not writable in " + state + " state");
        }
    }

    public record Snapshot(List<Profile> profiles) {
        public Snapshot {
            profiles = List.copyOf(profiles == null ? List.of() : profiles);
        }
    }

    public record Profile(
            UUID observerId,
            boolean enabled,
            Audience audience,
            Set<UUID> selectedPlayerIds,
            ActorRelation actorRelation,
            boolean playerSources,
            boolean nonPlayerSources,
            boolean includeLocation,
            boolean includeResults,
            Set<String> includedRoots,
            Set<String> excludedRoots,
            Set<String> includedActions,
            Set<String> excludedActions,
            TypedFilters typedFilters,
            long revision
    ) {
        public Profile {
            Objects.requireNonNull(observerId, "observerId");
            Objects.requireNonNull(audience, "audience");
            selectedPlayerIds = Set.copyOf(selectedPlayerIds == null ? Set.of() : selectedPlayerIds);
            Objects.requireNonNull(actorRelation, "actorRelation");
            includedRoots = normalized(includedRoots);
            excludedRoots = normalized(excludedRoots);
            includedActions = normalized(includedActions);
            excludedActions = normalized(excludedActions);
            typedFilters = typedFilters == null ? TypedFilters.defaults() : typedFilters;
            if (revision < 1) {
                throw new IllegalArgumentException("Command spy profile revision must be positive");
            }
        }

        public static Profile defaults(UUID observerId) {
            return new Profile(
                    observerId,
                    false,
                    Audience.EVERYONE,
                    Set.of(),
                    ActorRelation.EITHER,
                    true,
                    false,
                    false,
                    false,
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    TypedFilters.defaults(),
                    1L);
        }

        public Profile withEnabled(boolean value) {
            return new Profile(observerId, value, audience, selectedPlayerIds, actorRelation, playerSources,
                    nonPlayerSources, includeLocation, includeResults, includedRoots, excludedRoots,
                    includedActions, excludedActions, typedFilters, revision + 1);
        }

        public Profile withAudience(Audience value, Set<UUID> selected) {
            return new Profile(observerId, enabled, value, selected, actorRelation, playerSources,
                    nonPlayerSources, includeLocation, includeResults, includedRoots, excludedRoots,
                    includedActions, excludedActions, typedFilters, revision + 1);
        }

        public Profile withRelation(ActorRelation value) {
            return new Profile(observerId, enabled, audience, selectedPlayerIds, value, playerSources,
                    nonPlayerSources, includeLocation, includeResults, includedRoots, excludedRoots,
                    includedActions, excludedActions, typedFilters, revision + 1);
        }

        public Profile withSources(boolean players, boolean nonPlayers) {
            return new Profile(observerId, enabled, audience, selectedPlayerIds, actorRelation, players,
                    nonPlayers, includeLocation, includeResults, includedRoots, excludedRoots,
                    includedActions, excludedActions, typedFilters, revision + 1);
        }

        public Profile withProjection(boolean location, boolean results) {
            return new Profile(observerId, enabled, audience, selectedPlayerIds, actorRelation, playerSources,
                    nonPlayerSources, location, results, includedRoots, excludedRoots,
                    includedActions, excludedActions, typedFilters, revision + 1);
        }

        public Profile withFilters(
                Set<String> includeRoots,
                Set<String> excludeRoots,
                Set<String> includeActions,
                Set<String> excludeActions
        ) {
            return new Profile(observerId, enabled, audience, selectedPlayerIds, actorRelation, playerSources,
                    nonPlayerSources, includeLocation, includeResults, includeRoots, excludeRoots,
                    includeActions, excludeActions, typedFilters, revision + 1);
        }

        public Profile withTypedFilters(TypedFilters replacement) {
            return new Profile(observerId, enabled, audience, selectedPlayerIds, actorRelation, playerSources,
                    nonPlayerSources, includeLocation, includeResults, includedRoots, excludedRoots,
                    includedActions, excludedActions, replacement, revision + 1);
        }

        private static Set<String> normalized(Set<String> values) {
            return (values == null ? Set.<String>of() : values).stream()
                    .map(value -> Objects.requireNonNull(value, "filter").trim().toLowerCase(Locale.ROOT))
                    .filter(value -> !value.isBlank())
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        }
    }

    public record TypedFilters(
            Set<String> disabledSources,
            Set<String> disabledResults,
            Set<String> disabledWorlds,
            Set<String> disabledOrigins,
            Set<String> includedPlayers,
            Set<String> excludedPlayers
    ) {
        public TypedFilters {
            disabledSources = Profile.normalized(disabledSources);
            disabledResults = Profile.normalized(disabledResults);
            disabledWorlds = Profile.normalized(disabledWorlds);
            disabledOrigins = Profile.normalized(disabledOrigins);
            includedPlayers = Profile.normalized(includedPlayers);
            excludedPlayers = Profile.normalized(excludedPlayers);
        }

        public static TypedFilters defaults() {
            return new TypedFilters(Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of());
        }

        public int size() {
            return disabledSources.size() + disabledResults.size()
                    + disabledWorlds.size() + disabledOrigins.size()
                    + includedPlayers.size() + excludedPlayers.size();
        }

        public TypedFilters withValue(String kind, String value, boolean enabled) {
            Set<String> sources = new java.util.HashSet<>(disabledSources);
            Set<String> results = new java.util.HashSet<>(disabledResults);
            Set<String> worlds = new java.util.HashSet<>(disabledWorlds);
            Set<String> origins = new java.util.HashSet<>(disabledOrigins);
            Set<String> destination = switch (kind) {
                case "source" -> sources;
                case "result" -> results;
                case "world" -> worlds;
                case "origin" -> origins;
                default -> throw new IllegalArgumentException("Unknown command spy typed filter");
            };
            if (enabled) {
                destination.remove(value);
            } else {
                destination.add(value);
            }
            return new TypedFilters(sources, results, worlds, origins, includedPlayers, excludedPlayers);
        }

        public TypedFilters withPlayer(String playerId, boolean include, boolean enabled) {
            Set<String> includes = new java.util.HashSet<>(includedPlayers);
            Set<String> excludes = new java.util.HashSet<>(excludedPlayers);
            if (include) {
                excludes.remove(playerId);
                if (enabled) {
                    includes.add(playerId);
                } else {
                    includes.remove(playerId);
                }
            } else {
                includes.remove(playerId);
                if (enabled) {
                    excludes.add(playerId);
                } else {
                    excludes.remove(playerId);
                }
            }
            return new TypedFilters(
                    disabledSources,
                    disabledResults,
                    disabledWorlds,
                    disabledOrigins,
                    includes,
                    excludes);
        }

        public TypedFilters withoutPlayer(String playerId) {
            Set<String> includes = new java.util.HashSet<>(includedPlayers);
            Set<String> excludes = new java.util.HashSet<>(excludedPlayers);
            includes.remove(playerId);
            excludes.remove(playerId);
            return new TypedFilters(
                    disabledSources,
                    disabledResults,
                    disabledWorlds,
                    disabledOrigins,
                    includes,
                    excludes);
        }
    }

    public enum Audience {
        EVERYONE,
        SELECTED
    }

    public enum ActorRelation {
        INITIATOR,
        EFFECTIVE,
        EITHER
    }
}
