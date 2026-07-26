package com.enviouse.sef.identity;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.commands.NicknamePolicy;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.utils.SEFUtilities;
import com.enviouse.sef.vanish.VanishUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public final class IdentityService {
    private final Supplier<MinecraftServer> serverSupplier;
    private final PlayerProfileRepository profiles;

    public IdentityService(Supplier<MinecraftServer> serverSupplier, PlayerProfileRepository profiles) {
        this.serverSupplier = Objects.requireNonNull(serverSupplier, "serverSupplier");
        this.profiles = Objects.requireNonNull(profiles, "profiles");
    }

    public ActionResult<Identity> resolve(String input, ServerPlayer viewer) {
        String normalized = NicknamePolicy.normalizeIdentity(input);
        if (normalized.isBlank()) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "identity is empty");
        }

        MinecraftServer server = serverSupplier.get();
        List<Identity> matches = new ArrayList<>();
        java.util.Set<UUID> hiddenOnlinePlayers = new java.util.HashSet<>();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (viewer != null && VanishUtil.isVanished(player, viewer)) {
                    hiddenOnlinePlayers.add(player.getUUID());
                    continue;
                }
                String username = player.getGameProfile().getName();
                String nickname = profiles.find(player.getUUID()).map(PlayerProfileRepository.Profile::nickname).orElse(null);
                if (matches(normalized, username) || matches(normalized, nickname)) {
                    matches.add(fromOnline(player, username, nickname));
                }
            }
        }
        for (PlayerProfileRepository.Profile profile : profiles.snapshot()) {
            if (hiddenOnlinePlayers.contains(profile.playerId())) {
                continue;
            }
            if (matches(normalized, profile.authenticatedUsername())
                    || matches(normalized, profile.nickname())) {
                matches.add(fromProfile(profile));
            }
        }
        List<Identity> distinct = matches.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Identity::playerId,
                        value -> value,
                        (left, right) -> left))
                .values().stream()
                .sorted(Comparator.comparing(identity -> identity.authenticatedUsername().toLowerCase(Locale.ROOT)))
                .toList();
        if (distinct.isEmpty()) {
            return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "identity not found");
        }
        if (distinct.size() > 1) {
            return ActionResult.failure(ActionResult.ReasonCode.AMBIGUOUS, "identity is ambiguous");
        }
        return ActionResult.success(distinct.getFirst());
    }

    public Identity synthetic(String username, String prefix, String suffix, String nickname) {
        String safeUsername = bounded(username, 16);
        return new Identity(
                null,
                safeUsername,
                bounded(nickname, 64),
                bounded(prefix, 128),
                bounded(suffix, 128),
                Component.literal(nickname == null || nickname.isBlank() ? safeUsername : nickname),
                Provenance.SYNTHETIC,
                false);
    }

    private Identity fromOnline(ServerPlayer player, String username, String nickname) {
        String[] metadata = ServerEssentialsForge.instance != null && ServerEssentialsForge.instance.metadataProvider != null
                ? ServerEssentialsForge.instance.metadataProvider.getPlayerPrefixAndSuffix(player.getGameProfile())
                : null;
        String prefix = metadata == null || metadata.length < 1 ? "" : metadata[0];
        String suffix = metadata == null || metadata.length < 2 ? "" : metadata[1];
        return new Identity(
                player.getUUID(),
                username,
                nickname,
                bounded(prefix, 128),
                bounded(suffix, 128),
                SEFUtilities.getFormattedPlayerName(player.getGameProfile()),
                Provenance.ONLINE,
                true);
    }

    private Identity fromProfile(PlayerProfileRepository.Profile profile) {
        String visible = profile.nickname() == null || profile.nickname().isBlank()
                ? profile.authenticatedUsername()
                : NicknamePolicy.stripFormatting(profile.nickname());
        return new Identity(
                profile.playerId(),
                profile.authenticatedUsername(),
                profile.nickname(),
                "",
                "",
                Component.literal(visible == null ? profile.playerId().toString() : visible),
                Provenance.KNOWN_OFFLINE,
                false);
    }

    private static boolean matches(String normalized, String value) {
        return normalized.equals(NicknamePolicy.normalizeIdentity(NicknamePolicy.stripFormatting(value)));
    }

    private static String bounded(String value, int maximumLength) {
        if (value == null) {
            return "";
        }
        String sanitized = value.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return sanitized.length() <= maximumLength ? sanitized : sanitized.substring(0, maximumLength);
    }

    public record Identity(
            UUID playerId,
            String authenticatedUsername,
            String nickname,
            String prefix,
            String suffix,
            Component displayName,
            Provenance provenance,
            boolean online
    ) {
        public Identity {
            authenticatedUsername = authenticatedUsername == null ? "" : authenticatedUsername;
            nickname = nickname == null ? "" : nickname;
            prefix = prefix == null ? "" : prefix;
            suffix = suffix == null ? "" : suffix;
            Objects.requireNonNull(displayName, "displayName");
            Objects.requireNonNull(provenance, "provenance");
        }
    }

    public enum Provenance {
        ONLINE,
        KNOWN_OFFLINE,
        CACHED_PROFILE,
        DEFAULTED,
        SYNTHETIC
    }
}
