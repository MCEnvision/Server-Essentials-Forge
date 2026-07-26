package com.enviouse.sef.announcements;

/**
 * Compatibility facade for announcement duration parsing.
 */
public final class DurationParser {
    private DurationParser() {}

    public static long parseSeconds(String input) {
        com.enviouse.sef.util.DurationParser.Result result =
                com.enviouse.sef.util.DurationParser.parse(input, false);
        if (!result.valid() || com.enviouse.sef.util.DurationParser.toTicks(result)
                == com.enviouse.sef.util.DurationParser.INVALID_VALUE) {
            return -1L;
        }
        return result.seconds();
    }

    public static String humanReadable(long seconds) {
        return com.enviouse.sef.util.DurationParser.humanReadable(seconds);
    }
}
