package com.enviouse.sef.moderation;

import com.enviouse.sef.config.ConfigHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ConnectionAddressService {
    private final byte[] fingerprintKey = new byte[32];
    private final ProviderMode mode;

    public ConnectionAddressService() {
        new SecureRandom().nextBytes(fingerprintKey);
        mode = ProviderMode.parse(ConfigHandler.config.moderationAddressProvider.get());
    }

    public ProviderMode mode() {
        return mode;
    }

    public Health health(MinecraftServer server) {
        if (mode == ProviderMode.DISABLED) {
            return new Health(mode, false, false, "address operations are disabled");
        }
        if (mode != ProviderMode.DIRECT) {
            return new Health(mode, false, false, "the selected address provider has no active adapter");
        }
        int maximumShared = server == null ? 0 : maximumSharedSessions(server);
        int cap = ConfigHandler.config.moderationSharedAddressHardCap.get();
        boolean sharedHazard = !sharedActionSafe(
                0,
                maximumShared,
                cap,
                ConfigHandler.config.moderationFailOnSharedProxy.get());
        return new Health(mode, !sharedHazard, sharedHazard,
                sharedHazard ? "a shared address exceeds the configured hard cap" : "direct socket provider is healthy");
    }

    public Optional<Address> forPlayer(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        if (mode != ProviderMode.DIRECT) {
            return Optional.empty();
        }
        SocketAddress remote = player.connection.getRemoteAddress();
        if (!(remote instanceof InetSocketAddress socket) || socket.getAddress() == null) {
            return Optional.empty();
        }
        return Optional.of(address(socket.getAddress()));
    }

    public Optional<Address> literal(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String candidate = input.strip();
        if (candidate.startsWith("[") && candidate.endsWith("]")) {
            candidate = candidate.substring(1, candidate.length() - 1);
        }
        if (candidate.isBlank() || candidate.length() > 64 || candidate.indexOf('%') >= 0) {
            return Optional.empty();
        }
        try {
            InetAddress parsed;
            if (candidate.indexOf(':') >= 0) {
                parsed = InetAddress.getByName(candidate);
                if (!(parsed instanceof Inet6Address)) {
                    return Optional.empty();
                }
            } else {
                byte[] bytes = parseIpv4(candidate);
                if (bytes == null) {
                    return Optional.empty();
                }
                parsed = InetAddress.getByAddress(bytes);
            }
            return Optional.of(address(parsed));
        } catch (UnknownHostException exception) {
            return Optional.empty();
        }
    }

    public List<Session> sessions(MinecraftServer server, Address address) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(address, "address");
        if (mode != ProviderMode.DIRECT) {
            return List.of();
        }
        List<Session> matches = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Optional<Address> candidate = forPlayer(player);
            if (candidate.isPresent() && MessageDigest.isEqual(candidate.orElseThrow().bytes(), address.bytes())) {
                matches.add(new Session(player, candidate.orElseThrow()));
            }
        }
        return List.copyOf(matches);
    }

    public boolean safeForSharedAction(MinecraftServer server, Address address) {
        if (mode != ProviderMode.DIRECT) {
            return false;
        }
        int count = sessions(server, address).size();
        int cap = ConfigHandler.config.moderationSharedAddressHardCap.get();
        return sharedActionSafe(
                count,
                maximumSharedSessions(server),
                cap,
                ConfigHandler.config.moderationFailOnSharedProxy.get());
    }

    static boolean sharedActionSafe(int targetCount, int maximumShared, int cap, boolean failOnSharedProxy) {
        if (targetCount < 0 || maximumShared < 0 || cap < 1
                || targetCount > cap || maximumShared > cap) {
            return false;
        }
        return !failOnSharedProxy || maximumShared <= 1;
    }

    private int maximumSharedSessions(MinecraftServer server) {
        Map<String, Integer> counts = new HashMap<>();
        int maximum = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Optional<Address> address = forPlayer(player);
            if (address.isPresent()) {
                int count = counts.merge(address.orElseThrow().normalized(), 1, Integer::sum);
                maximum = Math.max(maximum, count);
            }
        }
        return maximum;
    }

    private Address address(InetAddress value) {
        byte[] bytes = value.getAddress().clone();
        String normalized = value.getHostAddress().toLowerCase(Locale.ROOT);
        int zone = normalized.indexOf('%');
        if (zone >= 0) {
            normalized = normalized.substring(0, zone);
        }
        return new Address(bytes, normalized, fingerprint(bytes));
    }

    private String fingerprint(byte[] address) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(fingerprintKey);
            digest.update(ByteBuffer.allocate(4).putInt(address.length).array());
            digest.update(address);
            return HexFormat.of().formatHex(digest.digest(), 0, 8);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static byte[] parseIpv4(String value) {
        String[] segments = value.split("\\.", -1);
        if (segments.length != 4) {
            return null;
        }
        byte[] bytes = new byte[4];
        for (int index = 0; index < segments.length; index++) {
            String segment = segments[index];
            if (segment.isBlank() || segment.length() > 3
                    || segment.length() > 1 && segment.charAt(0) == '0') {
                return null;
            }
            int parsed;
            try {
                parsed = Integer.parseInt(segment);
            } catch (NumberFormatException exception) {
                return null;
            }
            if (parsed < 0 || parsed > 255) {
                return null;
            }
            bytes[index] = (byte) parsed;
        }
        return bytes;
    }

    public enum ProviderMode {
        DIRECT,
        TRUSTED_PROXY,
        EXTERNAL,
        DISABLED;

        static ProviderMode parse(String value) {
            return switch (value == null ? "" : value.strip().toLowerCase(Locale.ROOT)) {
                case "direct", "direct_socket" -> DIRECT;
                case "trusted_proxy", "trusted-proxy" -> TRUSTED_PROXY;
                case "external" -> EXTERNAL;
                default -> DISABLED;
            };
        }
    }

    public record Address(byte[] bytes, String normalized, String fingerprint) {
        public Address {
            bytes = bytes.clone();
            if (bytes.length != 4 && bytes.length != 16) {
                throw new IllegalArgumentException("Address length is invalid");
            }
            Objects.requireNonNull(normalized, "normalized");
            Objects.requireNonNull(fingerprint, "fingerprint");
        }

        @Override
        public byte[] bytes() {
            return bytes.clone();
        }

        public String redacted() {
            return "address " + fingerprint;
        }
    }

    public record Session(ServerPlayer player, Address address) {
    }

    public record Health(ProviderMode mode, boolean available, boolean sharedAddressHazard, String detail) {
    }
}
