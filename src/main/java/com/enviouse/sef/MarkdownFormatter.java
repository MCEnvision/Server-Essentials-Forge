package com.enviouse.sef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MarkdownFormatter {
    private MarkdownFormatter() {
    }

    public static String markdownStringToFormattedString(String markdown) {
        return markdownStringToFormattedString(markdown, BitwiseStyling.ALL_STYLES);
    }

    public static String markdownStringToFormattedString(String markdown, byte allowedMask) {
        String input = Objects.requireNonNull(markdown, "markdown");
        Map<Integer, Token> tokens = pairedTokens(input);
        StringBuilder output = new StringBuilder(input.length() + 16);
        byte mask = 0;
        for (int position = 0; position < input.length(); position++) {
            char current = input.charAt(position);
            if (current == '\\') {
                if (position + 1 < input.length() && escapable(input.charAt(position + 1))) {
                    output.append(input.charAt(++position));
                } else {
                    output.append(current);
                }
                continue;
            }
            Token token = tokens.get(position);
            if (token == null) {
                output.append(current);
                continue;
            }
            byte replacement = bitToggle(mask, token.style());
            output.append(maskDiff(replacement, mask, allowedMask));
            mask = replacement;
            position += token.length() - 1;
        }
        return output.toString();
    }

    private static Map<Integer, Token> pairedTokens(String input) {
        Map<String, List<Token>> byDelimiter = new HashMap<>();
        for (int position = 0; position < input.length(); position++) {
            char current = input.charAt(position);
            if (current == '\\' && position + 1 < input.length()
                    && escapable(input.charAt(position + 1))) {
                position++;
                continue;
            }
            Token token = token(input, position);
            if (token == null) {
                continue;
            }
            byDelimiter.computeIfAbsent(token.delimiter(), ignored -> new ArrayList<>()).add(token);
            position += token.length() - 1;
        }
        Map<Integer, Token> paired = new HashMap<>();
        for (List<Token> candidates : byDelimiter.values()) {
            int pairedCount = candidates.size() - candidates.size() % 2;
            for (int index = 0; index < pairedCount; index++) {
                Token token = candidates.get(index);
                paired.put(token.position(), token);
            }
        }
        return paired;
    }

    private static Token token(String input, int position) {
        char marker = input.charAt(position);
        if (marker != '*' && marker != '_' && marker != '~') {
            return null;
        }
        boolean doubled = position + 1 < input.length() && input.charAt(position + 1) == marker;
        String delimiter = doubled ? input.substring(position, position + 2) : String.valueOf(marker);
        byte style = switch (delimiter) {
            case "**" -> BitwiseStyling.BOLD_BIT;
            case "__" -> BitwiseStyling.UNDERLINE_BIT;
            case "~~" -> BitwiseStyling.STRIKETHROUGH_BIT;
            case "*", "_" -> BitwiseStyling.ITALIC_BIT;
            case "~" -> BitwiseStyling.OBFUSCATED_BIT;
            default -> BitwiseStyling.NO_STYLE;
        };
        return new Token(position, delimiter.length(), delimiter, style);
    }

    private static boolean escapable(char value) {
        return value == '\\' || value == '*' || value == '_' || value == '~';
    }

    private static String maskDiff(byte newMask, byte oldMask, byte allowedMask) {
        byte effectiveNew = (byte) (newMask & allowedMask);
        byte effectiveOld = (byte) (oldMask & allowedMask);
        if (effectiveNew == effectiveOld) {
            return "";
        }
        if (effectiveNew == 0) {
            return TextFormatter.RESET_ALL_FORMAT;
        }
        int stylesNew = Integer.bitCount(Byte.toUnsignedInt(effectiveNew));
        int stylesOld = Integer.bitCount(Byte.toUnsignedInt(effectiveOld));
        if (stylesNew > stylesOld) {
            return BitwiseStyling.styleString((byte) (effectiveNew & ~effectiveOld));
        }
        return TextFormatter.RESET_ALL_FORMAT + BitwiseStyling.styleString(effectiveNew);
    }

    private static byte bitToggle(byte mask, byte bit) {
        return (byte) (mask ^ bit);
    }

    private record Token(int position, int length, String delimiter, byte style) {
    }
}
