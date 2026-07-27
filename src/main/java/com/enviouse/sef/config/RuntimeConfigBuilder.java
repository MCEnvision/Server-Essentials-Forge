package com.enviouse.sef.config;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class RuntimeConfigBuilder {
    private final Deque<String> sections = new ArrayDeque<>();
    private final List<RuntimeConfigValue<?>> values = new ArrayList<>();
    private String pendingComment = "";

    public RuntimeConfigBuilder push(String section) {
        String normalized = Objects.requireNonNull(section, "section").strip();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("runtime configuration section is empty");
        }
        sections.addLast(normalized);
        pendingComment = "";
        return this;
    }

    public RuntimeConfigBuilder pop() {
        if (sections.isEmpty()) {
            throw new IllegalStateException("runtime configuration section stack is empty");
        }
        sections.removeLast();
        pendingComment = "";
        return this;
    }

    public RuntimeConfigBuilder comment(String... comments) {
        pendingComment = String.join(" ", comments).strip();
        return this;
    }

    public <T> RuntimeConfigValue<T> define(String name, T defaultValue) {
        return define(name, defaultValue, ignored -> true);
    }

    public <T> RuntimeConfigValue<T> define(
            String name,
            T defaultValue,
            Predicate<Object> validator
    ) {
        return add(name, defaultValue, validator, null, null);
    }

    public <T extends Number & Comparable<T>> RuntimeConfigValue<T> defineInRange(
            String name,
            T defaultValue,
            T minimum,
            T maximum
    ) {
        if (minimum.compareTo(maximum) > 0
                || defaultValue.compareTo(minimum) < 0
                || defaultValue.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("runtime configuration range is invalid");
        }
        return add(
                name,
                defaultValue,
                candidate -> {
                    if (!(candidate instanceof Number number)) {
                        return false;
                    }
                    double value = number.doubleValue();
                    return value >= minimum.doubleValue() && value <= maximum.doubleValue();
                },
                minimum,
                maximum);
    }

    public List<RuntimeConfigValue<?>> values() {
        return List.copyOf(values);
    }

    private <T> RuntimeConfigValue<T> add(
            String name,
            T defaultValue,
            Predicate<Object> validator,
            Number minimum,
            Number maximum
    ) {
        String normalized = Objects.requireNonNull(name, "name").strip();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("runtime configuration key is empty");
        }
        List<String> path = new ArrayList<>(sections);
        path.add(normalized);
        RuntimeConfigValue<T> value = new RuntimeConfigValue<>(
                path,
                pendingComment,
                defaultValue,
                validator,
                minimum,
                maximum);
        values.add(value);
        pendingComment = "";
        return value;
    }
}
