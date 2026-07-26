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
}
