package com.enviouse.sef.commandlog;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandRedactionPolicyTest {
    @Test
    void networkAddressCommandsNeverExposeTheirArguments() {
        for (String root : List.of(
                "ban-ip", "banip", "tempban-ip", "tempbanip",
                "pardon-ip", "unban-ip", "unbanip",
                "kick-ip", "kickip")) {
            CommandRedactionPolicy.RedactedCommand redacted =
                    CommandRedactionPolicy.redact("/" + root + " 203.0.113.42 policy test");

            assertEquals(root, redacted.root());
            assertEquals(CommandRedactionPolicy.RedactionClass.SECRET, redacted.redactionClass());
            assertEquals("/" + root + " <network address redacted>", redacted.display());
            assertTrue(redacted.ruleIds().contains("network_address"));
            assertFalse(redacted.display().contains("203.0.113.42"));
            assertFalse(redacted.display().contains("policy test"));
        }
    }

    @Test
    void namespacedNetworkAddressCommandsRedactIpv6Arguments() {
        CommandRedactionPolicy.RedactedCommand redacted =
                CommandRedactionPolicy.redact("/sef:kick-ip 2001:db8::1 maintenance");

        assertEquals("kick-ip", redacted.root());
        assertEquals(CommandRedactionPolicy.RedactionClass.SECRET, redacted.redactionClass());
        assertFalse(redacted.display().contains("2001:db8::1"));
        assertFalse(redacted.display().contains("maintenance"));
    }

    @Test
    void wrapperCommandsNeverExposeNestedArguments() {
        for (String command : List.of(
                "/execute as @a run login hunter2",
                "/run data get entity Notch Inventory",
                "/silent minecraft:login hunter2",
                "/sudo Notch login hunter2",
                "/schedule function private:operator 1t")) {
            CommandRedactionPolicy.RedactedCommand redacted = CommandRedactionPolicy.redact(command);

            assertEquals(CommandRedactionPolicy.RedactionClass.PRIVATE_CONTENT, redacted.redactionClass());
            assertTrue(redacted.ruleIds().contains("nested_command"));
            assertFalse(redacted.display().contains("hunter2"));
            assertFalse(redacted.display().contains("Inventory"));
            assertFalse(redacted.display().contains("private:operator"));
        }
    }

    @Test
    void moderationReasonsAndDataArgumentsArePrivate() {
        CommandRedactionPolicy.RedactedCommand ban =
                CommandRedactionPolicy.redact("/minecraft:ban Notch private evidence");
        CommandRedactionPolicy.RedactedCommand data =
                CommandRedactionPolicy.redact("/data get entity Notch Inventory");

        assertEquals(CommandRedactionPolicy.RedactionClass.PRIVATE_CONTENT, ban.redactionClass());
        assertFalse(ban.display().contains("private evidence"));
        assertEquals(CommandRedactionPolicy.RedactionClass.SECRET, data.redactionClass());
        assertFalse(data.display().contains("Inventory"));
    }

    @Test
    void controlCharactersCannotHidePrivateCommandRoots() {
        for (String command : List.of(
                "/msg\nNotch private text",
                "/sef:helpop\rprivate report",
                "/adminchat\u0000private staff text",
                "/teammsg\u202Eprivate team text")) {
            CommandRedactionPolicy.RedactedCommand redacted = CommandRedactionPolicy.redact(command);

            assertEquals(CommandRedactionPolicy.RedactionClass.PRIVATE_CONTENT, redacted.redactionClass());
            assertFalse(redacted.display().contains("Notch"));
            assertFalse(redacted.display().contains("private text"));
            assertFalse(redacted.display().contains("private report"));
            assertFalse(redacted.display().contains("private staff text"));
            assertFalse(redacted.display().contains("private team text"));
        }
    }

    @Test
    void everyPrivateChatAliasRedactsItsMessage() {
        for (String root : List.of(
                "helpop", "ac", "adminchat", "staffchat", "pchat", "teammsg", "tm")) {
            CommandRedactionPolicy.RedactedCommand redacted =
                    CommandRedactionPolicy.redact("/" + root + " private text");

            assertEquals(root, redacted.root());
            assertEquals(CommandRedactionPolicy.RedactionClass.PRIVATE_CONTENT, redacted.redactionClass());
            assertEquals("/" + root + " <private>", redacted.display());
        }
    }
}
