package com.enviouse.sef.commands;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class CommandRootPolicy {
    private CommandRootPolicy() {
    }

    public static Decision evaluate(String command, String allowedCommands, String deniedCommands, int maximumLength) {
        return evaluate(command, allowedCommands, deniedCommands, maximumLength, true, true);
    }

    public static Decision evaluate(
            String command,
            String allowedCommands,
            String deniedCommands,
            int maximumLength,
            boolean allowLeadingSlash,
            boolean allowSelectors
    ) {
        if (command == null) {
            return Decision.denied("missing command");
        }

        String normalized = command.strip();
        if (!allowLeadingSlash && normalized.startsWith("/")) {
            return Decision.denied("leading slash is not allowed");
        }
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1).stripLeading();
        }

        if (normalized.isEmpty()) {
            return Decision.denied("missing command");
        }
        if (normalized.length() > maximumLength) {
            return Decision.denied("command is too long");
        }
        if (normalized.codePoints().anyMatch(Character::isISOControl)) {
            return Decision.denied("command contains control characters");
        }
        if (!allowSelectors && containsSelector(normalized)) {
            return Decision.denied("entity selectors are not allowed");
        }

        String root = commandRoot(normalized);
        Set<String> denied = parseRoots(deniedCommands);
        if (denied.contains(root) || denied.contains("*")) {
            return Decision.denied("command root is denied");
        }

        Set<String> allowed = parseRoots(allowedCommands);
        if (!allowed.contains("*") && !allowed.contains(root)) {
            return Decision.denied("command root is not allowed");
        }

        return Decision.allowed(normalized, root);
    }

    private static boolean containsSelector(String command) {
        for (int index = 0; index + 1 < command.length(); index++) {
            if (command.charAt(index) != '@') {
                continue;
            }
            char selector = Character.toLowerCase(command.charAt(index + 1));
            if ("pares".indexOf(selector) < 0) {
                continue;
            }
            if (index == 0 || Character.isWhitespace(command.charAt(index - 1))) {
                return true;
            }
        }
        return false;
    }

    static String commandRoot(String command) {
        int separator = 0;
        while (separator < command.length() && !Character.isWhitespace(command.charAt(separator))) {
            separator++;
        }
        String root = command.substring(0, separator).toLowerCase(Locale.ROOT);
        int namespace = root.indexOf(':');
        return namespace < 0 ? root : root.substring(namespace + 1);
    }

    private static Set<String> parseRoots(String configuredRoots) {
        if (configuredRoots == null || configuredRoots.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(configuredRoots.split(","))
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .map(value -> value.startsWith("/") ? value.substring(1) : value)
                .map(CommandRootPolicy::commandRoot)
                .collect(Collectors.toUnmodifiableSet());
    }

    public record Decision(boolean allowed, String command, String root, String reason) {
        private static Decision allowed(String command, String root) {
            return new Decision(true, command, root, "");
        }

        private static Decision denied(String reason) {
            return new Decision(false, "", "", reason);
        }
    }
}
