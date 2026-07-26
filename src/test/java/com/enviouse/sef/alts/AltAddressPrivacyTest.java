package com.enviouse.sef.alts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AltAddressPrivacyTest {
    @Test
    void saltedHashesAreDeterministicAndDoNotExposeAddresses() {
        byte[] firstSalt = new byte[32];
        byte[] secondSalt = new byte[32];
        secondSalt[0] = 1;

        String first = AltAddressPrivacy.hash("203.0.113.25", firstSalt);
        assertEquals(first, AltAddressPrivacy.hash("203.0.113.25", firstSalt));
        assertNotEquals(first, AltAddressPrivacy.hash("203.0.113.25", secondSalt));
        assertFalse(first.contains("203.0.113.25"));
        assertTrue(AltAddressPrivacy.isHashed(first));
    }

    @Test
    void displayRedactionHandlesIpv4Ipv6AndHashes() {
        assertEquals("203.0.113.x", AltAddressPrivacy.redact("203.0.113.25"));
        assertEquals("2001:db8:x", AltAddressPrivacy.redact("2001:db8:1"));
        assertEquals("sha256:abcdef123456", AltAddressPrivacy.redact("sha256:abcdef1234567890"));
        assertTrue(AltAddressPrivacy.isLocal("::1"));
    }

    @Test
    void localDetectionCoversPrivateLinkLocalAndMappedAddresses() {
        assertTrue(AltAddressPrivacy.isLocal("10.12.4.8"));
        assertTrue(AltAddressPrivacy.isLocal("172.16.9.2"));
        assertTrue(AltAddressPrivacy.isLocal("192.168.1.2"));
        assertTrue(AltAddressPrivacy.isLocal("169.254.10.3"));
        assertTrue(AltAddressPrivacy.isLocal("fe80::1%eth0"));
        assertTrue(AltAddressPrivacy.isLocal("fc00::10"));
        assertFalse(AltAddressPrivacy.isLocal("203.0.113.25"));
        assertFalse(AltAddressPrivacy.isLocal("2001:db8::1"));
    }
}
