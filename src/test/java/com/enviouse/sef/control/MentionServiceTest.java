package com.enviouse.sef.control;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MentionServiceTest {
    @Test
    void parserNormalizesAndDeduplicatesBoundedMentions() {
        assertEquals(
                Set.of("notch", "herobrine"),
                MentionService.parse("@Notch hello @notch and @Herobrine"));
    }

    @Test
    void parserRejectsEmbeddedAndOversizedNames() {
        assertFalse(MentionService.parse("mail@example.com @this_name_is_far_too_long_for_the_bound").contains("example"));
        assertEquals(Set.of(), MentionService.parse("plain chat"));
    }
}
