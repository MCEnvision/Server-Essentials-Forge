package com.enviouse.sef.automation;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.audit.SecurityAuditService;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.permissions.DelegatedPermissionScope;
import com.enviouse.sef.permissions.EphemeralExecutionGrant;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SudoDelegationAudit {
    private SudoDelegationAudit() {
    }

    public static boolean record(
            Stage stage,
            CommandSourceStack source,
            ServerPlayer target,
            UUID correlationId,
            EphemeralExecutionGrant grant,
            String root,
            String profileId,
            AuditService.Result result,
            ActionResult.ReasonCode reason,
            String detail
    ) {
        UUID actorId = source.getEntity() == null
                ? UUID.nameUUIDFromBytes(
                        ("sef:source:" + source.getTextName()).getBytes(StandardCharsets.UTF_8))
                : source.getEntity().getUUID();
        return AuditService.record(event(
                stage,
                actorId,
                source.getTextName(),
                source.getEntity() == null ? "server" : "player",
                target.getUUID(),
                correlationId,
                grant,
                root,
                profileId,
                result,
                reason,
                detail));
    }

    static AuditService.Event event(
            Stage stage,
            UUID actorId,
            String actorName,
            String sourceType,
            UUID targetId,
            UUID correlationId,
            EphemeralExecutionGrant grant,
            String root,
            String profileId,
            AuditService.Result result,
            ActionResult.ReasonCode reason,
            String detail
    ) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("stage", stage.id());
        parameters.put("effective_actor", targetId.toString());
        parameters.put("root", root == null ? "" : root);
        parameters.put("profile", profileId == null ? "" : profileId);
        parameters.put("detail", detail == null ? "" : detail);
        if (grant != null) {
            parameters.put("grant_id", grant.grantId().toString());
            parameters.put("confirmation_id", grant.confirmationId().toString());
            parameters.put("command_digest", grant.commandDigest());
            parameters.put("maximum_invocations", Integer.toString(grant.maximumInvocations()));
            parameters.put("used", Boolean.toString(grant.used()));
            parameters.put("scope_active", Boolean.toString(DelegatedPermissionScope.active()));
        }
        Map<String, String> providerContext = grant == null
                ? Map.of()
                : Map.of(
                        "target_session_revision", Long.toString(grant.targetSessionRevision()),
                        "command_tree_revision", Long.toString(grant.commandTreeRevision()),
                        "permission_provider_revision", Long.toString(grant.permissionProviderRevision()),
                        "feature_revision", Long.toString(grant.featureRevision()),
                        "configuration_revision", Long.toString(grant.configurationRevision()));
        return new AuditService.Event(
                1,
                UUID.randomUUID(),
                Instant.now(),
                SecurityAuditService.currentSessionId(),
                actorId,
                actorName,
                sourceType,
                "sef:sudo.delegation." + stage.id(),
                List.of(targetId),
                parameters,
                result,
                reason,
                0L,
                "sudo",
                null,
                correlationId,
                grant == null ? 0L : grant.profileRevision(),
                grant == null ? 0L : grant.sudoPolicyRevision(),
                providerContext,
                AuditService.RedactionClass.SECRET_ARGUMENTS,
                List.of("sudo_command_digest"),
                null,
                "",
                AuditService.AuditClass.DELEGATED_EXECUTION);
    }

    public enum Stage {
        ADMISSION("admission"),
        CONFIRMATION("confirmation"),
        DISPATCH("dispatch"),
        RESULT("result"),
        CLEANUP("cleanup");

        private final String id;

        Stage(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }
}
