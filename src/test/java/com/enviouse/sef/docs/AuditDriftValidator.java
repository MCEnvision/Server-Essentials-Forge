package com.enviouse.sef.docs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Deterministic and mutation-oriented checks for the Phase 000 inventory gate.
 */
public final class AuditDriftValidator {
    private AuditDriftValidator() {
    }

    public static void requireDeterministic(JsonElement first, JsonElement second) {
        String left = normalized(first);
        String right = normalized(second);
        if (!left.equals(right)) {
            throw new IllegalStateException("inventory generation is not deterministic");
        }
    }

    public static void requireExactSemanticKeys(JsonArray expected, JsonArray actual) {
        Set<String> expectedKeys = semanticKeys(expected);
        Set<String> actualKeys = semanticKeys(actual);
        if (!expectedKeys.equals(actualKeys)) {
            Set<String> missing = new HashSet<>(expectedKeys);
            missing.removeAll(actualKeys);
            Set<String> added = new HashSet<>(actualKeys);
            added.removeAll(expectedKeys);
            throw new IllegalStateException("inventory identity drift missing=" + sorted(missing)
                    + " added=" + sorted(added));
        }
    }

    public static void requireTraceability(JsonArray rows) {
        AuditEvidenceContract.validateInventorySet(rows);
        for (JsonElement element : rows) {
            JsonObject row = element.getAsJsonObject();
            requireText(row, "canonicalOwner");
            requireText(row, "requirement");
            requireText(row, "laterPhase");
            requireText(row, "traceabilityId");
            for (JsonElement location : row.getAsJsonArray("sourceLocations")) {
                if (!location.getAsString().equals(location.getAsString().replace('\\', '/'))
                        || location.getAsString().startsWith("/")) {
                    throw new IllegalStateException("source location is not repository relative "
                            + row.get("rowId").getAsString());
                }
            }
        }
    }

    public static String normalized(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "null";
        }
        if (element.isJsonObject()) {
            Map<String, JsonElement> sorted = new TreeMap<>();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                sorted.put(entry.getKey(), entry.getValue());
            }
            return sorted.entrySet().stream()
                    .map(entry -> quote(entry.getKey()) + ":" + normalized(entry.getValue()))
                    .collect(Collectors.joining(",", "{", "}"));
        }
        if (element.isJsonArray()) {
            ArrayList<String> values = new ArrayList<>();
            for (JsonElement child : element.getAsJsonArray()) {
                values.add(normalized(child));
            }
            return values.stream().collect(Collectors.joining(",", "[", "]"));
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        return primitive.isString() ? quote(primitive.getAsString()) : primitive.toString();
    }

    private static Set<String> semanticKeys(JsonArray rows) {
        Set<String> result = new HashSet<>();
        for (JsonElement element : rows) {
            JsonObject row = element.getAsJsonObject();
            result.add(row.get("category").getAsString() + "\u0000" + row.get("semanticKey").getAsString());
        }
        return result;
    }

    private static String sorted(Set<String> values) {
        return values.stream().sorted(Comparator.naturalOrder()).toList().toString();
    }

    private static void requireText(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()
                || !object.getAsJsonPrimitive(key).isString()
                || object.get(key).getAsString().isBlank()) {
            throw new IllegalStateException("traceability field is missing " + key);
        }
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
