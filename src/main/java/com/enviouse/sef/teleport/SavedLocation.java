package com.enviouse.sef.teleport;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public record SavedLocation(
        String dimensionId,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {
    public SavedLocation {
        dimensionId = Objects.requireNonNull(dimensionId, "dimensionId").trim();
        if (ResourceLocation.tryParse(dimensionId) == null) {
            throw new IllegalArgumentException("Invalid dimension identifier");
        }
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || !Float.isFinite(yaw) || !Float.isFinite(pitch)) {
            throw new IllegalArgumentException("Location contains a non finite value");
        }
    }

    public static SavedLocation from(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return new SavedLocation(
                player.serverLevel().dimension().location().toString(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot());
    }

    public BlockPos blockPosition() {
        return BlockPos.containing(x, y, z);
    }

    public JsonObject encode() {
        JsonObject object = new JsonObject();
        object.addProperty("dimension", dimensionId);
        object.addProperty("x", x);
        object.addProperty("y", y);
        object.addProperty("z", z);
        object.addProperty("yaw", yaw);
        object.addProperty("pitch", pitch);
        return object;
    }

    public static SavedLocation decode(JsonObject object) {
        Objects.requireNonNull(object, "object");
        return new SavedLocation(
                requiredString(object, "dimension"),
                requiredDouble(object, "x"),
                requiredDouble(object, "y"),
                requiredDouble(object, "z"),
                requiredFloat(object, "yaw"),
                requiredFloat(object, "pitch"));
    }

    private static String requiredString(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
            throw new IllegalStateException("Location field is missing, " + key);
        }
        return object.get(key).getAsString();
    }

    private static double requiredDouble(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
            throw new IllegalStateException("Location field is missing, " + key);
        }
        return object.get(key).getAsDouble();
    }

    private static float requiredFloat(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
            throw new IllegalStateException("Location field is missing, " + key);
        }
        return object.get(key).getAsFloat();
    }
}
