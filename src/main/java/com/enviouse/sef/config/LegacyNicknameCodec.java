package com.enviouse.sef.config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class LegacyNicknameCodec {
    public record Entry(UUID uuid, String nickname) {
    }

    private LegacyNicknameCodec() {
    }

    public static List<Entry> parse(String input) {
        List<Entry> parsed = new ArrayList<>();
        if (input == null || input.isBlank()) return parsed;
        String[] lines = input.split("\\R");
        for (int index = 0; index < lines.length; index++) {
            if (!lines[index].trim().equals("[PlayerDataEntry]")) continue;
            UUID uuid = null;
            String nickname = null;
            for (index++; index < lines.length && !lines[index].trim().equals("[PlayerDataEntry]"); index++) {
                String line = lines[index].trim();
                int separator = line.indexOf(':');
                if (separator < 0) continue;
                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                try {
                    if (key.equals("UUID")) uuid = UUID.fromString(decode(value));
                    if (key.equals("Nickname")) nickname = decode(value);
                } catch (RuntimeException ignored) {
                }
            }
            index--;
            if (uuid != null) parsed.add(new Entry(uuid, nickname));
        }
        return List.copyOf(parsed);
    }

    static String decode(String value) {
        if (value == null || value.equals("null")) return null;
        if (value.length() < 2 || value.charAt(0) != '"' || value.charAt(value.length() - 1) != '"') {
            return null;
        }
        StringBuilder output = new StringBuilder(value.length() - 2);
        for (int index = 1; index < value.length() - 1; index++) {
            char current = value.charAt(index);
            if (current == '\\' && index + 1 < value.length() - 1) {
                char escaped = value.charAt(++index);
                if (escaped == 'u' && index + 4 < value.length() - 1) {
                    output.append((char) Integer.parseUnsignedInt(value.substring(index + 1, index + 5), 16));
                    index += 4;
                } else if (escaped == '"' || escaped == '\\') {
                    output.append(escaped);
                } else {
                    return null;
                }
            } else {
                output.append(current);
            }
        }
        return output.toString();
    }
}
