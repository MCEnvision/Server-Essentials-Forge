package com.enviouse.sef.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DurationParserTest {
    @Test
    void parsesPlainSecondsAndCompoundDurations() {
        assertEquals(90L, DurationParser.parse("90", false).seconds());
        assertEquals(5_445L, DurationParser.parse("1h 30m 45s", false).seconds());
        assertEquals(788_400L, DurationParser.parse("1w2d3h", false).seconds());
    }

    @Test
    void permanentMustBeExplicitlyAllowed() {
        DurationParser.Result allowed = DurationParser.parse("permanent", true);
        assertTrue(allowed.valid());
        assertTrue(allowed.permanent());
        assertEquals(-1L, DurationParser.toTicks(allowed));

        DurationParser.Result denied = DurationParser.parse("permanent", false);
        assertFalse(denied.valid());
        assertEquals(DurationParser.Error.PERMANENT_NOT_ALLOWED, denied.error());
    }

    @Test
    void rejectsAmbiguousAndMalformedDurations() {
        assertFalse(DurationParser.parse("", true).valid());
        assertFalse(DurationParser.parse("0", true).valid());
        assertFalse(DurationParser.parse("-1h", true).valid());
        assertFalse(DurationParser.parse("1h30", true).valid());
        assertFalse(DurationParser.parse("1h2h", true).valid());
        assertFalse(DurationParser.parse("1hour", true).valid());
        assertFalse(DurationParser.parse("10s trailing", true).valid());
    }

    @Test
    void rejectsParseAndScaleOverflow() {
        assertEquals(DurationParser.Error.OVERFLOW,
                DurationParser.parse("999999999999999999999999s", true).error());

        DurationParser.Result seconds = DurationParser.parse(String.valueOf(Long.MAX_VALUE), true);
        assertTrue(seconds.valid());
        assertEquals(DurationParser.INVALID_VALUE, DurationParser.toMilliseconds(seconds));
    }

    @Test
    void formatsAllSupportedUnits() {
        assertEquals("1w2d3h4m5s", DurationParser.humanReadable(788_645L));
    }
}
