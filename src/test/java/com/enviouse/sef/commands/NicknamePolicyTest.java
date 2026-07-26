package com.enviouse.sef.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NicknamePolicyTest {
    @Test
    void validatesVisibleCodePointLength() {
        assertTrue(NicknamePolicy.validate("Alex", 1, 16).valid());
        assertTrue(NicknamePolicy.validate("😀", 1, 1).valid());
        assertFalse(NicknamePolicy.validate("", 1, 16).valid());
        assertFalse(NicknamePolicy.validate("TooLong", 1, 3).valid());
    }

    @Test
    void rejectsControlAndInvisibleFormatCharacters() {
        assertFalse(NicknamePolicy.validate("Admin\nName", 1, 20).valid());
        assertFalse(NicknamePolicy.validate("Admin\u200BName", 1, 20).valid());
        assertFalse(NicknamePolicy.validate("Admin\uE000", 1, 20).valid());
    }

    @Test
    void normalizesCompatibilityCharactersAndCase() {
        assertEquals("admin", NicknamePolicy.normalizeIdentity("ＡＤＭＩＮ"));
        assertEquals("notch", NicknamePolicy.normalizeIdentity(" Notch "));
    }

    @Test
    void stripsLegacyAndHexFormattingBeforeIdentityChecks() {
        assertEquals("Admin", NicknamePolicy.stripFormatting("&cAd&#00FF00min&r"));
        assertEquals("Admin", NicknamePolicy.stripFormatting("\u00A7cAdmin"));
        assertTrue(NicknamePolicy.containsColorFormatting("&#00FF00Admin"));
        assertTrue(NicknamePolicy.containsStyleFormatting("&lAdmin"));
    }
}
