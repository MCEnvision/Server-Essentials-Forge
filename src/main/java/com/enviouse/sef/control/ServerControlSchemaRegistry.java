package com.enviouse.sef.control;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
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

public final class ServerControlSchemaRegistry {
    public static final int MAXIMUM_FIELDS = 32;
    public static final int MAXIMUM_VALUE_LENGTH = 4096;
    private static final Map<String, FeatureSchema> SCHEMAS = build();

    private ServerControlSchemaRegistry() {
    }

    public static FeatureSchema require(String featureId) {
        FeatureSchema schema = SCHEMAS.get(normalize(featureId));
        if (schema == null) {
            throw new IllegalArgumentException("server control schema is unavailable");
        }
        return schema;
    }

    public static List<FeatureSchema> schemas() {
        return SCHEMAS.values().stream()
                .sorted(Comparator.comparing(FeatureSchema::featureId))
                .toList();
    }

    public static String validate(String featureId, String fieldId, String value) {
        FieldDefinition field = require(featureId).field(fieldId);
        return field.validate(value);
    }

    private static Map<String, FeatureSchema> build() {
        Map<String, FeatureSchema> result = new LinkedHashMap<>();
        for (ServerControlCatalog.FeatureDefinition feature : ServerControlCatalog.FEATURES) {
            FeatureSchema schema = schema(feature.id());
            if (result.putIfAbsent(feature.id(), schema) != null) {
                throw new IllegalStateException("duplicate server control schema");
            }
        }
        return Map.copyOf(result);
    }

    private static FeatureSchema schema(String id) {
        return switch (id) {
            case "maintenance" -> policy(id, ScreenArchetype.TIMELINE, HudPolicy.REQUIRED,
                    text("message", true, 512),
                    instant("starts_at", false),
                    duration("duration_seconds", false, 0, 2_592_000),
                    bool("deny_login", true),
                    integer("reminder_seconds", false, 5, 3600));
            case "policy_lab" -> query(id, ScreenArchetype.DIFF, HudPolicy.NONE,
                    text("candidate", true, 2048),
                    enumeration("scope", true, "command", "player", "world", "server"),
                    uuid("subject", false),
                    world("world", false));
            case "config_drift" -> query(id, ScreenArchetype.DIFF, HudPolicy.CONTEXTUAL,
                    enumeration("scope", true, "all", "modules", "world", "providers"),
                    bool("auto_repair", true));
            case "guardrails" -> policy(id, ScreenArchetype.RULE_EDITOR, HudPolicy.REQUIRED,
                    enumeration("metric", true, "players", "tick_time", "memory", "entities", "commands"),
                    decimal("threshold", true, 0, 1_000_000_000),
                    enumeration("response", true, "warn", "pause", "deny", "rollback"),
                    duration("window_seconds", true, 1, 86_400));
            case "change_windows" -> scheduled(id, ScreenArchetype.CALENDAR, HudPolicy.CONTEXTUAL,
                    instant("opens_at", true),
                    duration("duration_seconds", true, 1, 2_592_000),
                    list("allowed_actions", true, 128),
                    text("reason", false, 512));
            case "permission_impact" -> query(id, ScreenArchetype.DIFF, HudPolicy.NONE,
                    text("permission", true, 128),
                    enumeration("operation", true, "grant", "deny", "remove"),
                    uuid("subject", false));
            case "dependency_graph" -> query(id, ScreenArchetype.GRAPH, HudPolicy.NONE,
                    text("feature", true, 128),
                    bool("include_disabled", true));
            case "player_impact" -> query(id, ScreenArchetype.DIFF, HudPolicy.NONE,
                    text("action", true, 128),
                    list("targets", false, 128),
                    text("parameters", false, 1024));
            case "resource_governor" -> policy(id, ScreenArchetype.DASHBOARD, HudPolicy.REQUIRED,
                    integer("maximum_tick_millis", true, 1, 60_000),
                    integer("maximum_entities", true, 0, 1_000_000),
                    integer("maximum_items", true, 0, 1_000_000),
                    enumeration("response", true, "observe", "throttle", "pause_jobs"));
            case "operational_snapshots" -> query(id, ScreenArchetype.DIFF, HudPolicy.NONE,
                    enumeration("scope", true, "all", "configuration", "world", "staff", "jobs"),
                    text("label", true, 128));
            case "reports", "tickets", "appeals", "access_applications", "privacy" ->
                    queue(id, ScreenArchetype.QUEUE, HudPolicy.CONTEXTUAL,
                            enumeration("category", true, categories(id)),
                            text("description", true, 4096),
                            list("attachments", false, 16),
                            enumeration("priority", true, "low", "normal", "high", "urgent"));
            case "staff_notes" -> queue(id, ScreenArchetype.TIMELINE, HudPolicy.NONE,
                    uuid("subject", true),
                    enumeration("visibility", true, "staff", "senior", "owner"),
                    text("note", true, 4096),
                    instant("expires_at", false));
            case "chat_channels" -> policy(id, ScreenArchetype.RULE_EDITOR, HudPolicy.CONTEXTUAL,
                    text("channel", true, 32),
                    text("format", true, 512),
                    text("permission", false, 128),
                    integer("radius", false, 0, 100_000),
                    bool("default", true));
            case "mentions" -> policy(id, ScreenArchetype.PREFERENCES, HudPolicy.CONTEXTUAL,
                    enumeration("mode", true, "all", "friends", "staff", "off"),
                    bool("sound", true),
                    duration("cooldown_seconds", true, 0, 3600));
            case "friends" -> transaction(id, ScreenArchetype.RELATIONSHIPS, HudPolicy.NONE,
                    uuid("player", true),
                    enumeration("relationship", true, "friend", "trusted"),
                    instant("expires_at", false));
            case "interaction_blocks" -> policy(id, ScreenArchetype.PREFERENCES, HudPolicy.NONE,
                    uuid("player", false),
                    enumeration(
                            "interaction",
                            true,
                            "messages",
                            "teleports",
                            "mail",
                            "payments",
                            "mentions",
                            "friends",
                            "home_invites",
                            "event_invites",
                            "trade",
                            "parcels",
                            "all"),
                    bool("blocked", true));
            case "session_quarantine" -> policy(id, ScreenArchetype.QUEUE, HudPolicy.REQUIRED,
                    uuid("subject", true),
                    text("reason", true, 512),
                    duration("duration_seconds", true, 1, 2_592_000),
                    enumeration("scope", true, "commands", "chat", "movement", "all"));
            case "player_warp_review" -> queue(id, ScreenArchetype.QUEUE, HudPolicy.NONE,
                    text("warp", true, 64),
                    enumeration("reason", true, "unsafe", "misleading", "abuse", "inactive", "other"),
                    text("note", false, 1024));
            case "rules" -> policy(id, ScreenArchetype.ARTICLE, HudPolicy.REQUIRED,
                    text("revision", true, 64),
                    text("content", true, 4096),
                    bool("acceptance_required", true),
                    enumeration("restriction", true, "none", "chat", "commands", "lobby"));
            case "onboarding" -> scheduled(id, ScreenArchetype.CHECKLIST, HudPolicy.REQUIRED,
                    text("checklist", true, 2048),
                    duration("reminder_seconds", true, 10, 86_400),
                    text("completion_bundle", false, 128));
            case "playtime_rewards", "daily_rewards", "weekly_rewards" ->
                    transaction(id, ScreenArchetype.REWARDS, HudPolicy.CONTEXTUAL,
                            duration("period_seconds", true, rewardPeriod(id), 31_536_000),
                            text("reward_bundle", true, 128),
                            integer("maximum_claims", true, 1, 10_000),
                            bool("requires_inventory_space", true));
            case "sleep_vote" -> policy(id, ScreenArchetype.VOTE, HudPolicy.REQUIRED,
                    integer("required_percent", true, 1, 100),
                    duration("acceleration_seconds", true, 1, 600),
                    bool("ignore_afk", true),
                    bool("clear_weather", true));
            case "death_compass" -> policy(id, ScreenArchetype.MAP, HudPolicy.CONTEXTUAL,
                    duration("retention_seconds", true, 60, 31_536_000),
                    integer("maximum_locations", true, 1, 128),
                    bool("cross_dimension", true));
            case "afk_zones" -> policy(id, ScreenArchetype.MAP, HudPolicy.CONTEXTUAL,
                    world("world", true),
                    text("minimum", true, 128),
                    text("maximum", true, 128),
                    duration("afk_after_seconds", true, 1, 86_400),
                    bool("protect", true));
            case "graves" -> transaction(id, ScreenArchetype.MAP, HudPolicy.CONTEXTUAL,
                    duration("retention_seconds", true, 60, 2_592_000),
                    enumeration("container", true, "chest", "barrel", "virtual"),
                    bool("protect_owner", true),
                    bool("keep_experience", true));
            case "inventory_recovery" -> transaction(id, ScreenArchetype.TIMELINE, HudPolicy.NONE,
                    integer("maximum_snapshots", true, 1, 128),
                    duration("retention_seconds", true, 60, 31_536_000),
                    bool("include_ender_chest", true));
            case "restart_coordinator" -> scheduled(id, ScreenArchetype.TIMELINE, HudPolicy.REQUIRED,
                    instant("restart_at", true),
                    list("warnings_seconds", true, 32),
                    text("message", true, 512),
                    bool("save_worlds", true));
            case "resource_worlds" -> scheduled(id, ScreenArchetype.MAP, HudPolicy.REQUIRED,
                    world("world", true),
                    instant("reset_at", true),
                    text("generator", false, 128),
                    bool("evacuate", true));
            case "chunk_pregen" -> integration(id, ScreenArchetype.PROGRESS, HudPolicy.REQUIRED,
                    world("world", true),
                    text("center", true, 128),
                    integer("radius", true, 1, 100_000),
                    text("provider", true, 64));
            case "cleanup" -> scheduled(id, ScreenArchetype.PROGRESS, HudPolicy.CONTEXTUAL,
                    enumeration("targets", true, "items", "experience", "mobs", "projectiles", "all"),
                    duration("interval_seconds", true, 10, 604_800),
                    integer("minimum_age_seconds", true, 0, 86_400),
                    list("worlds", false, 128));
            case "performance" -> query(id, ScreenArchetype.DASHBOARD, HudPolicy.REQUIRED,
                    duration("sample_seconds", true, 1, 3600),
                    bool("include_entities", true),
                    bool("include_chunks", true));
            case "admin_journal" -> transaction(id, ScreenArchetype.TIMELINE, HudPolicy.NONE,
                    text("action", true, 128),
                    text("inverse_action", false, 128),
                    text("snapshot", true, 4096),
                    bool("reversible", true));
            case "command_anomaly" -> policy(id, ScreenArchetype.DASHBOARD, HudPolicy.REQUIRED,
                    integer("commands_per_window", true, 1, 100_000),
                    duration("window_seconds", true, 1, 3600),
                    enumeration("response", true, "observe", "alert", "throttle", "quarantine"));
            case "incidents" -> queue(id, ScreenArchetype.WORKSPACE, HudPolicy.REQUIRED,
                    enumeration("severity", true, "low", "medium", "high", "critical"),
                    text("summary", true, 2048),
                    list("responders", false, 128),
                    list("linked_records", false, 128));
            case "rollouts" -> scheduled(id, ScreenArchetype.PROGRESS, HudPolicy.REQUIRED,
                    text("change", true, 128),
                    integer("canary_percent", true, 1, 100),
                    duration("stage_seconds", true, 1, 604_800),
                    enumeration("failure_response", true, "pause", "rollback", "continue"));
            case "server_calendar" -> scheduled(id, ScreenArchetype.CALENDAR, HudPolicy.CONTEXTUAL,
                    instant("starts_at", true),
                    duration("duration_seconds", true, 1, 2_592_000),
                    text("location", false, 128),
                    text("description", false, 2048));
            case "waypoints" -> policy(id, ScreenArchetype.MAP, HudPolicy.NONE,
                    world("world", true),
                    text("position", true, 128),
                    text("icon", false, 128),
                    enumeration("visibility", true, "private", "friends", "public"));
            case "portal_policy" -> policy(id, ScreenArchetype.GRAPH, HudPolicy.NONE,
                    world("source_world", true),
                    world("destination_world", true),
                    enumeration("mode", true, "allow", "deny", "redirect"),
                    text("redirect_position", false, 128));
            case "alias_diagnostics" -> query(id, ScreenArchetype.DASHBOARD, HudPolicy.NONE,
                    enumeration("scope", true, "aliases", "bundles", "panels", "warps", "all"),
                    bool("include_inactive", true));
            case "staff_duty" -> policy(id, ScreenArchetype.QUEUE, HudPolicy.REQUIRED,
                    enumeration("status", true, "on_duty", "busy", "handoff", "off_duty"),
                    text("queue", false, 64),
                    text("handoff_note", false, 2048));
            case "approvals" -> transaction(id, ScreenArchetype.APPROVAL, HudPolicy.REQUIRED,
                    text("action", true, 128),
                    text("preview_hash", true, 64),
                    instant("expires_at", true),
                    bool("separation_required", true));
            case "discipline" -> queue(id, ScreenArchetype.TIMELINE, HudPolicy.NONE,
                    uuid("subject", true),
                    text("policy", true, 128),
                    integer("points", true, 0, 1_000_000),
                    instant("decays_at", false));
            case "capability_leases" -> transaction(id, ScreenArchetype.APPROVAL, HudPolicy.REQUIRED,
                    uuid("subject", true),
                    text("permission", true, 128),
                    duration("duration_seconds", true, 1, 2_592_000),
                    text("provider", true, 64));
            case "admin_lock" -> policy(id, ScreenArchetype.APPROVAL, HudPolicy.REQUIRED,
                    enumeration("mode", true, "unlocked", "locked", "break_glass"),
                    duration("session_seconds", true, 1, 86_400),
                    text("reauth_provider", false, 64));
            case "automod" -> policy(id, ScreenArchetype.RULE_EDITOR, HudPolicy.REQUIRED,
                    enumeration("matcher", true, "literal", "glob", "regex_adapter"),
                    text("pattern", true, 512),
                    enumeration("response", true, "flag", "block", "mute_proposal", "quarantine_proposal"),
                    integer("severity", true, 1, 100));
            case "chat_control" -> policy(id, ScreenArchetype.DASHBOARD, HudPolicy.REQUIRED,
                    enumeration("mode", true, "open", "slow", "read_only", "staff_only", "locked"),
                    duration("slow_seconds", false, 0, 3600),
                    text("message", false, 512),
                    instant("restores_at", false));
            case "admission" -> policy(id, ScreenArchetype.DASHBOARD, HudPolicy.REQUIRED,
                    integer("maximum_players", true, 0, 1_000_000),
                    integer("reserved_slots", true, 0, 1_000_000),
                    integer("joins_per_minute", true, 1, 100_000),
                    text("denial_message", true, 512));
            case "queue" -> integration(id, ScreenArchetype.QUEUE, HudPolicy.REQUIRED,
                    enumeration("mode", true, "deny_retry", "restricted_lobby", "proxy_adapter"),
                    text("provider", false, 64),
                    duration("retry_seconds", true, 1, 3600),
                    integer("maximum_entries", true, 1, 100_000));
            case "invites" -> transaction(id, ScreenArchetype.QUEUE, HudPolicy.NONE,
                    text("code", true, 128),
                    integer("uses", true, 1, 100_000),
                    instant("expires_at", true),
                    text("grant_profile", true, 128));
            case "resource_packs" -> integration(id, ScreenArchetype.PROGRESS, HudPolicy.REQUIRED,
                    url("url", true),
                    hash("sha1", true, 40),
                    bool("required", true),
                    text("prompt", false, 512));
            case "server_presentation" -> scheduled(id, ScreenArchetype.PREVIEW, HudPolicy.NONE,
                    text("motd", true, 1024),
                    text("icon", false, 128),
                    text("player_sample", false, 512),
                    instant("starts_at", false));
            case "world_policy" -> policy(id, ScreenArchetype.RULE_EDITOR, HudPolicy.REQUIRED,
                    world("world", true),
                    text("gamerules", true, 4096),
                    enumeration("drift_response", true, "report", "restore", "pause"),
                    instant("starts_at", false));
            case "world_border" -> scheduled(id, ScreenArchetype.MAP, HudPolicy.REQUIRED,
                    world("world", true),
                    decimal("center_x", true, -29_999_984, 29_999_984),
                    decimal("center_z", true, -29_999_984, 29_999_984),
                    decimal("size", true, 1, 59_999_968),
                    duration("transition_seconds", true, 0, 31_536_000));
            case "spawn_ecology" -> policy(id, ScreenArchetype.DASHBOARD, HudPolicy.CONTEXTUAL,
                    world("world", true),
                    enumeration("category", true, "monster", "creature", "ambient", "water", "all"),
                    integer("cap", true, 0, 1_000_000),
                    list("deny_entities", false, 512));
            case "chunk_tickets" -> query(id, ScreenArchetype.DASHBOARD, HudPolicy.NONE,
                    world("world", false),
                    text("owner", false, 128),
                    bool("sef_owned_only", true));
            case "block_activity" -> query(id, ScreenArchetype.DASHBOARD, HudPolicy.CONTEXTUAL,
                    world("world", true),
                    integer("radius", true, 1, 10_000),
                    duration("sample_seconds", true, 1, 3600),
                    enumeration("kind", true, "redstone", "block_updates", "all"));
            case "datapacks" -> integration(id, ScreenArchetype.DIFF, HudPolicy.REQUIRED,
                    text("pack", true, 128),
                    hash("sha256", false, 64),
                    enumeration("operation", true, "scan", "stage", "validate", "publish", "rollback"),
                    bool("reload", true));
            case "mod_health" -> query(id, ScreenArchetype.DASHBOARD, HudPolicy.NONE,
                    enumeration("scope", true, "mods", "providers", "adapters", "commands", "all"),
                    bool("export", true));
            case "backups" -> integration(id, ScreenArchetype.PROGRESS, HudPolicy.REQUIRED,
                    text("provider", true, 64),
                    text("label", true, 128),
                    enumeration("operation", true, "checkpoint", "verify", "restore_stage", "rehearse"),
                    integer("retention", true, 1, 10_000));
            case "evidence" -> transaction(id, ScreenArchetype.VAULT, HudPolicy.NONE,
                    text("case", true, 128),
                    enumeration("source_type", true, "audit", "command", "chat", "inventory", "file_reference"),
                    text("source_reference", true, 512),
                    bool("sealed", true));
            case "parcels" -> transaction(id, ScreenArchetype.ESCROW, HudPolicy.CONTEXTUAL,
                    enumeration("operation", false,
                            "create", "accept", "decline", "return", "cancel", "freeze", "unfreeze", "recover"),
                    uuid("escrow_id", false),
                    uuid("recipient", false),
                    text("items", false, 4096),
                    decimal("currency", false, 0, 1_000_000_000),
                    instant("expires_at", false),
                    text("message", false, 1024),
                    text("reason", false, 512));
            case "lost_found" -> transaction(id, ScreenArchetype.VAULT, HudPolicy.CONTEXTUAL,
                    enumeration("operation", false,
                            "create", "claim", "return", "freeze", "unfreeze", "recover"),
                    uuid("escrow_id", false),
                    enumeration("source", false, "grave", "parcel", "trade", "auction", "kit", "admin"),
                    text("source_reference", false, 64),
                    text("items", false, 4096),
                    uuid("claimant", false),
                    instant("expires_at", false),
                    text("reason", false, 512));
            case "trades" -> transaction(id, ScreenArchetype.ESCROW, HudPolicy.REQUIRED,
                    enumeration("operation", false,
                            "create", "accept", "decline", "return", "cancel", "freeze", "unfreeze", "recover"),
                    uuid("escrow_id", false),
                    uuid("partner", false),
                    text("offer", false, 4096),
                    decimal("currency", false, 0, 1_000_000_000),
                    bool("ready", false),
                    hash("offer_hash", false, 64),
                    instant("expires_at", false),
                    text("reason", false, 512));
            case "auctions" -> transaction(id, ScreenArchetype.MARKET, HudPolicy.CONTEXTUAL,
                    enumeration("operation", false,
                            "create", "buy", "bid", "settle", "cancel", "watch", "unwatch",
                            "freeze", "unfreeze", "recover"),
                    uuid("escrow_id", false),
                    text("item", false, 2048),
                    decimal("price", false, 0, 1_000_000_000),
                    decimal("amount", false, 0, 1_000_000_000),
                    enumeration("sale_type", false, "buy_now", "bid"),
                    instant("expires_at", false),
                    text("reason", false, 512));
            case "polls" -> transaction(id, ScreenArchetype.VOTE, HudPolicy.CONTEXTUAL,
                    text("question", true, 512),
                    list("choices", true, 32),
                    enumeration("ballot", true, "public", "private", "anonymous"),
                    instant("closes_at", true));
            case "community_events" -> scheduled(id, ScreenArchetype.CALENDAR, HudPolicy.REQUIRED,
                    instant("starts_at", true),
                    integer("capacity", true, 1, 100_000),
                    text("location", false, 128),
                    text("reward_bundle", false, 128));
            case "knowledge" -> transaction(id, ScreenArchetype.ARTICLE, HudPolicy.NONE,
                    text("article_id", true, 64),
                    text("locale", true, 16),
                    text("content", true, 4096),
                    list("bindings", false, 128));
            case "display_profiles" -> policy(id, ScreenArchetype.PREVIEW, HudPolicy.REQUIRED,
                    enumeration("surface", true, "sidebar", "tab", "bossbar", "actionbar", "hud", "toast"),
                    text("template", true, 2048),
                    integer("priority", true, -10_000, 10_000),
                    duration("refresh_seconds", true, 1, 3600));
            case "display_ownership" -> policy(id, ScreenArchetype.DASHBOARD, HudPolicy.REQUIRED,
                    enumeration("surface", true, "sidebar", "tab", "bossbar", "actionbar", "hud", "toast"),
                    text("owner", true, 128),
                    integer("priority", true, -10_000, 10_000),
                    duration("lease_seconds", true, 1, 86_400));
            default -> throw new IllegalStateException("missing server control schema for " + id);
        };
    }

    private static FeatureSchema policy(
            String id,
            ScreenArchetype screen,
            HudPolicy hud,
            FieldDefinition... fields
    ) {
        return feature(id, RuntimeClass.LIVE_POLICY, screen, hud, true, fields);
    }

    private static FeatureSchema queue(
            String id,
            ScreenArchetype screen,
            HudPolicy hud,
            FieldDefinition... fields
    ) {
        return feature(id, RuntimeClass.REVIEW_QUEUE, screen, hud, true, fields);
    }

    private static FeatureSchema transaction(
            String id,
            ScreenArchetype screen,
            HudPolicy hud,
            FieldDefinition... fields
    ) {
        return feature(id, RuntimeClass.TRANSACTION, screen, hud, true, fields);
    }

    private static FeatureSchema scheduled(
            String id,
            ScreenArchetype screen,
            HudPolicy hud,
            FieldDefinition... fields
    ) {
        return feature(id, RuntimeClass.SCHEDULED_JOB, screen, hud, true, fields);
    }

    private static FeatureSchema query(
            String id,
            ScreenArchetype screen,
            HudPolicy hud,
            FieldDefinition... fields
    ) {
        return feature(id, RuntimeClass.DIAGNOSTIC, screen, hud, false, fields);
    }

    private static FeatureSchema integration(
            String id,
            ScreenArchetype screen,
            HudPolicy hud,
            FieldDefinition... fields
    ) {
        return feature(id, RuntimeClass.INTEGRATION, screen, hud, true, fields);
    }

    private static FeatureSchema feature(
            String id,
            RuntimeClass runtimeClass,
            ScreenArchetype screen,
            HudPolicy hud,
            boolean reversible,
            FieldDefinition... fields
    ) {
        Set<Operation> operations = new LinkedHashSet<>(Set.of(
                Operation.CREATE,
                Operation.CONFIGURE,
                Operation.PREVIEW,
                Operation.CANCEL,
                Operation.ARCHIVE));
        switch (runtimeClass) {
            case DIAGNOSTIC -> operations.add(Operation.EXECUTE);
            case REVIEW_QUEUE -> {
                operations.add(Operation.APPROVE);
                operations.add(Operation.DENY);
                operations.add(Operation.RESOLVE);
            }
            case TRANSACTION -> {
                operations.add(Operation.APPROVE);
                operations.add(Operation.EXECUTE);
                operations.add(Operation.CLAIM);
            }
            case LIVE_POLICY, SCHEDULED_JOB, INTEGRATION -> {
                operations.add(Operation.ACTIVATE);
                operations.add(Operation.PAUSE);
                operations.add(Operation.EXECUTE);
            }
        }
        return new FeatureSchema(
                normalize(id),
                "sef:control/" + normalize(id),
                runtimeClass,
                screen,
                hud,
                reversible,
                runtimeClass == RuntimeClass.TRANSACTION || ServerControlCatalog.require(id).dangerous(),
                List.of(fields),
                Set.copyOf(operations));
    }

    private static FieldDefinition text(String id, boolean required, int maximum) {
        return new FieldDefinition(id, FieldType.TEXT, required, 0, maximum, Set.of());
    }

    private static FieldDefinition integer(String id, boolean required, long minimum, long maximum) {
        return new FieldDefinition(id, FieldType.INTEGER, required, minimum, maximum, Set.of());
    }

    private static FieldDefinition decimal(String id, boolean required, long minimum, long maximum) {
        return new FieldDefinition(id, FieldType.DECIMAL, required, minimum, maximum, Set.of());
    }

    private static FieldDefinition bool(String id, boolean required) {
        return new FieldDefinition(id, FieldType.BOOLEAN, required, 0, 0, Set.of());
    }

    private static FieldDefinition duration(String id, boolean required, long minimum, long maximum) {
        return new FieldDefinition(id, FieldType.DURATION_SECONDS, required, minimum, maximum, Set.of());
    }

    private static FieldDefinition instant(String id, boolean required) {
        return new FieldDefinition(id, FieldType.INSTANT, required, 0, 0, Set.of());
    }

    private static FieldDefinition uuid(String id, boolean required) {
        return new FieldDefinition(id, FieldType.UUID, required, 0, 0, Set.of());
    }

    private static FieldDefinition world(String id, boolean required) {
        return new FieldDefinition(id, FieldType.RESOURCE_LOCATION, required, 0, 0, Set.of());
    }

    private static FieldDefinition url(String id, boolean required) {
        return new FieldDefinition(id, FieldType.HTTPS_URL, required, 0, 0, Set.of());
    }

    private static FieldDefinition hash(String id, boolean required, int length) {
        return new FieldDefinition(id, FieldType.HASH, required, length, length, Set.of());
    }

    private static FieldDefinition list(String id, boolean required, int maximumEntries) {
        return new FieldDefinition(id, FieldType.LIST, required, 0, maximumEntries, Set.of());
    }

    private static FieldDefinition enumeration(String id, boolean required, String... values) {
        return new FieldDefinition(id, FieldType.ENUM, required, 0, 0, Set.of(values));
    }

    private static String[] categories(String id) {
        return switch (id) {
            case "reports" -> new String[]{"chat", "conduct", "cheating", "griefing", "other"};
            case "tickets" -> new String[]{"support", "bug", "billing", "recovery", "other"};
            case "appeals" -> new String[]{"warning", "mute", "jail", "ban", "other"};
            case "access_applications" -> new String[]{"member", "builder", "staff", "event", "other"};
            case "privacy" -> new String[]{"export", "correction", "deletion", "consent", "other"};
            default -> throw new IllegalArgumentException("queue category is unavailable");
        };
    }

    private static long rewardPeriod(String id) {
        return switch (id) {
            case "playtime_rewards" -> 3600L;
            case "daily_rewards" -> 86_400L;
            case "weekly_rewards" -> 604_800L;
            default -> throw new IllegalArgumentException("reward period is unavailable");
        };
    }

    private static String normalize(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9][a-z0-9_]{0,63}")) {
            throw new IllegalArgumentException("server control schema id is invalid");
        }
        return normalized;
    }

    public record FeatureSchema(
            String featureId,
            String workflowId,
            RuntimeClass runtimeClass,
            ScreenArchetype screen,
            HudPolicy hud,
            boolean reversible,
            boolean confirmationRequired,
            List<FieldDefinition> fields,
            Set<Operation> operations
    ) {
        public FeatureSchema {
            featureId = normalize(featureId);
            workflowId = Objects.requireNonNull(workflowId, "workflowId");
            runtimeClass = Objects.requireNonNull(runtimeClass, "runtimeClass");
            screen = Objects.requireNonNull(screen, "screen");
            hud = Objects.requireNonNull(hud, "hud");
            fields = List.copyOf(fields);
            operations = Set.copyOf(operations);
            if (!workflowId.equals("sef:control/" + featureId)
                    || fields.isEmpty()
                    || fields.size() > MAXIMUM_FIELDS
                    || operations.isEmpty()) {
                throw new IllegalArgumentException("server control feature schema is invalid");
            }
            Set<String> ids = new LinkedHashSet<>();
            for (FieldDefinition field : fields) {
                if (!ids.add(field.id())) {
                    throw new IllegalArgumentException("duplicate server control field");
                }
            }
        }

        public FieldDefinition field(String fieldId) {
            String normalized = normalize(fieldId);
            return fields.stream()
                    .filter(field -> field.id().equals(normalized))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("server control field is unavailable"));
        }

        public List<String> missing(Map<String, String> metadata) {
            Map<String, String> values = Objects.requireNonNullElse(metadata, Map.of());
            List<String> result = new ArrayList<>();
            for (FieldDefinition field : fields) {
                if (field.required() && !values.containsKey("field." + field.id())) {
                    result.add(field.id());
                }
            }
            return List.copyOf(result);
        }
    }

    public record FieldDefinition(
            String id,
            FieldType type,
            boolean required,
            long minimum,
            long maximum,
            Set<String> enumValues
    ) {
        public FieldDefinition {
            id = normalize(id);
            type = Objects.requireNonNull(type, "type");
            enumValues = enumValues.stream()
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
            if (minimum > maximum && type != FieldType.TEXT
                    || type == FieldType.ENUM && enumValues.isEmpty()
                    || type != FieldType.ENUM && !enumValues.isEmpty()) {
                throw new IllegalArgumentException("server control field definition is invalid");
            }
        }

        public String validate(String input) {
            String value = Objects.requireNonNullElse(input, "").strip();
            if (value.length() > MAXIMUM_VALUE_LENGTH
                    || value.codePoints().anyMatch(Character::isISOControl)) {
                throw new IllegalArgumentException("server control field value is outside bounds");
            }
            if (value.isEmpty()) {
                if (required) {
                    throw new IllegalArgumentException("server control field is required");
                }
                return "";
            }
            return switch (type) {
                case TEXT -> {
                    if (value.length() > maximum) {
                        throw new IllegalArgumentException("server control text is outside bounds");
                    }
                    yield value;
                }
                case INTEGER, DURATION_SECONDS -> {
                    if (!value.matches("-?(0|[1-9][0-9]{0,18})")) {
                        throw new IllegalArgumentException("server control integer is invalid");
                    }
                    long parsed;
                    try {
                        parsed = Long.parseLong(value);
                    } catch (NumberFormatException exception) {
                        throw new IllegalArgumentException("server control integer is invalid", exception);
                    }
                    if (parsed < minimum || parsed > maximum) {
                        throw new IllegalArgumentException("server control integer is outside bounds");
                    }
                    yield Long.toString(parsed);
                }
                case DECIMAL -> {
                    BigDecimal parsed;
                    try {
                        parsed = new BigDecimal(value);
                    } catch (NumberFormatException exception) {
                        throw new IllegalArgumentException("server control decimal is invalid", exception);
                    }
                    if (parsed.scale() > 4
                            || parsed.compareTo(BigDecimal.valueOf(minimum)) < 0
                            || parsed.compareTo(BigDecimal.valueOf(maximum)) > 0) {
                        throw new IllegalArgumentException("server control decimal is outside bounds");
                    }
                    yield parsed.stripTrailingZeros().toPlainString();
                }
                case BOOLEAN -> {
                    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException("server control boolean is invalid");
                    }
                    yield value.toLowerCase(Locale.ROOT);
                }
                case ENUM -> {
                    String normalized = value.toLowerCase(Locale.ROOT);
                    if (!enumValues.contains(normalized)) {
                        throw new IllegalArgumentException("server control enum value is invalid");
                    }
                    yield normalized;
                }
                case INSTANT -> {
                    try {
                        yield Instant.parse(value).toString();
                    } catch (RuntimeException exception) {
                        throw new IllegalArgumentException("server control instant is invalid", exception);
                    }
                }
                case UUID -> {
                    try {
                        yield java.util.UUID.fromString(value).toString();
                    } catch (IllegalArgumentException exception) {
                        throw new IllegalArgumentException("server control UUID is invalid", exception);
                    }
                }
                case RESOURCE_LOCATION -> {
                    String normalized = value.toLowerCase(Locale.ROOT);
                    if (!normalized.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
                        throw new IllegalArgumentException("server control resource location is invalid");
                    }
                    yield normalized;
                }
                case HTTPS_URL -> {
                    URI uri;
                    try {
                        uri = URI.create(value);
                    } catch (IllegalArgumentException exception) {
                        throw new IllegalArgumentException("server control URL is invalid", exception);
                    }
                    if (!"https".equalsIgnoreCase(uri.getScheme())
                            || uri.getHost() == null
                            || uri.getUserInfo() != null
                            || uri.getFragment() != null
                            || value.length() > 2048) {
                        throw new IllegalArgumentException("server control URL is invalid");
                    }
                    yield uri.normalize().toASCIIString();
                }
                case HASH -> {
                    String normalized = value.toLowerCase(Locale.ROOT);
                    if (normalized.length() < minimum
                            || normalized.length() > maximum
                            || !normalized.matches("[0-9a-f]+")) {
                        throw new IllegalArgumentException("server control hash is invalid");
                    }
                    yield normalized;
                }
                case LIST -> {
                    String[] entries = value.split(",", -1);
                    if (entries.length > maximum) {
                        throw new IllegalArgumentException("server control list is outside bounds");
                    }
                    List<String> normalized = new ArrayList<>(entries.length);
                    for (String entry : entries) {
                        String item = entry.strip();
                        if (item.isEmpty() || item.length() > 128) {
                            throw new IllegalArgumentException("server control list entry is invalid");
                        }
                        normalized.add(item);
                    }
                    yield String.join(",", normalized);
                }
            };
        }
    }

    public enum FieldType {
        TEXT,
        INTEGER,
        DECIMAL,
        BOOLEAN,
        ENUM,
        DURATION_SECONDS,
        INSTANT,
        UUID,
        RESOURCE_LOCATION,
        HTTPS_URL,
        HASH,
        LIST
    }

    public enum RuntimeClass {
        LIVE_POLICY,
        REVIEW_QUEUE,
        TRANSACTION,
        SCHEDULED_JOB,
        DIAGNOSTIC,
        INTEGRATION
    }

    public enum ScreenArchetype {
        DASHBOARD,
        TIMELINE,
        DIFF,
        GRAPH,
        QUEUE,
        RULE_EDITOR,
        PREFERENCES,
        RELATIONSHIPS,
        ARTICLE,
        CHECKLIST,
        REWARDS,
        VOTE,
        MAP,
        PROGRESS,
        WORKSPACE,
        CALENDAR,
        APPROVAL,
        PREVIEW,
        VAULT,
        ESCROW,
        MARKET
    }

    public enum HudPolicy {
        REQUIRED,
        CONTEXTUAL,
        NONE
    }

    public enum Operation {
        CREATE,
        CONFIGURE,
        PREVIEW,
        ACTIVATE,
        PAUSE,
        APPROVE,
        DENY,
        RESOLVE,
        EXECUTE,
        CLAIM,
        CANCEL,
        ARCHIVE
    }
}
