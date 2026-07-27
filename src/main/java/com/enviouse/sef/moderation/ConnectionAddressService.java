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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ConnectionAddressService {
    private static final int MAXIMUM_ADAPTERS = 32;
    private static final List<RegisteredAdapter> ADAPTERS = new ArrayList<>();

    private final byte[] fingerprintKey = new byte[32];
    private final ProviderMode mode;
    private volatile String adapterFailure = "";
    private volatile String failedAdapterId = "";

    public ConnectionAddressService() {
        this(ProviderMode.parse(ConfigHandler.config.moderationAddressProvider.get()));
    }

    public ConnectionAddressService(ProviderMode mode) {
        new SecureRandom().nextBytes(fingerprintKey);
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    public ProviderMode mode() {
        return mode;
    }

    public Health health(MinecraftServer server) {
        if (mode == ProviderMode.DISABLED) {
            return new Health(mode, false, false, "address operations are disabled");
        }
        RegisteredAdapter adapter = mode == ProviderMode.DIRECT ? null : activeAdapter(mode);
        if (mode != ProviderMode.DIRECT && adapter == null) {
            return new Health(mode, false, false, "the selected address provider has no active adapter");
        }
        if (adapter != null && !adapter.id().equals(failedAdapterId)) {
            adapterFailure = "";
            failedAdapterId = "";
        }
        if (!adapterFailure.isBlank()) {
            return new Health(mode, false, false, adapterFailure);
        }
        int maximumShared = server == null ? 0 : maximumSharedSessions(server);
        if (!adapterFailure.isBlank()) {
            return new Health(mode, false, false, adapterFailure);
        }
        int cap = ConfigHandler.config.moderationSharedAddressHardCap.get();
        boolean sharedHazard = !sharedActionSafe(
                0,
                maximumShared,
                cap,
                ConfigHandler.config.moderationFailOnSharedProxy.get());
        String providerDetail = mode == ProviderMode.DIRECT
                ? "direct socket provider is healthy"
                : "address adapter " + adapter.id() + " is healthy";
        return new Health(mode, !sharedHazard, sharedHazard,
                sharedHazard ? "a shared address exceeds the configured hard cap" : providerDetail);
    }

    public Optional<Address> forPlayer(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        if (mode == ProviderMode.DISABLED) {
            return Optional.empty();
        }
        if (mode == ProviderMode.DIRECT) {
            SocketAddress remote = player.connection.getRemoteAddress();
            if (!(remote instanceof InetSocketAddress socket) || socket.getAddress() == null) {
                return Optional.empty();
            }
            return Optional.of(address(socket.getAddress()));
        }
        RegisteredAdapter adapter = activeAdapter(mode);
        if (adapter == null) {
            return Optional.empty();
        }
        try {
            Optional<ProvidedAddress> supplied = adapter.adapter().resolve(player);
            if (supplied == null || supplied.isEmpty()) {
                failedAdapterId = adapter.id();
                adapterFailure = "address adapter " + adapter.id() + " did not resolve an online session";
                return Optional.empty();
            }
            Address resolved = address(InetAddress.getByAddress(supplied.orElseThrow().bytes()));
            adapterFailure = "";
            failedAdapterId = "";
            return Optional.of(resolved);
        } catch (RuntimeException | LinkageError | UnknownHostException exception) {
            failedAdapterId = adapter.id();
            adapterFailure = "address adapter " + adapter.id() + " failed";
            return Optional.empty();
        }
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
        if (!providerActive()) {
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
        if (!providerActive()) {
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

    private boolean providerActive() {
        return mode == ProviderMode.DIRECT
                || mode != ProviderMode.DISABLED && activeAdapter(mode) != null;
    }

    public static synchronized boolean registerAdapter(Adapter adapter) {
        if (adapter == null || ADAPTERS.size() >= MAXIMUM_ADAPTERS) {
            return false;
        }
        final String id;
        final ProviderMode mode;
        final int priority;
        try {
            id = normalizeAdapterId(adapter.id());
            mode = Objects.requireNonNull(adapter.mode(), "mode");
            priority = adapter.priority();
        } catch (RuntimeException | LinkageError exception) {
            return false;
        }
        if (id == null || mode == ProviderMode.DIRECT || mode == ProviderMode.DISABLED
                || priority < -10_000 || priority > 10_000
                || ADAPTERS.stream().anyMatch(existing -> existing.id().equals(id))) {
            return false;
        }
        ADAPTERS.add(new RegisteredAdapter(id, mode, priority, adapter));
        ADAPTERS.sort(Comparator.comparingInt(RegisteredAdapter::priority)
                .reversed()
                .thenComparing(RegisteredAdapter::id));
        return true;
    }

    public static synchronized boolean unregisterAdapter(String id) {
        String normalized = normalizeAdapterId(id);
        return normalized != null && ADAPTERS.removeIf(adapter -> adapter.id().equals(normalized));
    }

    private static synchronized RegisteredAdapter activeAdapter(ProviderMode mode) {
        return ADAPTERS.stream()
                .filter(adapter -> adapter.mode() == mode)
                .findFirst()
                .orElse(null);
    }

    private static String normalizeAdapterId(String id) {
        if (id == null) {
            return null;
        }
        String normalized = id.strip().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || normalized.length() > 128
                || normalized.codePoints().anyMatch(Character::isISOControl)
                || !normalized.matches("[a-z0-9_.:-]+")) {
            return null;
        }
        return normalized;
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

        public static ProviderMode parse(String value) {
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

    public interface Adapter {
        String id();

        ProviderMode mode();

        int priority();

        Optional<ProvidedAddress> resolve(ServerPlayer player);
    }

    public record ProvidedAddress(byte[] bytes) {
        public ProvidedAddress {
            bytes = Objects.requireNonNull(bytes, "bytes").clone();
            if (bytes.length != 4 && bytes.length != 16) {
                throw new IllegalArgumentException("Address length is invalid");
            }
        }

        @Override
        public byte[] bytes() {
            return bytes.clone();
        }
    }

    private record RegisteredAdapter(String id, ProviderMode mode, int priority, Adapter adapter) {
    }
}
