package com.enviouse.sef.docs;

import com.enviouse.sef.events.CommandRegistrationHandler;
import com.enviouse.sef.kernel.KernelServices;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CommandRegistrationHandlerInventoryTest {
    @Test
    void liveRegistrationHasCatalogRootsAndNoOrphanCatalogRoutes() throws Exception {
        KernelServices.initialize();
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        RegisterCommandsEvent event = new RegisterCommandsEvent(
                dispatcher,
                Commands.CommandSelection.DEDICATED,
                mock(CommandBuildContext.class));

        CommandRegistrationHandler handler = new CommandRegistrationHandler();
        handler.registerCommands(event);
        handler.registerLowPriorityCommands(event);

        Set<String> roots = dispatcher.getRoot().getChildren().stream()
                .map(node -> node.getName().toLowerCase(java.util.Locale.ROOT))
                .collect(Collectors.toSet());
        assertFalse(roots.isEmpty());

        Set<String> activeShortcutRoots = KernelServices.shortcuts().diagnostics().stream()
                .filter(diagnostic -> diagnostic.status() == com.enviouse.sef.kernel.command.ShortcutRegistry.Status.ACTIVE
                        || diagnostic.status()
                        == com.enviouse.sef.kernel.command.ShortcutRegistry.Status.ACTIVE_OVERRIDE)
                .map(diagnostic -> diagnostic.root().toLowerCase(java.util.Locale.ROOT))
                .collect(Collectors.toSet());
        assertTrue(activeShortcutRoots.stream().allMatch(roots::contains),
                () -> "active shortcut roots missing from live dispatcher " + activeShortcutRoots.stream()
                        .filter(root -> !roots.contains(root))
                        .toList());
        Set<String> disabledShortcutRoots = KernelServices.shortcuts().diagnostics().stream()
                .filter(diagnostic -> diagnostic.status() == com.enviouse.sef.kernel.command.ShortcutRegistry.Status.DISABLED)
                .map(diagnostic -> diagnostic.root().toLowerCase(java.util.Locale.ROOT))
                .collect(Collectors.toSet());
        assertTrue(disabledShortcutRoots.stream().noneMatch(roots::contains),
                () -> "disabled shortcut roots leaked into live dispatcher " + disabledShortcutRoots.stream()
                        .filter(roots::contains)
                        .toList());
        assertTrue(KernelServices.shortcuts().diagnostics().stream()
                .allMatch(diagnostic -> KernelServices.catalog().find(diagnostic.actionId()).isPresent()));

        JsonObject inventory = CommandInventoryGenerator.generateLive(dispatcher);
        assertEquals(roots.size(), inventory.get("dispatcherRootCount").getAsInt());
        assertEquals(roots.size(), inventory.getAsJsonArray("dispatcherRoots").size());
        String evidenceRoot = System.getProperty("sef.audit.evidenceRoot", "").trim();
        if (!evidenceRoot.isEmpty()) {
            CommandInventoryGenerator.writeLive(
                    java.nio.file.Path.of(evidenceRoot),
                    "command-inventory-live.json",
                    dispatcher);
        }
    }
}
