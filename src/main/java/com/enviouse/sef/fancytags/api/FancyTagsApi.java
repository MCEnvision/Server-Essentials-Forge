package com.enviouse.sef.fancytags.api;

import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.fancytags.FancyTagGroupResolver;
import com.enviouse.sef.fancytags.FancyTagService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.vanish.VanishUtil;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public final class FancyTagsApi {
    private FancyTagsApi() {
    }

    public static Read read() {
        return Read.INSTANCE;
    }

    public static Administration administration() {
        return Administration.INSTANCE;
    }

    public static final class Read {
        private static final Read INSTANCE = new Read();

        private Read() {
        }

        public List<FancyTagService.TagRecord> publishedTags() {
            return KernelServices.fancyTags().tags().stream()
                    .filter(value -> value.status() == FancyTagService.TagStatus.PUBLISHED)
                    .toList();
        }

        public List<FancyTagService.CategoryRecord> categories() {
            return KernelServices.fancyTags().categories();
        }

        public List<FancyTagService.PaletteRecord> palettes() {
            return KernelServices.fancyTags().palettes();
        }

        public List<FancyTagService.TemplateRecord> templates() {
            return KernelServices.fancyTags().templates();
        }

        public List<FancyTagService.AssignmentRecord> assignmentsFor(UUID subjectId) {
            Objects.requireNonNull(subjectId, "subjectId");
            String target = subjectId.toString();
            return KernelServices.fancyTags().assignments().stream()
                    .filter(value -> value.targetType() == FancyTagService.TargetType.PLAYER)
                    .filter(value -> value.targetId().equals(target))
                    .toList();
        }

        public java.util.Optional<FancyTagService.TagRecord> publishedTag(String reference) {
            return KernelServices.fancyTags().find(reference)
                    .filter(value -> value.status() == FancyTagService.TagStatus.PUBLISHED);
        }

        public List<FancyTagService.ResolvedTag> resolve(
                UUID viewerId,
                UUID subjectId,
                Set<String> groups,
                String team,
                boolean vanished,
                FancyTagService.RenderContext context,
                Predicate<String> visibilityPermission
        ) {
            return KernelServices.fancyTags().resolve(
                    new FancyTagService.ViewerContext(viewerId, subjectId, groups, team, vanished),
                    context,
                    visibilityPermission);
        }
    }

    public static final class Administration {
        private static final Administration INSTANCE = new Administration();

        private Administration() {
        }

        public ActionResult<FancyTagService.TagRecord> createDraft(
                ServerPlayer actor,
                String resourceKey,
                String displayName
        ) {
            if (!has(actor, "commands.tags.create")) {
                return denied();
            }
            return KernelServices.fancyTags().createDraft(
                    resourceKey,
                    displayName,
                    actor.getUUID());
        }

        public ActionResult<FancyTagService.ArtworkRevision> importArtwork(
                ServerPlayer actor,
                String tagReference,
                byte[] encoded,
                long expectedRecordRevision
        ) {
            if (!has(actor, "commands.tags.import.client") || !has(actor, "commands.tags.edit")) {
                return denied();
            }
            return KernelServices.fancyTags().importArtwork(
                    tagReference,
                    encoded,
                    actor.getUUID(),
                    expectedRecordRevision);
        }

        public ActionResult<FancyTagService.TagRecord> changeStatus(
                ServerPlayer actor,
                String tagReference,
                FancyTagService.TagStatus status,
                long expectedRecordRevision
        ) {
            Objects.requireNonNull(status, "status");
            String permission = switch (status) {
                case PUBLISHED -> "commands.tags.publish";
                case HIDDEN -> "commands.tags.hide";
                case ARCHIVED, SUSPENDED -> "commands.tags.archive";
                case DRAFT -> "commands.tags.restore";
                case PENDING_DELETE, CORRUPT -> "commands.tags.delete";
            };
            if (!has(actor, permission)) {
                return denied();
            }
            return KernelServices.fancyTags().changeStatus(
                    tagReference,
                    status,
                    actor.getUUID(),
                    expectedRecordRevision);
        }

        public ActionResult<FancyTagService.AssignmentRecord> assignPlayer(
                ServerPlayer actor,
                UUID targetId,
                String tagReference,
                FancyTagService.TagSlot slot,
                int priority,
                Instant expiresAt
        ) {
            if (!has(actor, "commands.tags.assign.player")) {
                return denied();
            }
            Objects.requireNonNull(targetId, "targetId");
            ActionResult<Void> authorization = authorizePlayer(actor, targetId, slot);
            if (!authorization.successful()) {
                return ActionResult.failure(authorization.reason(), authorization.detail());
            }
            return assign(
                    actor,
                    FancyTagService.TargetType.PLAYER,
                    targetId.toString(),
                    tagReference,
                    slot,
                    priority,
                    expiresAt);
        }

        public ActionResult<FancyTagService.AssignmentRecord> assignGroup(
                ServerPlayer actor,
                String group,
                String tagReference,
                FancyTagService.TagSlot slot,
                int priority,
                Instant expiresAt
        ) {
            if (!has(actor, "commands.tags.assign.group")) {
                return denied();
            }
            if (!FancyTagGroupResolver.health().healthy()) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.PROVIDER_ERROR,
                        "tag group provider is unavailable");
            }
            return assign(actor, FancyTagService.TargetType.GROUP, group, tagReference, slot, priority, expiresAt);
        }

        public ActionResult<FancyTagService.AssignmentRecord> assignTeam(
                ServerPlayer actor,
                String team,
                String tagReference,
                FancyTagService.TagSlot slot,
                int priority,
                Instant expiresAt
        ) {
            if (!has(actor, "commands.tags.assign.team")) {
                return denied();
            }
            String normalized = normalizeTarget(team);
            if (actor.getServer().getScoreboard().getPlayerTeam(normalized) == null) {
                return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "scoreboard team not found");
            }
            return assign(actor, FancyTagService.TargetType.TEAM, normalized, tagReference, slot, priority, expiresAt);
        }

        public ActionResult<FancyTagService.AssignmentRecord> assignDefault(
                ServerPlayer actor,
                String tagReference,
                FancyTagService.TagSlot slot,
                int priority,
                Instant expiresAt
        ) {
            if (!has(actor, "commands.tags.assign.default")) {
                return denied();
            }
            return assign(actor, FancyTagService.TargetType.DEFAULT, "default", tagReference, slot, priority, expiresAt);
        }

        public ActionResult<Void> unassign(ServerPlayer actor, UUID assignmentId) {
            if (!has(actor, "commands.tags.unassign")) {
                return denied();
            }
            Objects.requireNonNull(assignmentId, "assignmentId");
            FancyTagService.AssignmentRecord assignment = KernelServices.fancyTags().assignments().stream()
                    .filter(value -> value.id().equals(assignmentId))
                    .findFirst()
                    .orElse(null);
            if (assignment == null) {
                return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "tag assignment not found");
            }
            if (assignment.targetType() == FancyTagService.TargetType.PLAYER) {
                ActionResult<Void> authorization = authorizePlayer(
                        actor,
                        UUID.fromString(assignment.targetId()),
                        assignment.slot());
                if (!authorization.successful()) {
                    return authorization;
                }
            }
            return KernelServices.fancyTags().unassign(assignmentId, actor.getUUID());
        }

        private static ActionResult<FancyTagService.AssignmentRecord> assign(
                ServerPlayer actor,
                FancyTagService.TargetType targetType,
                String target,
                String tagReference,
                FancyTagService.TagSlot slot,
                int priority,
                Instant expiresAt
        ) {
            Objects.requireNonNull(slot, "slot");
            String normalizedTarget = targetType == FancyTagService.TargetType.DEFAULT
                    ? "default"
                    : normalizeTarget(target);
            boolean occupied = KernelServices.fancyTags().assignments().stream()
                    .anyMatch(value -> value.enabled()
                            && value.targetType() == targetType
                            && value.targetId().equals(normalizedTarget)
                            && value.slot() == slot);
            if (occupied && !has(actor, "tags.assign.multiple")) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.POLICY_DENIED,
                        "tag assignment slot already has an active tag");
            }
            return KernelServices.fancyTags().assign(
                    tagReference,
                    targetType,
                    normalizedTarget,
                    slot,
                    priority,
                    expiresAt,
                    actor.getUUID());
        }

        private static ActionResult<Void> authorizePlayer(
                ServerPlayer actor,
                UUID targetId,
                FancyTagService.TagSlot slot
        ) {
            Objects.requireNonNull(actor, "actor");
            Objects.requireNonNull(slot, "slot");
            ServerPlayer target = actor.getServer().getPlayerList().getPlayer(targetId);
            if (target == null) {
                return has(actor, "commands.tags.assign.offline")
                        ? ActionResult.success(null)
                        : ActionResult.failure(
                                ActionResult.ReasonCode.NOT_FOUND,
                                "player target is not online");
            }
            var decision = PlayerTargetPolicy.decide(
                    actor.createCommandSourceStack(),
                    target,
                    PermissionsHandler.phasePermission("tags.assign.hierarchy.override"),
                    PermissionsHandler.phasePermission("tags.assign.exempt"),
                    PermissionsHandler.phasePermission("tags.assign.exemption.override"),
                    false,
                    true);
            if (!decision.allowed()) {
                return ActionResult.failure(decision.reason(), "player target is protected");
            }
            if (actor != target
                    && VanishUtil.isVanished(target, actor)
                    && !has(actor, "tags.assign.vanished")) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.TARGET_DENIED,
                        "vanished player target is unavailable");
            }
            return ActionResult.success(null);
        }

        private static String normalizeTarget(String value) {
            String normalized = Objects.requireNonNull(value, "value").trim().toLowerCase(java.util.Locale.ROOT);
            if (normalized.isBlank()
                    || normalized.length() > 128
                    || normalized.codePoints().anyMatch(Character::isISOControl)) {
                throw new IllegalArgumentException("invalid tag assignment target");
            }
            return normalized;
        }

        private static boolean has(ServerPlayer player, String id) {
            Objects.requireNonNull(player, "player");
            var node = PermissionsHandler.phasePermission(id);
            return node != null && PermissionService.has(player, node);
        }

        private static <T> ActionResult<T> denied() {
            return ActionResult.failure(ActionResult.ReasonCode.PERMISSION_DENIED, "permission denied");
        }
    }
}
