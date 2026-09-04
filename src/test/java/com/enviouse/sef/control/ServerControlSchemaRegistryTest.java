package com.enviouse.sef.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void everyPublishedFieldRejectsControlCharactersAndOversizedValues() {
        int fieldCount = 0;
        for (ServerControlSchemaRegistry.FeatureSchema schema : ServerControlSchemaRegistry.schemas()) {
            for (ServerControlSchemaRegistry.FieldDefinition field : schema.fields()) {
                fieldCount++;
                assertThrows(
                        IllegalArgumentException.class,
                        () -> field.validate("safe\u0000value"),
                        schema.featureId() + "." + field.id());
                assertThrows(
                        IllegalArgumentException.class,
                        () -> field.validate("x".repeat(ServerControlSchemaRegistry.MAXIMUM_VALUE_LENGTH + 1)),
                        schema.featureId() + "." + field.id());
            }
        }
        assertTrue(fieldCount >= 100);
    }

    @Test
    void eachFieldTypeRejectsMalformedSemanticValues() {
        for (ServerControlSchemaRegistry.FeatureSchema schema : ServerControlSchemaRegistry.schemas()) {
            for (ServerControlSchemaRegistry.FieldDefinition field : schema.fields()) {
                String invalid = switch (field.type()) {
                    case TEXT -> "x".repeat((int) field.maximum() + 1);
                    case INTEGER, DURATION_SECONDS -> "not-a-number";
                    case DECIMAL -> "1.23456";
                    case BOOLEAN -> "maybe";
                    case ENUM -> "__unknown__";
                    case INSTANT -> "not-an-instant";
                    case UUID -> "not-a-uuid";
                    case RESOURCE_LOCATION -> "not a resource";
                    case HTTPS_URL -> "http://example.test/path";
                    case HASH -> "not-a-hash";
                    case LIST -> ",";
                };
                assertThrows(
                        IllegalArgumentException.class,
                        () -> field.validate(invalid),
                        schema.featureId() + "." + field.id());
            }
        }
    }
}
