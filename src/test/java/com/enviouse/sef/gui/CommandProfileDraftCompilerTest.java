package com.enviouse.sef.gui;

import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.command.PanelContracts;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandProfileDraftCompilerTest {
    @Test
    void compilerAcceptsTypedCatalogActionAndRejectsUnknownOrUnboundedDrafts() {
        KernelServices.initialize();
        CommandProfileDraftCompiler compiler =
                new CommandProfileDraftCompiler(KernelServices.catalog(), 2);

        assertTrue(compiler.compile(new CommandProfileDraftCompiler.Draft(
                "info",
                "sef:core.info",
                PanelContracts.ExecutionContext.ACTOR,
                Map.of("page", "1"),
                Set.of())).accepted());
        assertFalse(compiler.compile(new CommandProfileDraftCompiler.Draft(
                "missing",
                "sef:missing",
                PanelContracts.ExecutionContext.ACTOR,
                Map.of(),
                Set.of())).accepted());
        assertFalse(compiler.compile(new CommandProfileDraftCompiler.Draft(
                "server",
                "sef:core.info",
                PanelContracts.ExecutionContext.SERVER_PROFILE,
                Map.of(),
                Set.of())).accepted());
        assertFalse(compiler.compile(new CommandProfileDraftCompiler.Draft(
                "large",
                "sef:core.info",
                PanelContracts.ExecutionContext.ACTOR,
                Map.of("one", "1", "two", "2", "three", "3"),
                Set.of())).accepted());
    }
}
