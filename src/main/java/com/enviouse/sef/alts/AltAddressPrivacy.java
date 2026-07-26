package com.enviouse.sef.alts;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HexFormat;

public final class AltAddressPrivacy {
    private static final String HASH_PREFIX = "sha256:";

    private AltAddressPrivacy() {
    }

    public static String hash(String address, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hashed = digest.digest(address.getBytes(StandardCharsets.UTF_8));
            return HASH_PREFIX + HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public static boolean isHashed(String address) {
        return address != null && address.startsWith(HASH_PREFIX);
    }

    public static String redact(String address) {
        if (address == null || address.isBlank()) return "unknown";
        if (isHashed(address)) {
            int end = Math.min(address.length(), HASH_PREFIX.length() + 12);
            return address.substring(0, end);
        }
        int lastDot = address.lastIndexOf('.');
        if (lastDot > 0) return address.substring(0, lastDot) + ".x";
        int lastColon = address.lastIndexOf(':');
        if (lastColon > 0) return address.substring(0, lastColon) + ":x";
        return "redacted";
    }

    public static boolean isLocal(String address) {
        if (address == null || address.isBlank()) return true;
        String normalized = address.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.equals("local") || normalized.equals("localhost")) return true;
        int zone = normalized.indexOf('%');
        if (zone >= 0) normalized = normalized.substring(0, zone);
        try {
            InetAddress parsed = InetAddress.getByName(normalized);
            byte[] bytes = parsed.getAddress();
            boolean uniqueLocalIpv6 = bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
            return parsed.isAnyLocalAddress()
                    || parsed.isLoopbackAddress()
                    || parsed.isLinkLocalAddress()
                    || parsed.isSiteLocalAddress()
                    || uniqueLocalIpv6;
        } catch (UnknownHostException ignored) {
            return false;
        }
    }
}
