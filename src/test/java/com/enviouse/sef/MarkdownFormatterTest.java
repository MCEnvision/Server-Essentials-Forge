package com.enviouse.sef;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarkdownFormatterTest {
    @Test
    void everyMarkerFormatsOnlyWhenPaired() {
        assertEquals("&lbold&r", MarkdownFormatter.markdownStringToFormattedString("**bold**"));
        assertEquals("&nunder&r", MarkdownFormatter.markdownStringToFormattedString("__under__"));
        assertEquals("&mstrike&r", MarkdownFormatter.markdownStringToFormattedString("~~strike~~"));
        assertEquals("&oitalic&r", MarkdownFormatter.markdownStringToFormattedString("*italic*"));
        assertEquals("&oitalic&r", MarkdownFormatter.markdownStringToFormattedString("_italic_"));
        assertEquals("&kobfuscated&r", MarkdownFormatter.markdownStringToFormattedString("~obfuscated~"));
        assertEquals("*unmatched", MarkdownFormatter.markdownStringToFormattedString("*unmatched"));
    }

    @Test
    void escapesAndWhitespaceArePreserved() {
        assertEquals(
                "  *literal* \\\\ path\\q  ",
                MarkdownFormatter.markdownStringToFormattedString(
                        "  \\*literal\\* \\\\\\\\ path\\q  "));
        assertEquals(
                "folder\\name &lbold&r",
                MarkdownFormatter.markdownStringToFormattedString("folder\\name **bold**"));
    }

    @Test
    void nestedStylesRestoreTheOuterStyle() {
        assertEquals(
                "&lbold &oinner&r&l outer&r",
                MarkdownFormatter.markdownStringToFormattedString("**bold *inner* outer**"));
    }

    @Test
    void plainUnicodeTextIsUnchanged() {
        Random random = new Random(6_172_026L);
        for (int sample = 0; sample < 2_000; sample++) {
            int length = random.nextInt(128);
            StringBuilder plain = new StringBuilder(length);
            for (int index = 0; index < length; index++) {
                int choice = random.nextInt(5);
                plain.append(switch (choice) {
                    case 0 -> (char) ('a' + random.nextInt(26));
                    case 1 -> (char) ('0' + random.nextInt(10));
                    case 2 -> ' ';
                    case 3 -> '\u2603';
                    default -> '\u4e16';
                });
            }
            assertEquals(
                    plain.toString(),
                    MarkdownFormatter.markdownStringToFormattedString(plain.toString()));
        }
    }
}
