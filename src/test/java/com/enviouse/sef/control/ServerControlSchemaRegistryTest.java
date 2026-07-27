package com.enviouse.sef.control;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerControlSchemaRegistryTest {
    @Test
    void everyCatalogFeatureHasDedicatedTypedWorkflow() {
        assertEquals(ServerControlCatalog.FEATURES.size(), ServerControlSchemaRegistry.schemas().size());
        var workflows = new HashSet<String>();
        for (ServerControlCatalog.FeatureDefinition feature : ServerControlCatalog.FEATURES) {
            var schema = ServerControlSchemaRegistry.require(feature.id());
            assertEquals(feature.id(), schema.featureId());
            assertTrue(workflows.add(schema.workflowId()), feature.id());
            assertFalse(schema.fields().isEmpty(), feature.id());
            assertFalse(schema.operations().isEmpty(), feature.id());
            assertFalse(schema.workflowId().contains("generic"), feature.id());
        }
    }

    @Test
    void typedFieldsRejectUnsafeOrOutOfRangeValues() {
        assertEquals(
                "locked",
                ServerControlSchemaRegistry.validate("chat_control", "mode", "LOCKED"));
        assertEquals(
                "1000",
                ServerControlSchemaRegistry.validate("world_border", "size", "1000.0"));
        assertThrows(
                IllegalArgumentException.class,
                () -> ServerControlSchemaRegistry.validate("admission", "maximum_players", "-1"));
        assertThrows(
                IllegalArgumentException.class,
                () -> ServerControlSchemaRegistry.validate(
                        "resource_packs",
                        "url",
                        "http://example.invalid/pack.zip"));
        assertThrows(
                IllegalArgumentException.class,
                () -> ServerControlSchemaRegistry.validate("maintenance", "message", "x\nop @a"));
    }
}
