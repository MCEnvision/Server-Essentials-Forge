package com.enviouse.sef.storage;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.Instant;

public final class InstantJsonAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
    @Override
    public JsonElement serialize(Instant source, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(source.toString());
    }

    @Override
    public Instant deserialize(JsonElement source, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            return Instant.parse(source.getAsString());
        } catch (RuntimeException exception) {
            throw new JsonParseException("Invalid instant", exception);
        }
    }
}
