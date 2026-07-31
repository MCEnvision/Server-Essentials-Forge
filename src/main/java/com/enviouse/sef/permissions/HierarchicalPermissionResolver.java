package com.enviouse.sef.permissions;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

final class HierarchicalPermissionResolver {
    private HierarchicalPermissionResolver() {
    }

    static boolean resolve(
            String permission,
            Function<String, Evaluation> checker
    ) {
        return resolveDecision(permission, checker) == Evaluation.GRANTED;
    }

    static Evaluation resolveDecision(
            String permission,
            Function<String, Evaluation> checker
    ) {
        String normalized = Objects.requireNonNull(permission, "permission")
                .strip()
                .toLowerCase(Locale.ROOT);
        Objects.requireNonNull(checker, "checker");
        if (normalized.isBlank()) {
            return Evaluation.UNDEFINED;
        }
        Evaluation exact = decision(checker, normalized);
        if (exact != Evaluation.UNDEFINED) {
            return exact;
        }
        int separator = normalized.lastIndexOf('.');
        while (separator > 0) {
            Evaluation wildcard = decision(
                    checker,
                    normalized.substring(0, separator) + ".*");
            if (wildcard != Evaluation.UNDEFINED) {
                return wildcard;
            }
            separator = normalized.lastIndexOf('.', separator - 1);
        }
        return decision(checker, "*");
    }

    private static Evaluation decision(
            Function<String, Evaluation> checker,
            String permission
    ) {
        return Objects.requireNonNullElse(
                checker.apply(permission),
                Evaluation.UNDEFINED);
    }

    enum Evaluation {
        GRANTED,
        DENIED,
        UNDEFINED
    }
}
