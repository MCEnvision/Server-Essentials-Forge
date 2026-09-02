package com.enviouse.sef.message;

import com.enviouse.sef.kernel.ActionResult;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageServiceTest {
    private final MessageService messages = new MessageService();

    @Test
    void placeholderValuesRemainTypedAndAreNotReparsed() {
        MessageService.Template template = messages.compile(
                "&7[{from}] {message} after",
                Set.of("from", "message")).value();

        ActionResult<Component> rendered = messages.render(template, Map.of(
                "from", Component.literal("Alice"),
                "message", Component.literal("&cboom {from}")));

        assertTrue(rendered.successful());
        assertEquals("[Alice] &cboom {from} after", rendered.value().getString());
    }

    @Test
    void unknownAndMissingPlaceholdersFailClosed() {
        assertFalse(messages.compile("{secret}", Set.of("message")).successful());
        MessageService.Template template = messages.compile(
                "{from} {message}",
                Set.of("from", "message")).value();

        ActionResult<Component> rendered =
                messages.render(template, Map.of("from", Component.literal("Alice")));

        assertFalse(rendered.successful());
        assertEquals(ActionResult.ReasonCode.INVALID_INPUT, rendered.reason());
    }

    @Test
    void compiledTemplatesAreImmutableSnapshots() {
        MessageService.Template first = messages.compile("{message}", Set.of("message")).value();
        MessageService.Template second = messages.compile("&c{message}", Set.of("message")).value();

        assertEquals("{message}", first.source());
        assertEquals("&c{message}", second.source());
        assertEquals(1, first.segments().size());
    }

    @Test
    void defaultConnectionTemplatesUseOnlySupportedPlaceholders() {
        assertTrue(messages.compile(
                "&e{player} joined the game",
                Set.of("player", "username", "uuid", "world")).successful());
        assertTrue(messages.compile(
                "&e{player} left the game",
                Set.of("player", "username", "uuid", "world")).successful());
    }

    @Test
    void templateAndRenderedOutputBoundsFailClosed() {
        assertFalse(messages.compile("x".repeat(4097), Set.of()).successful());
        MessageService.Template template = messages.compile(
                "{message}",
                Set.of("message")).value();

        assertFalse(messages.render(template, Map.of(
                "message", Component.literal("x".repeat(16_385)))).successful());
    }
}
