package com.enviouse.sef.control;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ServerControlCatalog {
    private static final Set<String> PLAYER_CREATE = Set.of(
            "reports", "tickets", "mentions", "friends", "interaction_blocks",
            "rules", "onboarding", "playtime_rewards", "daily_rewards", "weekly_rewards",
            "sleep_vote", "death_compass", "afk_zones", "graves", "server_calendar", "waypoints",
            "appeals", "access_applications", "privacy", "parcels", "lost_found", "trades",
            "auctions", "polls", "community_events", "knowledge");
    private static final Set<String> SENSITIVE = Set.of(
            "reports", "tickets", "staff_notes", "session_quarantine", "admin_journal",
            "incidents", "approvals",
            "appeals", "discipline", "capability_leases", "admin_lock", "access_applications",
            "privacy", "evidence", "backups");
    private static final Set<String> DANGEROUS = Set.of(
            "maintenance", "guardrails", "change_windows", "resource_governor",
            "session_quarantine", "restart_coordinator", "resource_worlds", "cleanup",
            "rollouts", "approvals", "capability_leases", "admin_lock", "chat_control",
            "admission", "queue", "resource_packs", "world_policy", "world_border",
            "spawn_ecology", "chunk_tickets", "datapacks", "backups", "evidence",
            "auctions", "display_ownership");
    public static final List<FeatureDefinition> FEATURES = build();
    public static final Map<String, FeatureDefinition> BY_ID = FEATURES.stream()
            .collect(Collectors.toUnmodifiableMap(FeatureDefinition::id, Function.identity()));

    private ServerControlCatalog() {
    }

    public static FeatureDefinition require(String id) {
        FeatureDefinition definition = BY_ID.get(normalize(id));
        if (definition == null) {
            throw new IllegalArgumentException("unknown server control feature");
        }
        return definition;
    }

    private static List<FeatureDefinition> build() {
        List<FeatureDefinition> result = new ArrayList<>();
        add(result, "operations",
                "maintenance", "policy_lab", "config_drift", "guardrails", "change_windows",
                "permission_impact", "dependency_graph", "player_impact", "resource_governor",
                "operational_snapshots");
        add(result, "community",
                "reports", "tickets", "staff_notes", "chat_channels", "mentions", "friends",
                "interaction_blocks", "session_quarantine", "player_warp_review");
        add(result, "onboarding",
                "rules", "onboarding", "playtime_rewards", "daily_rewards", "weekly_rewards",
                "sleep_vote", "death_compass", "afk_zones");
        add(result, "recovery",
                "graves", "inventory_recovery", "restart_coordinator", "resource_worlds",
                "chunk_pregen", "cleanup", "performance");
        add(result, "governance",
                "admin_journal", "command_anomaly", "incidents", "rollouts", "server_calendar",
                "waypoints", "portal_policy", "alias_diagnostics");
        add(result, "staff",
                "staff_duty", "approvals", "appeals", "discipline", "capability_leases",
                "admin_lock");
        add(result, "access",
                "automod", "chat_control", "admission", "queue", "access_applications", "invites");
        add(result, "world",
                "resource_packs", "server_presentation", "world_policy", "world_border",
                "spawn_ecology");
        add(result, "diagnostics",
                "chunk_tickets", "block_activity", "datapacks", "mod_health", "backups");
        add(result, "privacy", "privacy", "evidence");
        add(result, "market",
                "parcels", "lost_found", "trades", "auctions");
        add(result, "knowledge",
                "polls", "community_events", "knowledge");
        add(result, "display", "display_profiles", "display_ownership");
        result.sort(Comparator.comparing(FeatureDefinition::category).thenComparing(FeatureDefinition::id));
        return List.copyOf(result);
    }

    private static void add(List<FeatureDefinition> target, String category, String... ids) {
        for (String id : ids) {
            String normalized = normalize(id);
            target.add(new FeatureDefinition(
                    normalized,
                    title(normalized),
                    category,
                    PLAYER_CREATE.contains(normalized),
                    SENSITIVE.contains(normalized),
                    DANGEROUS.contains(normalized),
                    DANGEROUS.contains(normalized) ? 1000 : 10_000,
                    Set.of(
                            ServerControlRepository.RecordState.OPEN,
                            ServerControlRepository.RecordState.ACTIVE,
                            ServerControlRepository.RecordState.PAUSED,
                            ServerControlRepository.RecordState.APPROVED,
                            ServerControlRepository.RecordState.DENIED,
                            ServerControlRepository.RecordState.RESOLVED,
                            ServerControlRepository.RecordState.CANCELLED,
                            ServerControlRepository.RecordState.ARCHIVED,
                            ServerControlRepository.RecordState.EXPIRED)));
        }
    }

    private static String normalize(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9][a-z0-9_]{0,63}")) {
            throw new IllegalArgumentException("server control feature id is invalid");
        }
        return normalized;
    }

    private static String title(String id) {
        String[] parts = id.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }

    public record FeatureDefinition(
            String id,
            String title,
            String category,
            boolean playerCreate,
            boolean sensitive,
            boolean dangerous,
            int maximumRecords,
            Set<ServerControlRepository.RecordState> states
    ) {
        public FeatureDefinition {
            id = normalize(id);
            title = Objects.requireNonNull(title, "title").trim();
            category = normalize(category);
            states = Set.copyOf(Objects.requireNonNull(states, "states"));
            if (title.isBlank() || title.length() > 64
                    || maximumRecords < 1 || maximumRecords > 100_000
                    || states.isEmpty()) {
                throw new IllegalArgumentException("server control feature definition is invalid");
            }
        }
    }
}
