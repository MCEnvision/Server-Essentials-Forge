package com.enviouse.sef.config;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class RuntimeConfigValue<T> implements Supplier<T> {
    private final List<String> legacyPath;
    private final String description;
    private final T defaultValue;
    private final Predicate<Object> validator;
    private final Number minimum;
    private final Number maximum;
    private volatile T value;

    RuntimeConfigValue(
            List<String> legacyPath,
            String description,
            T defaultValue,
            Predicate<Object> validator,
            Number minimum,
            Number maximum
    ) {
        this.legacyPath = List.copyOf(legacyPath);
        this.description = Objects.requireNonNullElse(description, "").strip();
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.minimum = minimum;
        this.maximum = maximum;
        value = defaultValue;
    }

    @Override
    public T get() {
        return value;
    }

    public T getDefault() {
        return defaultValue;
    }

    public List<String> legacyPath() {
        return legacyPath;
    }

    public String description() {
        return description;
    }

    public Number minimum() {
        return minimum;
    }

    public Number maximum() {
        return maximum;
    }

    public Kind kind() {
        if (defaultValue instanceof Boolean) {
            return Kind.BOOLEAN;
        }
        if (defaultValue instanceof Float || defaultValue instanceof Double) {
            return Kind.DECIMAL;
        }
        if (defaultValue instanceof Number) {
            return Kind.INTEGER;
        }
        return Kind.STRING;
    }

    public String defaultValueString() {
        return String.valueOf(defaultValue);
    }

    public String legacyPathString() {
        int start = !legacyPath.isEmpty()
                && legacyPath.getFirst().equals("ServerEssentialsForgeModConfig")
                ? 1
                : 0;
        return legacyPath.subList(start, legacyPath.size()).stream()
                .map(String::toLowerCase)
                .reduce((left, right) -> left + "." + right)
                .orElseThrow();
    }

    public synchronized void apply(String raw) {
        Object parsed = switch (kind()) {
            case BOOLEAN -> parseBoolean(raw);
            case INTEGER -> parseInteger(raw);
            case DECIMAL -> parseDecimal(raw);
            case STRING -> Objects.requireNonNullElse(raw, "");
        };
        if (!validator.test(parsed)) {
            throw new IllegalArgumentException("runtime configuration value is invalid");
        }
        @SuppressWarnings("unchecked")
        T replacement = (T) parsed;
        value = replacement;
    }

    public synchronized void set(T replacement) {
        Objects.requireNonNull(replacement, "replacement");
        if (!validator.test(replacement)) {
            throw new IllegalArgumentException("runtime configuration value is invalid");
        }
        value = replacement;
    }

    public synchronized void reset() {
        value = defaultValue;
    }

    private Object parseInteger(String raw) {
        try {
            if (defaultValue instanceof Integer) {
                return Integer.valueOf(raw);
            }
            if (defaultValue instanceof Long) {
                return Long.valueOf(raw);
            }
            throw new IllegalArgumentException("unsupported integer configuration type");
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("runtime integer configuration value is invalid", exception);
        }
    }

    private Object parseDecimal(String raw) {
        try {
            double parsed = Double.parseDouble(raw);
            if (!Double.isFinite(parsed)) {
                throw new IllegalArgumentException("runtime decimal configuration value is not finite");
            }
            return defaultValue instanceof Float ? Float.valueOf((float) parsed) : parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("runtime decimal configuration value is invalid", exception);
        }
    }

    private static boolean parseBoolean(String raw) {
        if (!"true".equalsIgnoreCase(raw) && !"false".equalsIgnoreCase(raw)) {
            throw new IllegalArgumentException("runtime boolean configuration value is invalid");
        }
        return Boolean.parseBoolean(raw);
    }

    public enum Kind {
        BOOLEAN,
        INTEGER,
        DECIMAL,
        STRING
    }
}
