package com.enviouse.sef.commandlog;

import com.enviouse.sef.kernel.KernelServices;

import java.util.Locale;
import java.util.Set;

public final class CommandRedactionPolicy {
    private static final Set<String> SECRET_ROOTS = Set.of(
            "login", "register", "password", "changepassword", "auth", "authenticate",
            "2fa", "otp", "pin", "token");
    private static final Set<String> PRIVATE_CONTENT_ROOTS = Set.of(
            "msg", "tell", "w", "whisper", "r", "reply", "mail", "pchat",
            "fakemessage", "fakerankmessage",
            "ban", "tempban", "kick", "kickall", "kickme", "mute", "tempmute",
            "warn", "jail", "freeze");
    private static final Set<String> WRAPPER_ROOTS = Set.of(
            "execute", "function", "run", "schedule", "silent", "sudo");
    private static final Set<String> SENSITIVE_ARGUMENT_ROOTS = Set.of("data");
    private static final Set<String> NETWORK_ADDRESS_ROOTS = Set.of(
            "ban-ip", "banip", "tempban-ip", "tempbanip",
            "pardon-ip", "unban-ip", "unbanip",
            "kick-ip", "kickip");

    private CommandRedactionPolicy() {
    }

    public static RedactedCommand redact(String input) {
        String sanitized = sanitize(input, 512);
        while (sanitized.startsWith("/")) {
            sanitized = sanitized.substring(1).stripLeading();
        }
        String root = root(sanitized);
        if (root.isBlank()) {
            return new RedactedCommand("", "<empty>", RedactionClass.UNKNOWN_ROOT, Set.of("empty"));
        }
        if (SECRET_ROOTS.contains(root)) {
            return new RedactedCommand(root, "/" + root + " <secret>", RedactionClass.SECRET, Set.of("secret_root"));
        }
        if (NETWORK_ADDRESS_ROOTS.contains(root)) {
            return new RedactedCommand(
                    root,
                    "/" + root + " <network address redacted>",
                    RedactionClass.SECRET,
                    Set.of("network_address"));
        }
        if (PRIVATE_CONTENT_ROOTS.contains(root)) {
            return new RedactedCommand(root, "/" + root + " <private>", RedactionClass.PRIVATE_CONTENT,
                    Set.of("private_content"));
        }
        if (WRAPPER_ROOTS.contains(root)) {
            return new RedactedCommand(
                    root,
                    "/" + root + " <nested command redacted>",
                    RedactionClass.PRIVATE_CONTENT,
                    Set.of("nested_command"));
        }
        if (SENSITIVE_ARGUMENT_ROOTS.contains(root)) {
            return new RedactedCommand(
                    root,
                    "/" + root + " <arguments redacted>",
                    RedactionClass.SECRET,
                    Set.of("sensitive_arguments"));
        }
        if (!knownRoot(root)) {
            return new RedactedCommand(root, "/" + root + " <redacted>", RedactionClass.UNKNOWN_ROOT,
                    Set.of("unknown_root"));
        }
        return new RedactedCommand(root, "/" + sanitized, RedactionClass.PUBLIC, Set.of());
    }

    static String root(String command) {
        if (command == null || command.isBlank()) {
            return "";
        }
        int separator = 0;
        while (separator < command.length() && !Character.isWhitespace(command.charAt(separator))) {
            separator++;
        }
        String root = command.substring(0, separator).toLowerCase(Locale.ROOT);
        int namespace = root.indexOf(':');
        return namespace < 0 ? root : root.substring(namespace + 1);
    }

    private static boolean knownRoot(String root) {
        try {
            return KernelServices.catalog().rootOwner(root).isPresent()
                    || Set.of(
                    "advancement", "attribute", "bossbar", "clear", "clone", "damage", "data",
                    "datapack", "debug", "defaultgamemode", "deop", "difficulty", "effect",
                    "enchant", "execute", "experience", "fill", "fillbiome", "forceload",
                    "function", "gamemode", "gamerule", "give", "help", "item", "jfr",
                    "kick", "kill", "list", "locate", "loot", "me", "op", "particle",
                    "place", "playsound", "publish", "random", "recipe", "reload", "return",
                    "ride", "rotate", "save", "say", "schedule", "scoreboard", "seed",
                    "setblock", "setidletimeout", "setworldspawn", "spawnpoint", "spectate",
                    "spreadplayers", "stop", "stopsound", "summon", "tag", "team",
                    "teammsg", "teleport", "tick", "time", "title", "transfer", "trigger",
                    "weather", "whitelist", "worldborder").contains(root);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static String sanitize(String value, int maximumLength) {
        if (value == null) {
            return "";
        }
        String sanitized = value.replace("\r", "\\r").replace("\n", "\\n").codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString()
                .trim();
        return sanitized.length() <= maximumLength ? sanitized : sanitized.substring(0, maximumLength);
    }

    public record RedactedCommand(
            String root,
            String display,
            RedactionClass redactionClass,
            Set<String> ruleIds
    ) {
        public RedactedCommand {
            root = sanitize(root, 64).toLowerCase(Locale.ROOT);
            display = sanitize(display, 512);
            ruleIds = Set.copyOf(ruleIds);
        }
    }

    public enum RedactionClass {
        PUBLIC,
        PRIVATE_CONTENT,
        SECRET,
        UNKNOWN_ROOT
    }
}
