package com.enviouse.sef.recovery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ItemStackSnapshotCodec {
    public static final int HARD_MAXIMUM_SLOTS = 256;
    public static final int HARD_MAXIMUM_ENCODED_CHARACTERS = 1_048_576;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private ItemStackSnapshotCodec() {
    }

    public static List<SlotStack> capture(
            Container container,
            HolderLookup.Provider registries
    ) {
        Objects.requireNonNull(container, "container");
        Objects.requireNonNull(registries, "registries");
        if (container.getContainerSize() < 0
                || container.getContainerSize() > HARD_MAXIMUM_SLOTS) {
            throw new IllegalArgumentException("inventory slot count is outside bounds");
        }
        List<SlotStack> result = new ArrayList<>();
        int encodedCharacters = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            JsonElement encoded = ItemStack.CODEC.encodeStart(
                    registries.createSerializationContext(JsonOps.INSTANCE),
                    stack.copy()).getOrThrow();
            encodedCharacters = Math.addExact(encodedCharacters, GSON.toJson(encoded).length());
            if (encodedCharacters > HARD_MAXIMUM_ENCODED_CHARACTERS) {
                throw new IllegalArgumentException("inventory snapshot is too large");
            }
            result.add(new SlotStack(slot, encoded));
        }
        return List.copyOf(result);
    }

    public static List<SlotStack> captureStacks(
            List<ItemStack> stacks,
            HolderLookup.Provider registries
    ) {
        Objects.requireNonNull(stacks, "stacks");
        Objects.requireNonNull(registries, "registries");
        if (stacks.size() > HARD_MAXIMUM_SLOTS) {
            throw new IllegalArgumentException("item stack count is outside bounds");
        }
        List<SlotStack> result = new ArrayList<>();
        int encodedCharacters = 0;
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack stack = Objects.requireNonNull(stacks.get(slot), "stack");
            if (stack.isEmpty()) {
                continue;
            }
            JsonElement encoded = ItemStack.CODEC.encodeStart(
                    registries.createSerializationContext(JsonOps.INSTANCE),
                    stack.copy()).getOrThrow();
            encodedCharacters = Math.addExact(encodedCharacters, GSON.toJson(encoded).length());
            if (encodedCharacters > HARD_MAXIMUM_ENCODED_CHARACTERS) {
                throw new IllegalArgumentException("item stack snapshot is too large");
            }
            result.add(new SlotStack(slot, encoded));
        }
        return List.copyOf(result);
    }

    public static List<ItemStack> decodeStacks(
            List<SlotStack> encoded,
            HolderLookup.Provider registries
    ) {
        Objects.requireNonNull(encoded, "encoded");
        int size = encoded.stream().mapToInt(SlotStack::slot).max().orElse(-1) + 1;
        List<ItemStack> result = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            result.add(ItemStack.EMPTY);
        }
        for (DecodedSlot decoded : decode(encoded, registries, size)) {
            result.set(decoded.slot(), decoded.stack().copy());
        }
        return result.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
    }

    public static List<DecodedSlot> decode(
            List<SlotStack> encoded,
            HolderLookup.Provider registries,
            int containerSize
    ) {
        Objects.requireNonNull(encoded, "encoded");
        Objects.requireNonNull(registries, "registries");
        if (containerSize < 0
                || containerSize > HARD_MAXIMUM_SLOTS
                || encoded.size() > HARD_MAXIMUM_SLOTS) {
            throw new IllegalArgumentException("inventory snapshot slot count is outside bounds");
        }
        int encodedCharacters = 0;
        List<DecodedSlot> result = new ArrayList<>();
        boolean[] occupied = new boolean[containerSize];
        for (SlotStack entry : encoded) {
            if (entry.slot() < 0
                    || entry.slot() >= containerSize
                    || occupied[entry.slot()]) {
                throw new IllegalArgumentException("inventory snapshot slot is invalid");
            }
            encodedCharacters = Math.addExact(
                    encodedCharacters,
                    GSON.toJson(entry.stack()).length());
            if (encodedCharacters > HARD_MAXIMUM_ENCODED_CHARACTERS) {
                throw new IllegalArgumentException("inventory snapshot is too large");
            }
            ItemStack stack = ItemStack.CODEC.parse(
                    registries.createSerializationContext(JsonOps.INSTANCE),
                    entry.stack()).getOrThrow();
            if (stack.isEmpty()) {
                throw new IllegalArgumentException("inventory snapshot contains an empty stack");
            }
            occupied[entry.slot()] = true;
            result.add(new DecodedSlot(entry.slot(), stack));
        }
        result.sort(Comparator.comparingInt(DecodedSlot::slot));
        return List.copyOf(result);
    }

    public static void apply(Container container, List<DecodedSlot> decoded) {
        Objects.requireNonNull(container, "container");
        Objects.requireNonNull(decoded, "decoded");
        container.clearContent();
        for (DecodedSlot entry : decoded) {
            container.setItem(entry.slot(), entry.stack().copy());
        }
        container.setChanged();
    }

    public record SlotStack(int slot, JsonElement stack) {
        public SlotStack {
            if (slot < 0 || slot >= HARD_MAXIMUM_SLOTS) {
                throw new IllegalArgumentException("inventory snapshot slot is outside bounds");
            }
            stack = Objects.requireNonNull(stack, "stack").deepCopy();
        }
    }

    public record DecodedSlot(int slot, ItemStack stack) {
        public DecodedSlot {
            if (slot < 0 || slot >= HARD_MAXIMUM_SLOTS) {
                throw new IllegalArgumentException("inventory snapshot slot is outside bounds");
            }
            stack = Objects.requireNonNull(stack, "stack").copy();
        }
    }
}
