package com.enviouse.sef.commands;

import java.text.Normalizer;
import java.util.Locale;

public final class NicknamePolicy {
    private NicknamePolicy() {
    }

    static Validation validate(String visibleNickname, int minimumLength, int maximumLength) {
        if (visibleNickname == null) {
            return Validation.invalid("nickname is missing");
        }

        String trimmed = visibleNickname.strip();
        int length = trimmed.codePointCount(0, trimmed.length());
        if (length < minimumLength || length > maximumLength) {
            return Validation.invalid("nickname must be between " + minimumLength + " and " + maximumLength + " characters");
        }
        if (trimmed.codePoints().anyMatch(NicknamePolicy::isUnsafeCodePoint)) {
            return Validation.invalid("nickname contains unsupported control characters");
        }

        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
        return new Validation(true, normalized, "");
    }

    public static String normalizeIdentity(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.strip(), Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
    }

    public static String stripFormatting(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        StringBuilder visible = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if ((current == '&' || current == '\u00A7') && index + 1 < value.length()) {
                char code = Character.toLowerCase(value.charAt(index + 1));
                if (code == '#' && index + 7 < value.length()
                        && isHex(value, index + 2, index + 8)) {
                    index += 7;
                    continue;
                }
                if (isLegacyColor(code) || isLegacyStyle(code)) {
                    index++;
                    continue;
                }
                if (current == '&' && code == '&') {
                    visible.append('&');
                    index++;
                    continue;
                }
            }
            visible.append(current);
        }
        return visible.toString();
    }

    static boolean containsColorFormatting(String value) {
        return containsFormatting(value, true);
    }

    static boolean containsStyleFormatting(String value) {
        return containsFormatting(value, false);
    }

    private static boolean containsFormatting(String value, boolean colors) {
        if (value == null) {
            return false;
        }
        for (int index = 0; index + 1 < value.length(); index++) {
            char current = value.charAt(index);
            if (current != '&' && current != '\u00A7') {
                continue;
            }
            char code = Character.toLowerCase(value.charAt(index + 1));
            if (colors && code == '#' && index + 7 < value.length()
                    && isHex(value, index + 2, index + 8)) {
                return true;
            }
            if (colors ? isLegacyColor(code) : isLegacyStyle(code)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHex(String value, int start, int end) {
        for (int index = start; index < end; index++) {
            if (Character.digit(value.charAt(index), 16) < 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLegacyColor(char code) {
        return code >= '0' && code <= '9' || code >= 'a' && code <= 'f';
    }

    private static boolean isLegacyStyle(char code) {
        return code >= 'k' && code <= 'o' || code == 'r';
    }

    private static boolean isUnsafeCodePoint(int codePoint) {
        int type = Character.getType(codePoint);
        return Character.isISOControl(codePoint)
                || type == Character.FORMAT
                || type == Character.SURROGATE
                || type == Character.PRIVATE_USE
                || type == Character.UNASSIGNED;
    }

    record Validation(boolean valid, String normalized, String error) {
        private static Validation invalid(String error) {
            return new Validation(false, "", error);
        }
    }
}
