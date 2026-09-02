package com.enviouse.sef.economy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandCostScheduleTest {
    @Test
    void quotesEverySupportedCostComponent() {
        CommandCostSchedule schedule = CommandCostSchedule.parse(
                "sef:test=1.00,"
                        + "sef:test@use=0.25,"
                        + "sef:test@target=2.00,"
                        + "sef:test@distance=0.10,"
                        + "sef:test@item=0.50",
                2,
                1_000_000L);

        BigDecimal quote = schedule.quote(
                "sef:test",
                Map.of("distance", "12.1", "amount", "3"),
                List.of(UUID.randomUUID(), UUID.randomUUID()));

        assertEquals(new BigDecimal("8.05"), quote);
        assertEquals(new BigDecimal("1.25"), schedule.cost("sef:test"));
    }

    @Test
    void rejectsDuplicateComponentsAndOversizedQuotes() {
        assertThrows(IllegalArgumentException.class, () -> CommandCostSchedule.parse(
                "sef:test@target=1.00,sef:test@per_target=2.00",
                2,
                10_000L));

        CommandCostSchedule schedule = CommandCostSchedule.parse(
                "sef:test@item=100.00",
                2,
                10_000L);
        assertThrows(IllegalArgumentException.class, () -> schedule.quote(
                "sef:test",
                Map.of("amount", "2"),
                List.of()));
    }
}
