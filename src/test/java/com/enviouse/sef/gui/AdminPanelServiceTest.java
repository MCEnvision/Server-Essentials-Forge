package com.enviouse.sef.gui;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.enviouse.sef.kernel.command.PanelContracts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminPanelServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void draftPublicationConflictHistoryRollbackAndPersistenceAreRevisionSafe() throws Exception {
        KernelServices.initialize();
        AdminPanelService service = new AdminPanelService(
                KernelServices.catalog(),
                KernelServices.capabilities());
        service.load(temporaryDirectory);
        UUID actor = UUID.randomUUID();

        AdminPanelService.PanelDefinition draft =
                service.createDraft("operations", "Operations", actor).value();
        CommandDefinition action = KernelServices.catalog().find("sef:core.info").orElseThrow();
        String permission = action.permissionIds().stream().sorted().findFirst().orElseThrow();
        AdminPanelService.Control control = new AdminPanelService.Control(
                "info",
                0,
                1,
                action.id(),
                permission,
                PanelContracts.TargetPolicy.NONE,
                PanelContracts.ExecutionContext.ACTOR,
                "",
                PanelContracts.AudienceKind.SELF,
                1,
                Map.of(),
                false);

        ActionResult<AdminPanelService.PanelDefinition> added =
                service.addControl(draft.id(), draft.revision(), control, actor);
        assertTrue(added.successful());
        assertEquals(
                ActionResult.ReasonCode.CONFLICT,
                service.addControl(draft.id(), draft.revision(), control, actor).reason());

        AdminPanelService.PanelDefinition first =
                service.publish(draft.id(), added.value().revision(), actor).value();
        assertEquals(AdminPanelService.State.PUBLISHED, first.state());
        assertTrue(service.execution(first.id(), "info").isPresent());

        AdminPanelService.PanelDefinition secondDraft =
                service.saveDraft(first, first.revision(), actor).value();
        AdminPanelService.PanelDefinition second =
                service.publish(first.id(), secondDraft.revision(), actor).value();
        assertEquals(2, service.history(first.id()).size());
        AdminPanelService.PanelDefinition rolledBack =
                service.rollback(first.id(), second.revision(), first.revision(), actor).value();
        assertTrue(rolledBack.revision() > second.revision());
        assertEquals(first.controls(), rolledBack.controls());
        assertEquals(
                ActionResult.ReasonCode.CONFLICT,
                service.rollback(first.id(), second.revision(), first.revision(), actor).reason());

        service.flush();
        AdminPanelService reloaded = new AdminPanelService(
                KernelServices.catalog(),
                KernelServices.capabilities());
        assertEquals(
                com.enviouse.sef.storage.repository.StorageRepository.RepositoryState.READY,
                reloaded.load(temporaryDirectory).state());
        assertEquals(rolledBack.revision(), reloaded.panel(first.id()).orElseThrow().revision());
        assertEquals(3, reloaded.history(first.id()).size());
        assertFalse(reloaded.dirty());
    }

    @Test
    void forgedPermissionAndUnknownActionFailValidationBeforePublication() {
        KernelServices.initialize();
        AdminPanelService service = new AdminPanelService(
                KernelServices.catalog(),
                KernelServices.capabilities());
        UUID actor = UUID.randomUUID();
        AdminPanelService.PanelDefinition draft =
                service.createDraft("unsafe", "Unsafe", actor).value();
        AdminPanelService.Control unknown = new AdminPanelService.Control(
                "unknown",
                0,
                1,
                "sef:missing",
                "sef.kernel.panel.use",
                PanelContracts.TargetPolicy.NONE,
                PanelContracts.ExecutionContext.ACTOR,
                "",
                PanelContracts.AudienceKind.SELF,
                1,
                Map.of(),
                true);
        ActionResult<AdminPanelService.PanelDefinition> result =
                service.addControl(draft.id(), draft.revision(), unknown, actor);
        assertEquals(ActionResult.ReasonCode.INVALID_DEFINITION, result.reason());
        assertTrue(service.panel("unsafe").isEmpty());
    }
}
