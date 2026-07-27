package com.enviouse.sef.economy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class EconomySignParser {
    private static final Pattern ITEM_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    private static final Pattern NAME = Pattern.compile("[a-z0-9_.-]{1,64}");

    private EconomySignParser() {
    }

    public static ParseResult parse(List<String> sourceLines, int maximumQuantity) {
        Objects.requireNonNull(sourceLines, "sourceLines");
        if (sourceLines.size() != 4) {
            return ParseResult.failure("Economy signs require exactly four lines");
        }
        if (maximumQuantity < 1) {
            throw new IllegalArgumentException("Maximum sign quantity must be positive");
        }
        List<String> lines = sourceLines.stream()
                .map(EconomySignParser::normalizeLine)
                .toList();
        String header = lines.getFirst().toLowerCase(Locale.ROOT);
        if (!header.startsWith("[") || !header.endsWith("]")) {
            return ParseResult.notEconomy();
        }
        SignType type = SignType.parse(header.substring(1, header.length() - 1));
        if (type == null) {
            return ParseResult.notEconomy();
        }
        try {
            Definition definition = switch (type) {
                case BALANCE, DISPOSAL -> noArguments(type, lines);
                case BUY, SELL -> pricedItem(type, lines, maximumQuantity);
                case TRADE -> trade(lines, maximumQuantity);
                case FREE -> free(lines, maximumQuantity);
                case KIT -> named(type, lines, false);
                case HEAL, REPAIR -> priceOnly(type, lines);
                case TIME -> namedWithPrice(type, lines, Set.of("day", "night"));
                case WEATHER -> namedWithPrice(type, lines, Set.of("clear", "rain", "thunder"));
                case WARP -> namedWithPrice(type, lines, Set.of());
            };
            return ParseResult.success(definition);
        } catch (IllegalArgumentException exception) {
            return ParseResult.failure(exception.getMessage());
        }
    }

    private static Definition noArguments(SignType type, List<String> lines) {
        requireBlank(lines, 1, 4);
        return definition(type, List.of(), lines);
    }

    private static Definition pricedItem(SignType type, List<String> lines, int maximumQuantity) {
        String item = itemId(lines.get(1));
        int quantity = quantity(lines.get(2), maximumQuantity);
        String price = money(lines.get(3));
        return definition(type, List.of(item, Integer.toString(quantity), price), lines);
    }

    private static Definition trade(List<String> lines, int maximumQuantity) {
        ItemQuantity offered = itemQuantity(lines.get(1), maximumQuantity);
        ItemQuantity received = itemQuantity(lines.get(2), maximumQuantity);
        requireBlank(lines, 3, 4);
        return definition(
                SignType.TRADE,
                List.of(
                        offered.itemId(), Integer.toString(offered.quantity()),
                        received.itemId(), Integer.toString(received.quantity())),
                lines);
    }

    private static Definition free(List<String> lines, int maximumQuantity) {
        String item = itemId(lines.get(1));
        int quantity = quantity(lines.get(2), maximumQuantity);
        requireBlank(lines, 3, 4);
        return definition(SignType.FREE, List.of(item, Integer.toString(quantity)), lines);
    }

    private static Definition named(SignType type, List<String> lines, boolean allowEmpty) {
        String name = normalizeName(lines.get(1), allowEmpty);
        requireBlank(lines, 2, 4);
        return definition(type, List.of(name), lines);
    }

    private static Definition priceOnly(SignType type, List<String> lines) {
        String price = money(lines.get(1));
        requireBlank(lines, 2, 4);
        return definition(type, List.of(price), lines);
    }

    private static Definition namedWithPrice(SignType type, List<String> lines, Set<String> allowedNames) {
        String name = normalizeName(lines.get(1), false);
        if (!allowedNames.isEmpty() && !allowedNames.contains(name)) {
            throw new IllegalArgumentException("Economy sign option is invalid");
        }
        String price = money(lines.get(2));
        requireBlank(lines, 3, 4);
        return definition(type, List.of(name, price), lines);
    }

    private static Definition definition(SignType type, List<String> arguments, List<String> lines) {
        return new Definition(type, arguments, fingerprint(lines));
    }

    private static ItemQuantity itemQuantity(String value, int maximumQuantity) {
        String[] parts = value.split("\\*", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Trade items must use item_id*quantity");
        }
        return new ItemQuantity(itemId(parts[0]), quantity(parts[1], maximumQuantity));
    }

    private static String itemId(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (!ITEM_ID.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Economy sign item id is invalid");
        }
        return normalized;
    }

    private static int quantity(String value, int maximumQuantity) {
        if (!value.matches("[1-9][0-9]{0,8}")) {
            throw new IllegalArgumentException("Economy sign quantity is invalid");
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed > maximumQuantity) {
                throw new IllegalArgumentException("Economy sign quantity exceeds the configured limit");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Economy sign quantity is invalid", exception);
        }
    }

    private static String money(String value) {
        if (value.isEmpty()
                || value.length() > 32
                || !value.matches("(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?")) {
            throw new IllegalArgumentException("Economy sign amount must be a plain nonnegative decimal");
        }
        return value;
    }

    private static String normalizeName(String value, boolean allowEmpty) {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (allowEmpty && normalized.isEmpty()) {
            return "";
        }
        if (!NAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Economy sign name is invalid");
        }
        return normalized;
    }

    private static void requireBlank(List<String> lines, int start, int end) {
        for (int index = start; index < end; index++) {
            if (!lines.get(index).isEmpty()) {
                throw new IllegalArgumentException("Economy sign has unexpected text");
            }
        }
    }

    private static String normalizeLine(String value) {
        String normalized = Objects.requireNonNullElse(value, "").strip();
        if (normalized.length() > 384
                || normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Economy sign text is invalid");
        }
        return normalized;
    }

    private static String fingerprint(List<String> lines) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public enum SignType {
        BALANCE,
        BUY,
        SELL,
        TRADE,
        FREE,
        DISPOSAL,
        KIT,
        HEAL,
        REPAIR,
        TIME,
        WEATHER,
        WARP;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static SignType parse(String value) {
            return Arrays.stream(values())
                    .filter(type -> type.id().equals(value.toLowerCase(Locale.ROOT)))
                    .findFirst()
                    .orElse(null);
        }
    }

    public record Definition(SignType type, List<String> arguments, String fingerprint) {
        public Definition {
            Objects.requireNonNull(type, "type");
            arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
            if (arguments.size() > 4) {
                throw new IllegalArgumentException("Economy sign argument count exceeds limit");
            }
            Objects.requireNonNull(fingerprint, "fingerprint");
        }
    }

    public record ParseResult(Status status, Definition definition, String detail) {
        public ParseResult {
            Objects.requireNonNull(status, "status");
            detail = Objects.requireNonNullElse(detail, "");
        }

        static ParseResult success(Definition definition) {
            return new ParseResult(Status.SUCCESS, definition, "");
        }

        static ParseResult failure(String detail) {
            return new ParseResult(Status.INVALID, null, detail);
        }

        static ParseResult notEconomy() {
            return new ParseResult(Status.NOT_ECONOMY, null, "");
        }

        public boolean successful() {
            return status == Status.SUCCESS;
        }
    }

    public enum Status {
        SUCCESS,
        NOT_ECONOMY,
        INVALID
    }

    private record ItemQuantity(String itemId, int quantity) {
    }
}
