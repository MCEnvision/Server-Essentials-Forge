package com.enviouse.sef.permissions;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PermissionDefinitionRegistryTest {
    @Test
    void preservesStableInsertionOrderAndDefaults() {
        PermissionDefinitionRegistry<String> registry = new PermissionDefinitionRegistry<>();
        registry.register("sef.public", true, "public", "public", () -> "one");
        registry.register("sef.admin", false, "admin", "admin", () -> "two");

        assertEquals(List.of("sef.public", "sef.admin"),
                registry.definitions().stream()
                        .map(PermissionDefinitionRegistry.Definition::id)
                        .toList());
        assertEquals(Boolean.TRUE, registry.defaults().get("sef.public"));
        assertEquals(Boolean.FALSE, registry.defaults().get("sef.admin"));
    }

    @Test
    void rejectsDuplicateNodeIds() {
        PermissionDefinitionRegistry<String> registry = new PermissionDefinitionRegistry<>();
        registry.register("sef.commands.nick", true, "nick", "nick", () -> "one");

        assertThrows(IllegalStateException.class, () -> registry.register(
                "sef.commands.nick",
                false,
                "duplicate",
                "duplicate",
                () -> "two"));
    }
}
