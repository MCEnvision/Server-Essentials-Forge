package com.enviouse.sef.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CommandRootPolicyTest {
    @Test
    void normalizesLeadingSlashAndNamespace() {
        CommandRootPolicy.Decision decision = CommandRootPolicy.evaluate(
                " /minecraft:msg Notch hello", "msg", "", 100);

        assertTrue(decision.allowed());
        assertEquals("minecraft:msg Notch hello", decision.command());
        assertEquals("msg", decision.root());
    }

    @Test
    void emptyAllowlistDeniesEverything() {
        assertFalse(CommandRootPolicy.evaluate("msg Notch hello", "", "", 100).allowed());
    }

    @Test
    void denylistWinsOverWildcardAllowlist() {
        CommandRootPolicy.Decision decision = CommandRootPolicy.evaluate("execute as @a run kill @s", "*", "execute", 100);

        assertFalse(decision.allowed());
        assertEquals("command root is denied", decision.reason());
    }

    @Test
    void rejectsControlsAndOversizedCommands() {
        assertFalse(CommandRootPolicy.evaluate("msg Notch hi\nop Notch", "*", "", 100).allowed());
        assertFalse(CommandRootPolicy.evaluate("op\tNotch", "*", "op", 100).allowed());
        assertFalse(CommandRootPolicy.evaluate("msg Notch hello", "*", "", 5).allowed());
    }

    @Test
    void strictStoredCommandModeRejectsSlashAndSelectors() {
        assertFalse(CommandRootPolicy.evaluate(
                "/say hello", "say", "", 100, false, false).allowed());
        assertFalse(CommandRootPolicy.evaluate(
                "say @a hello", "say", "", 100, false, false).allowed());
        assertTrue(CommandRootPolicy.evaluate(
                "say hello", "say", "", 100, false, false).allowed());
    }
}
