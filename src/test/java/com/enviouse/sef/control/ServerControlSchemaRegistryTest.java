package com.enviouse.sef.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerControlSchemaRegistryTest {
    @Test
    void nativeAdmissionQueueModeAndWaitSettingsAreValidated() {
        assertEquals("native_wait", ServerControlSchemaRegistry.validate("queue", "mode", "native_wait"));
        assertEquals("600", ServerControlSchemaRegistry.validate("queue", "maximum_wait_seconds", "600"));
        assertEquals(
                "The server is full. Waiting for a slot.",
                ServerControlSchemaRegistry.validate(
                        "queue",
                        "status_message",
                        "The server is full. Waiting for a slot."));
    }

    @Test
    void admissionQueueRejectsInvalidModeAndUnsafeWaitBounds() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ServerControlSchemaRegistry.validate("queue", "mode", "unknown"));
        assertThrows(
                IllegalArgumentException.class,
                () -> ServerControlSchemaRegistry.validate("queue", "maximum_wait_seconds", "9"));
        assertThrows(
                IllegalArgumentException.class,
                () -> ServerControlSchemaRegistry.validate("queue", "maximum_wait_seconds", "86401"));
    }
}
