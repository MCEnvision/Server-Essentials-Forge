package com.enviouse.sef.economy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EconomySignParserTest {
    @Test
    void parsesEverySupportedSignType() {
        assertType(EconomySignParser.SignType.BALANCE, "[balance]", "", "", "");
        assertType(EconomySignParser.SignType.BUY, "[buy]", "minecraft:stone", "64", "12.50");
        assertType(EconomySignParser.SignType.SELL, "[sell]", "minecraft:stone", "64", "12.50");
        assertType(EconomySignParser.SignType.TRADE, "[trade]",
                "minecraft:stone*4", "minecraft:diamond*1", "");
        assertType(EconomySignParser.SignType.FREE, "[free]", "minecraft:bread", "8", "");
        assertType(EconomySignParser.SignType.DISPOSAL, "[disposal]", "", "", "");
        assertType(EconomySignParser.SignType.KIT, "[kit]", "starter", "", "");
        assertType(EconomySignParser.SignType.HEAL, "[heal]", "5.00", "", "");
        assertType(EconomySignParser.SignType.REPAIR, "[repair]", "25.00", "", "");
        assertType(EconomySignParser.SignType.TIME, "[time]", "day", "2.00", "");
        assertType(EconomySignParser.SignType.WEATHER, "[weather]", "clear", "2.00", "");
        assertType(EconomySignParser.SignType.WARP, "[warp]", "spawn_market", "1.00", "");
    }

    @Test
    void rejectsLooseOrDangerousSyntax() {
        assertInvalid("[buy]", "stone", "64", "1.00");
        assertInvalid("[buy]", "minecraft:stone", "0", "1.00");
        assertInvalid("[buy]", "minecraft:stone", "65", "1.00");
        assertInvalid("[buy]", "minecraft:stone", "1", "1e3");
        assertInvalid("[balance]", "unexpected", "", "");
        assertInvalid("[trade]", "minecraft:stone:2", "minecraft:dirt*1", "");
        assertInvalid("[weather]", "tornado", "1.00", "");
    }

    @Test
    void fingerprintTracksNormalizedDefinitionText() {
        EconomySignParser.ParseResult first = parse(" [buy] ", " minecraft:stone ", " 2 ", " 1.00 ");
        EconomySignParser.ParseResult same = parse("[buy]", "minecraft:stone", "2", "1.00");
        EconomySignParser.ParseResult changed = parse("[buy]", "minecraft:stone", "3", "1.00");

        assertTrue(first.successful());
        assertEquals(first.definition().fingerprint(), same.definition().fingerprint());
        assertNotEquals(first.definition().fingerprint(), changed.definition().fingerprint());
    }

    @Test
    void unknownBracketHeaderIsNotClaimed() {
        assertEquals(
                EconomySignParser.Status.NOT_ECONOMY,
                parse("[shop]", "", "", "").status());
    }

    private static void assertType(
            EconomySignParser.SignType expected,
            String first,
            String second,
            String third,
            String fourth
    ) {
        EconomySignParser.ParseResult result = parse(first, second, third, fourth);
        assertTrue(result.successful(), result.detail());
        assertEquals(expected, result.definition().type());
    }

    private static void assertInvalid(String first, String second, String third, String fourth) {
        assertEquals(EconomySignParser.Status.INVALID, parse(first, second, third, fourth).status());
    }

    private static EconomySignParser.ParseResult parse(
            String first,
            String second,
            String third,
            String fourth
    ) {
        return EconomySignParser.parse(List.of(first, second, third, fourth), 64);
    }
}
