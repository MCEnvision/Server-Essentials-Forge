package com.enviouse.sef.gui.protocol;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

final class PayloadCodecSupport {
    private PayloadCodecSupport() {
    }

    static String readString(FriendlyByteBuf buffer, int maximum) {
        String value = buffer.readUtf(maximum);
        validateText(value, maximum);
        return value;
    }

    static void writeString(FriendlyByteBuf buffer, String value, int maximum) {
        String safe = Objects.requireNonNull(value, "value");
        validateText(safe, maximum);
        buffer.writeUtf(safe, maximum);
    }

    static byte[] readBytes(FriendlyByteBuf buffer, int maximum) {
        return buffer.readByteArray(maximum);
    }

    static void writeBytes(FriendlyByteBuf buffer, byte[] value, int maximum) {
        byte[] safe = Objects.requireNonNull(value, "value");
        if (safe.length > maximum) {
            throw new IllegalArgumentException("Payload byte array exceeds its bound");
        }
        buffer.writeByteArray(safe);
    }

    static <B extends FriendlyByteBuf, T> List<T> readList(
            B buffer,
            int maximum,
            Function<B, T> decoder
    ) {
        int size = buffer.readVarInt();
        if (size < 0 || size > maximum) {
            throw new IllegalArgumentException("Payload collection exceeds its bound");
        }
        List<T> result = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            result.add(Objects.requireNonNull(decoder.apply(buffer), "decoded value"));
        }
        return List.copyOf(result);
    }

    static <B extends FriendlyByteBuf, T> void writeList(
            B buffer,
            List<T> values,
            int maximum,
            BiConsumer<B, T> encoder
    ) {
        List<T> safe = List.copyOf(Objects.requireNonNull(values, "values"));
        if (safe.size() > maximum) {
            throw new IllegalArgumentException("Payload collection exceeds its bound");
        }
        buffer.writeVarInt(safe.size());
        for (T value : safe) {
            encoder.accept(buffer, Objects.requireNonNull(value, "value"));
        }
    }

    static void validateText(String value, int maximum) {
        String safe = Objects.requireNonNull(value, "value");
        if (safe.length() > maximum || safe.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Payload text is invalid");
        }
    }
}
