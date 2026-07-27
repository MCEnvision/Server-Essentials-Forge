package com.enviouse.sef.permissions;

import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.policy.CooldownDurationResolver;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class PermissionCooldownResolver implements CooldownDurationResolver {
    public static final long HARD_MAXIMUM_SECONDS = Duration.ofDays(365).toSeconds();
    public static final List<Long> NATIVE_TIERS = List.of(
            0L, 1L, 3L, 5L, 10L, 15L, 30L, 60L, 120L, 300L, 600L, 1800L, 3600L, 21600L, 86400L);
    private static final int MAXIMUM_CACHE_ENTRIES = 4096;
    private static final long CACHE_MILLIS = 1000L;

    private final Map<String, Definition> definitions;
    private final Map<String, Map<Long, PermissionNode<Boolean>>> nativeNodes;
    private final GrantProvider provider;
    private final Map<CacheKey, CacheEntry> cache = new ConcurrentHashMap<>();
    private final AtomicLong revision = new AtomicLong(1L);

    public PermissionCooldownResolver(List<CommandDefinition> commands) {
        this(definitions(commands), PermissionCooldownResolver::productionSnapshot, true);
    }

    PermissionCooldownResolver(
            List<Definition> definitions,
            GrantProvider provider,
            boolean registerNativeNodes
    ) {
        Objects.requireNonNull(definitions, "definitions");
        this.provider = Objects.requireNonNull(provider, "provider");
        if (definitions.size() > 8192) {
            throw new IllegalArgumentException("too many cooldown definitions");
        }
        Map<String, Definition> indexed = new LinkedHashMap<>();
        Map<String, Map<Long, PermissionNode<Boolean>>> registered = new LinkedHashMap<>();
        for (Definition definition : definitions) {
            if (indexed.putIfAbsent(definition.actionId(), definition) != null) {
                throw new IllegalArgumentException("duplicate cooldown action");
            }
            if (registerNativeNodes) {
                Map<Long, PermissionNode<Boolean>> tiers = new LinkedHashMap<>();
                for (long seconds : NATIVE_TIERS) {
                    String node = "sef.cooldown." + definition.permissionKey() + "." + seconds;
                    PermissionNode<Boolean> permission = PermissionManifest.register(
                            node.substring(4),
                            false,
                            "Cooldown " + definition.permissionKey() + " " + seconds,
                            "Sets the cooldown for " + definition.actionId() + " to " + seconds + " seconds");
                    tiers.put(seconds, permission);
                }
                registered.put(definition.actionId(), Map.copyOf(tiers));
            }
        }
        this.definitions = Map.copyOf(indexed);
        this.nativeNodes = Map.copyOf(registered);
    }

    @Override
    public Resolution resolve(UUID playerId, String actionId, Duration internalDefault) {
        Objects.requireNonNull(playerId, "playerId");
        String normalizedAction = normalizeAction(actionId);
        Definition definition = definitions.get(normalizedAction);
        Duration safeDefault = boundedDefault(internalDefault);
        if (definition == null) {
            return new Resolution(
                    normalizedAction,
                    actionKey(normalizedAction),
                    safeDefault,
                    "internal_default",
                    "",
                    true,
                    revision.get());
        }
        CacheKey key = new CacheKey(playerId, normalizedAction);
        long now = System.currentTimeMillis();
        CacheEntry cached = cache.get(key);
        if (cached != null && cached.expiresAtMillis() > now && cached.revision() == revision.get()) {
            return cached.resolution();
        }
        if (cache.size() >= MAXIMUM_CACHE_ENTRIES) {
            cache.entrySet().removeIf(entry -> entry.getValue().expiresAtMillis() <= now);
            if (cache.size() >= MAXIMUM_CACHE_ENTRIES) {
                cache.clear();
            }
        }
        Resolution resolved = resolveUncached(playerId, definition, safeDefault);
        cache.put(key, new CacheEntry(resolved, now + CACHE_MILLIS, revision.get()));
        return resolved;
    }

    public Resolution explain(UUID playerId, String actionId) {
        Definition definition = definitions.get(normalizeAction(actionId));
        Duration fallback = definition == null ? Duration.ZERO : definition.internalDefault();
        return resolve(playerId, actionId, fallback);
    }

    public List<Definition> definitions() {
        return definitions.values().stream()
                .sorted(Comparator.comparing(Definition::permissionKey))
                .toList();
    }

    public void invalidate() {
        revision.incrementAndGet();
        cache.clear();
    }

    public void invalidate(UUID playerId) {
        if (playerId != null) {
            cache.keySet().removeIf(key -> key.playerId().equals(playerId));
        }
    }

    public long revision() {
        return revision.get();
    }

    private Resolution resolveUncached(UUID playerId, Definition definition, Duration fallback) {
        GrantSnapshot snapshot;
        try {
            snapshot = provider.snapshot(playerId, definition.nodePrefix());
        } catch (RuntimeException | LinkageError exception) {
            return fallback(definition, fallback, "provider_outage");
        }
        if (snapshot == null || !snapshot.healthy()) {
            return fallback(definition, fallback, snapshot == null ? "provider_unavailable" : snapshot.provider());
        }
        Candidate direct = candidate(snapshot.directGranted(), definition.nodePrefix());
        if (direct != null) {
            return resolved(definition, direct, snapshot.provider() + "_direct");
        }
        Candidate inherited = candidate(snapshot.inheritedGranted(), definition.nodePrefix());
        if (inherited != null) {
            return resolved(definition, inherited, snapshot.provider());
        }
        if (!snapshot.dynamicSuffixes()) {
            Candidate nativeCandidate = nativeCandidate(playerId, definition);
            if (nativeCandidate != null) {
                return resolved(definition, nativeCandidate, "neoforge_registered_tier");
            }
        }
        return fallback(definition, fallback, snapshot.provider());
    }

    private Candidate nativeCandidate(UUID playerId, Definition definition) {
        var server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayer player = server == null ? null : server.getPlayerList().getPlayer(playerId);
        if (player == null) {
            return null;
        }
        return nativeNodes.getOrDefault(definition.actionId(), Map.of()).entrySet().stream()
                .filter(entry -> PermissionService.has(player, entry.getValue()))
                .min(Map.Entry.comparingByKey())
                .map(entry -> new Candidate(entry.getKey(), "sef.cooldown."
                        + definition.permissionKey() + "." + entry.getKey()))
                .orElse(null);
    }

    private Resolution resolved(Definition definition, Candidate candidate, String providerId) {
        return new Resolution(
                definition.actionId(),
                definition.permissionKey(),
                Duration.ofSeconds(candidate.seconds()),
                providerId,
                candidate.node(),
                false,
                revision.get());
    }

    private Resolution fallback(Definition definition, Duration fallback, String providerId) {
        return new Resolution(
                definition.actionId(),
                definition.permissionKey(),
                fallback,
                providerId,
                "",
                true,
                revision.get());
    }

    private static Candidate candidate(Set<String> nodes, String prefix) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        Candidate winner = null;
        for (String node : nodes) {
            if (node == null || !node.startsWith(prefix)) {
                continue;
            }
            String suffix = node.substring(prefix.length());
            Long seconds = parseSeconds(suffix);
            if (seconds == null) {
                continue;
            }
            if (winner == null || seconds < winner.seconds()) {
                winner = new Candidate(seconds, node);
            }
        }
        return winner;
    }

    static Long parseSeconds(String value) {
        if (value == null
                || value.isEmpty()
                || value.length() > 9
                || !value.chars().allMatch(character -> character >= '0' && character <= '9')) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value);
            return parsed <= HARD_MAXIMUM_SECONDS ? parsed : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static String actionKey(String actionId) {
        String normalized = normalizeAction(actionId);
        return switch (normalized) {
            case "sef:workstation.craft" -> "craft";
            case "sef:workstation.anvil" -> "anvil";
            case "sef:workstation.enchant" -> "enchantingtable";
            case "sef:workstation.super_enchant" -> "superenchantingtable";
            case "sef:workstation.super_enchant.mutate" -> "superenchantingtable.mutate";
            case "sef:workstation.repair" -> "repair";
            default -> normalized.substring(4).replace(':', '.').replace('_', '.');
        };
    }

    private static List<Definition> definitions(List<CommandDefinition> commands) {
        List<Definition> result = new ArrayList<>();
        Set<String> keys = new LinkedHashSet<>();
        for (CommandDefinition command : Objects.requireNonNull(commands, "commands")) {
            Duration fallback = internalDefault(command.id());
            String key = actionKey(command.id());
            if (!keys.add(key)) {
                key = command.id().substring(4).replace(':', '.');
                if (!keys.add(key)) {
                    throw new IllegalStateException("duplicate cooldown permission key " + key);
                }
            }
            result.add(new Definition(command.id(), key, fallback));
        }
        for (String ability : List.of("blaze_fireball", "blaze_hover", "blaze_fire_resistance")) {
            String action = "sef:disguise.ability." + ability;
            String key = "disguise." + ability.replace('_', '.');
            if (keys.add(key)) {
                result.add(new Definition(action, key, internalDefault(action)));
            }
        }
        return List.copyOf(result);
    }

    public static Duration internalDefault(String actionId) {
        String normalized = normalizeAction(actionId);
        if (normalized.startsWith("sef:teleport.")
                && !normalized.endsWith(".set")
                && !normalized.contains(".admin.")) {
            return Duration.ofSeconds(5);
        }
        if (normalized.startsWith("sef:inventory.")
                || normalized.startsWith("sef:item.")
                || normalized.startsWith("sef:utility.")
                || normalized.startsWith("sef:gamemode.")
                || normalized.startsWith("sef:kit.")) {
            return Duration.ofSeconds(3);
        }
        if (normalized.startsWith("sef:workstation.")) {
            return switch (normalized) {
                case "sef:workstation.craft" -> Duration.ofSeconds(10);
                case "sef:workstation.anvil", "sef:workstation.enchant" -> Duration.ofSeconds(30);
                case "sef:workstation.super_enchant",
                     "sef:workstation.super_enchant.mutate",
                     "sef:workstation.repair" -> Duration.ofSeconds(60);
                default -> Duration.ofSeconds(10);
            };
        }
        if (normalized.startsWith("sef:disguise.ability.")) {
            return Duration.ofSeconds(10);
        }
        return Duration.ZERO;
    }

    private static Duration boundedDefault(Duration value) {
        Duration result = Objects.requireNonNullElse(value, Duration.ZERO);
        if (result.isNegative() || result.getSeconds() > HARD_MAXIMUM_SECONDS || result.getNano() != 0) {
            throw new IllegalArgumentException("cooldown default is outside bounds");
        }
        return result;
    }

    private static String normalizeAction(String actionId) {
        String result = Objects.requireNonNull(actionId, "actionId").trim().toLowerCase(Locale.ROOT);
        if (!result.matches("sef:[a-z0-9_.-]{1,120}")) {
            throw new IllegalArgumentException("cooldown action id is invalid");
        }
        return result;
    }

    private static GrantSnapshot productionSnapshot(UUID playerId, String prefix) {
        if (!ModList.get().isLoaded("luckperms")) {
            return new GrantSnapshot("neoforge", true, false, Set.of(), Set.of());
        }
        return LuckPermsDynamicPermission.cooldownSnapshot(playerId, prefix);
    }

    public record Definition(String actionId, String permissionKey, Duration internalDefault) {
        public Definition {
            actionId = normalizeAction(actionId);
            permissionKey = Objects.requireNonNull(permissionKey, "permissionKey")
                    .trim()
                    .toLowerCase(Locale.ROOT);
            if (!permissionKey.matches("[a-z0-9][a-z0-9_.-]{0,119}")) {
                throw new IllegalArgumentException("cooldown permission key is invalid");
            }
            internalDefault = boundedDefault(internalDefault);
        }

        String nodePrefix() {
            return "sef.cooldown." + permissionKey + ".";
        }
    }

    public record GrantSnapshot(
            String provider,
            boolean healthy,
            boolean dynamicSuffixes,
            Set<String> directGranted,
            Set<String> inheritedGranted
    ) {
        public GrantSnapshot {
            provider = Objects.requireNonNullElse(provider, "unknown")
                    .trim()
                    .toLowerCase(Locale.ROOT);
            directGranted = Set.copyOf(Objects.requireNonNullElse(directGranted, Set.of()));
            inheritedGranted = Set.copyOf(Objects.requireNonNullElse(inheritedGranted, Set.of()));
        }
    }

    @FunctionalInterface
    interface GrantProvider {
        GrantSnapshot snapshot(UUID playerId, String prefix);
    }

    private record Candidate(long seconds, String node) {
    }

    private record CacheKey(UUID playerId, String actionId) {
    }

    private record CacheEntry(Resolution resolution, long expiresAtMillis, long revision) {
    }
}
