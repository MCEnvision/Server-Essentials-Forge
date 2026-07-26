package com.enviouse.sef.kernel.command;

import com.enviouse.sef.audit.AuditService;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public record CommandDefinition(
        String id,
        String canonicalRoute,
        Set<String> convenienceRoots,
        String descriptionKey,
        String usageKey,
        String helpCategory,
        String featureId,
        Set<String> permissionIds,
        AccessClass accessClass,
        Set<SourceType> sourceTypes,
        TargetBehavior targetBehavior,
        String cooldownId,
        boolean confirmationRequired,
        AuditService.AuditClass auditClass,
        String guiDescriptorId,
        String hudDescriptorId,
        String hudNotApplicableReason,
        String quotaId,
        String quotaNotApplicableReason,
        ConflictPolicy conflictPolicy,
        boolean playerFacing,
        boolean pipelineEnforced
) {
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_.-]*:[a-z0-9_./-]+");
    private static final Pattern ROUTE = Pattern.compile("[a-z0-9_:-]+(?: [a-z0-9_<>\\[\\].:-]+)*");
    private static final Pattern ROOT = Pattern.compile("[a-z0-9_:-]+");

    public CommandDefinition {
        id = normalized(id);
        canonicalRoute = normalized(canonicalRoute);
        descriptionKey = normalized(descriptionKey);
        usageKey = normalized(usageKey);
        helpCategory = normalized(helpCategory);
        featureId = normalized(featureId);
        cooldownId = normalized(cooldownId);
        guiDescriptorId = normalizedOptional(guiDescriptorId);
        hudDescriptorId = normalizedOptional(hudDescriptorId);
        hudNotApplicableReason = bounded(hudNotApplicableReason, 256);
        quotaId = normalizedOptional(quotaId);
        quotaNotApplicableReason = bounded(quotaNotApplicableReason, 256);
        convenienceRoots = copyNormalized(convenienceRoots);
        permissionIds = copyNormalized(permissionIds);
        sourceTypes = Set.copyOf(Objects.requireNonNull(sourceTypes, "sourceTypes"));
        Objects.requireNonNull(accessClass, "accessClass");
        Objects.requireNonNull(targetBehavior, "targetBehavior");
        Objects.requireNonNull(auditClass, "auditClass");
        Objects.requireNonNull(conflictPolicy, "conflictPolicy");

        if (!ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid command id " + id);
        }
        if (canonicalRoute.startsWith("/") || !ROUTE.matcher(canonicalRoute).matches()) {
            throw new IllegalArgumentException("Invalid canonical route " + canonicalRoute);
        }
        if (convenienceRoots.stream().anyMatch(root -> !ROOT.matcher(root).matches())) {
            throw new IllegalArgumentException("Invalid convenience root");
        }
        if (descriptionKey.isBlank() || usageKey.isBlank() || helpCategory.isBlank() || featureId.isBlank()) {
            throw new IllegalArgumentException("Command metadata is incomplete");
        }
        if (permissionIds.isEmpty()) {
            throw new IllegalArgumentException("Command permission policy is empty");
        }
        if (sourceTypes.isEmpty()) {
            throw new IllegalArgumentException("Command source policy is empty");
        }
        if (cooldownId.isBlank()) {
            throw new IllegalArgumentException("Command cooldown id is empty");
        }
        if (playerFacing && guiDescriptorId.isBlank()) {
            throw new IllegalArgumentException("Player facing command requires a GUI descriptor id");
        }
        if (hudDescriptorId.isBlank() == hudNotApplicableReason.isBlank()) {
            throw new IllegalArgumentException("Declare one HUD descriptor or one no HUD reason");
        }
        if (quotaId.isBlank() == quotaNotApplicableReason.isBlank()) {
            throw new IllegalArgumentException("Declare one quota id or one quota not applicable reason");
        }
        if (accessClass.isPrivileged() && auditClass == AuditService.AuditClass.NONE) {
            throw new IllegalArgumentException("Privileged command requires audit policy");
        }
        if (!pipelineEnforced) {
            throw new IllegalArgumentException("Catalog commands must use the shared pipeline");
        }
    }

    public String canonicalRoot() {
        int separator = canonicalRoute.indexOf(' ');
        return separator < 0 ? canonicalRoute : canonicalRoute.substring(0, separator);
    }

    private static Set<String> copyNormalized(Set<String> values) {
        Objects.requireNonNull(values, "values");
        return values.stream().map(CommandDefinition::normalized).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static String normalized(String value) {
        return Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizedOptional(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String bounded(String value, int maximumLength) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= maximumLength ? trimmed : trimmed.substring(0, maximumLength);
    }

    public enum AccessClass {
        PLAYER,
        TRUSTED_PLAYER,
        STAFF,
        ADMINISTRATOR,
        OWNER,
        CONSOLE_ONLY;

        public boolean isPrivileged() {
            return ordinal() >= STAFF.ordinal();
        }
    }

    public enum SourceType {
        PLAYER,
        CONSOLE,
        RCON,
        COMMAND_BLOCK,
        FUNCTION,
        SCHEDULED_TASK,
        SUDO,
        BUNDLE,
        PANEL,
        EXTERNAL_ADAPTER,
        SERVER_PROFILE,
        RUN_SERVER_WRAPPER,
        SILENT_ACTOR_WRAPPER,
        SILENT_SERVER_WRAPPER,
        ANNOUNCEMENT,
        GUI,
        INTEGRATION
    }

    public enum TargetBehavior {
        NONE,
        SELF,
        OPTIONAL_PLAYER,
        REQUIRED_PLAYER,
        BOUNDED_PLAYERS,
        SERVER
    }

    public enum ConflictPolicy {
        FAIL,
        PREFER_SEF,
        PREFER_EXISTING,
        CANONICAL_ONLY,
        RESTART_REQUIRED
    }
}
