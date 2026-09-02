package com.enviouse.sef.economy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.regex.Pattern;

public final class EconomyMoney {
    private static final Pattern UNSIGNED = Pattern.compile("(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?");
    private static final Pattern SIGNED = Pattern.compile("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?");

    private EconomyMoney() {
    }

    public static long parsePositive(String input, int minorUnits, long maximum) {
        long parsed = parse(input, minorUnits, 0L, maximum, false);
        if (parsed <= 0L) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        return parsed;
    }

    public static long parse(
            String input,
            int minorUnits,
            long minimum,
            long maximum,
            boolean signed
    ) {
        validateScale(minorUnits);
        if (minimum > maximum) {
            throw new IllegalArgumentException("Money bounds are invalid");
        }
        String value = Objects.requireNonNull(input, "input").strip();
        if (value.isEmpty() || value.length() > 32 || !(signed ? SIGNED : UNSIGNED).matcher(value).matches()) {
            throw new IllegalArgumentException("Amount must be a plain decimal number");
        }
        BigDecimal decimal;
        try {
            decimal = new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Amount is invalid", exception);
        }
        return toMinorUnits(decimal, minorUnits, minimum, maximum);
    }

    public static long toMinorUnits(
            BigDecimal amount,
            int minorUnits,
            long minimum,
            long maximum
    ) {
        validateScale(minorUnits);
        Objects.requireNonNull(amount, "amount");
        final long value;
        try {
            value = amount.setScale(minorUnits, RoundingMode.UNNECESSARY)
                    .movePointRight(minorUnits)
                    .longValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Amount has excessive precision or overflows", exception);
        }
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException("Amount is outside configured bounds");
        }
        return value;
    }

    public static BigDecimal toMajorUnits(long amount, int minorUnits) {
        validateScale(minorUnits);
        return BigDecimal.valueOf(amount, minorUnits);
    }

    public static String format(long amount, int minorUnits, String symbol) {
        String prefix = Objects.requireNonNullElse(symbol, "");
        return prefix + toMajorUnits(amount, minorUnits).setScale(minorUnits).toPlainString();
    }

    public static long multiply(long amount, int quantity, long maximum) {
        if (amount < 0L || quantity < 0 || maximum < 0L) {
            throw new IllegalArgumentException("Money multiplication inputs must be nonnegative");
        }
        final long result;
        try {
            result = Math.multiplyExact(amount, quantity);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Money multiplication overflows", exception);
        }
        if (result > maximum) {
            throw new IllegalArgumentException("Transaction value exceeds the configured limit");
        }
        return result;
    }

    private static void validateScale(int minorUnits) {
        if (minorUnits < 0 || minorUnits > 8) {
            throw new IllegalArgumentException("Minor units must be between zero and eight");
        }
    }
}
