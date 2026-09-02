package com.enviouse.sef.economy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EconomyMoneyTest {
    @Test
    void parsesPlainDecimalIntoMinorUnitsWithoutFloatingPoint() {
        assertEquals(12345L, EconomyMoney.parsePositive("123.45", 2, 1_000_000L));
        assertEquals(new BigDecimal("123.45"), EconomyMoney.toMajorUnits(12345L, 2));
        assertEquals("$123.45", EconomyMoney.format(12345L, 2, "$"));
    }

    @Test
    void rejectsExponentExcessPrecisionNegativeAndOverflow() {
        assertThrows(IllegalArgumentException.class,
                () -> EconomyMoney.parsePositive("1e3", 2, Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class,
                () -> EconomyMoney.parsePositive("1.001", 2, Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class,
                () -> EconomyMoney.parsePositive("-1", 2, Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class,
                () -> EconomyMoney.parsePositive("999999999999999999999999", 2, Long.MAX_VALUE));
    }

    @Test
    void multiplicationIsBoundedAndExact() {
        assertEquals(6400L, EconomyMoney.multiply(100L, 64, 10_000L));
        assertThrows(IllegalArgumentException.class, () -> EconomyMoney.multiply(100L, 101, 10_000L));
        assertThrows(IllegalArgumentException.class, () -> EconomyMoney.multiply(Long.MAX_VALUE, 2, Long.MAX_VALUE));
    }
}
