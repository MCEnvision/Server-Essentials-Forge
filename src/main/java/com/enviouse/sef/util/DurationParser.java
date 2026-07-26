package com.enviouse.sef.util;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationParser {
    public static final long INVALID_VALUE = Long.MIN_VALUE;

    private static final Pattern PART = Pattern.compile("(\\d+)([smhdw])");
    private static final Set<String> PERMANENT = Set.of("permanent", "perm", "forever", "infinite", "inf");

    private DurationParser() {
    }

    public static Result parse(String input, boolean allowPermanent) {
        if (input == null || input.isBlank()) {
            return Result.invalid(Error.EMPTY);
        }

        String normalized = input.strip().toLowerCase(Locale.ROOT);
        if (PERMANENT.contains(normalized)) {
            return allowPermanent ? Result.permanentResult() : Result.invalid(Error.PERMANENT_NOT_ALLOWED);
        }

        try {
            long seconds = Long.parseLong(normalized);
            return seconds > 0 ? Result.duration(seconds) : Result.invalid(Error.NON_POSITIVE);
        } catch (NumberFormatException ignored) {
        }

        Matcher matcher = PART.matcher(normalized);
        Set<Character> units = new HashSet<>();
        long total = 0L;
        int cursor = 0;
        boolean found = false;

        while (matcher.find()) {
            if (!normalized.substring(cursor, matcher.start()).isBlank()) {
                return Result.invalid(Error.INVALID_FORMAT);
            }

            long value;
            try {
                value = Long.parseLong(matcher.group(1));
            } catch (NumberFormatException exception) {
                return Result.invalid(Error.OVERFLOW);
            }
            if (value <= 0) {
                return Result.invalid(Error.NON_POSITIVE);
            }

            char unit = matcher.group(2).charAt(0);
            if (!units.add(unit)) {
                return Result.invalid(Error.DUPLICATE_UNIT);
            }

            try {
                total = Math.addExact(total, Math.multiplyExact(value, multiplier(unit)));
            } catch (ArithmeticException exception) {
                return Result.invalid(Error.OVERFLOW);
            }

            cursor = matcher.end();
            found = true;
        }

        if (!found || !normalized.substring(cursor).isBlank()) {
            return Result.invalid(Error.INVALID_FORMAT);
        }
        return total > 0 ? Result.duration(total) : Result.invalid(Error.NON_POSITIVE);
    }

    public static long toTicks(Result result) {
        return scale(result, 20L);
    }

    public static long toMilliseconds(Result result) {
        return scale(result, 1_000L);
    }

    public static String humanReadable(long seconds) {
        if (seconds <= 0) {
            return "0s";
        }

        long weeks = seconds / 604_800L;
        long days = seconds % 604_800L / 86_400L;
        long hours = seconds % 86_400L / 3_600L;
        long minutes = seconds % 3_600L / 60L;
        long remainingSeconds = seconds % 60L;
        StringBuilder result = new StringBuilder();
        append(result, weeks, 'w');
        append(result, days, 'd');
        append(result, hours, 'h');
        append(result, minutes, 'm');
        append(result, remainingSeconds, 's');
        return result.toString();
    }

    private static long scale(Result result, long multiplier) {
        if (!result.valid()) {
            return INVALID_VALUE;
        }
        if (result.permanent()) {
            return -1L;
        }
        try {
            return Math.multiplyExact(result.seconds(), multiplier);
        } catch (ArithmeticException exception) {
            return INVALID_VALUE;
        }
    }

    private static long multiplier(char unit) {
        return switch (unit) {
            case 's' -> 1L;
            case 'm' -> 60L;
            case 'h' -> 3_600L;
            case 'd' -> 86_400L;
            case 'w' -> 604_800L;
            default -> throw new IllegalArgumentException("Unsupported duration unit");
        };
    }

    private static void append(StringBuilder builder, long value, char unit) {
        if (value > 0) {
            builder.append(value).append(unit);
        }
    }

    public enum Error {
        NONE,
        EMPTY,
        INVALID_FORMAT,
        NON_POSITIVE,
        DUPLICATE_UNIT,
        OVERFLOW,
        PERMANENT_NOT_ALLOWED
    }

    public record Result(boolean valid, boolean permanent, long seconds, Error error) {
        private static Result duration(long seconds) {
            return new Result(true, false, seconds, Error.NONE);
        }

        private static Result permanentResult() {
            return new Result(true, true, 0L, Error.NONE);
        }

        private static Result invalid(Error error) {
            return new Result(false, false, 0L, error);
        }
    }
}
