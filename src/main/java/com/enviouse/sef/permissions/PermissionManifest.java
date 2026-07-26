package com.enviouse.sef.permissions;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Path;
import com.enviouse.sef.storage.StorageService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class PermissionManifest {
    public record Definition(
            String id,
            boolean defaultValue,
            String name,
            String description,
            PermissionNode<Boolean> node
    ) {
    }

    private static final PermissionDefinitionRegistry<PermissionNode<Boolean>> DEFINITIONS =
            new PermissionDefinitionRegistry<>();

    private PermissionManifest() {
    }

    public static synchronized PermissionNode<Boolean> register(
            String id,
            boolean defaultValue,
            String name,
            String description
    ) {
        String qualifiedId = ServerEssentialsForge.MODID + "." + id;
        return DEFINITIONS.register(qualifiedId, defaultValue, name, description, () -> {
            PermissionNode<Boolean> node = new PermissionNode<>(
                    ServerEssentialsForge.MODID,
                    id,
                    PermissionTypes.BOOLEAN,
                    (player, uuid, context) -> defaultValue);
            node.setInformation(Component.literal(name), TextFormatter.stringToFormattedText(description));
            return node;
        });
    }

    public static synchronized List<Definition> definitions() {
        return DEFINITIONS.definitions().stream()
                .map(definition -> new Definition(
                        definition.id(),
                        definition.defaultValue(),
                        definition.name(),
                        definition.description(),
                        definition.value()))
                .toList();
    }

    public static synchronized Map<String, Boolean> defaults() {
        return DEFINITIONS.defaults();
    }

    public static synchronized void writeRuntimeManifest(Path path) throws IOException {
        JsonObject data = new JsonObject();
        JsonArray permissions = new JsonArray();
        for (Definition definition : definitions()) {
            JsonObject permission = new JsonObject();
            permission.addProperty("id", definition.id());
            permission.addProperty("default", definition.defaultValue());
            permission.addProperty("name", definition.name());
            permission.addProperty("description", definition.description());
            permissions.add(permission);
        }
        data.add("permissions", permissions);
        StorageService.write(path, "permission manifest", 1, data, null);
    }
}
