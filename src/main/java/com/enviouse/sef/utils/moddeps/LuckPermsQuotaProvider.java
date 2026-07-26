package com.enviouse.sef.utils.moddeps;

import com.enviouse.sef.kernel.policy.QuotaService;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class LuckPermsQuotaProvider implements QuotaService.Provider {
    private static final Map<String, String> METADATA_KEYS = Map.of(
            "sef:homes", "sef.limit.homes.total",
            "sef:player_warps", "sef.limit.player_warps.total",
            "sef:targets", "sef.limit.targets",
            "sef:mail", "sef.limit.mail",
            "sef:definitions", "sef.limit.definitions");

    private final LuckPerms luckPerms;

    public LuckPermsQuotaProvider(LuckPerms luckPerms) {
        this.luckPerms = Objects.requireNonNull(luckPerms, "luckPerms");
    }

    @Override
    public String id() {
        return "luckperms_metadata";
    }

    @Override
    public int priority() {
        return 200;
    }

    @Override
    public QuotaService.Candidate resolve(
            QuotaService.Definition definition,
            QuotaService.Context context
    ) {
        User user = luckPerms.getUserManager().getUser(context.subjectId());
        if (user == null) {
            return null;
        }
        CachedMetaData metadata = user.getCachedData().getMetaData();
        String key = METADATA_KEYS.getOrDefault(definition.id(), genericKey(definition.id()));
        String raw = metadata.getMetaValue(key);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("unlimited")) {
            return definition.allowUnlimited()
                    ? QuotaService.Candidate.unlimited(id(), key)
                    : null;
        }
        try {
            long parsed = Long.parseLong(normalized);
            return parsed < 0L ? null : QuotaService.Candidate.finite(parsed, id(), key);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    static String genericKey(String quotaId) {
        String normalized = quotaId.startsWith("sef:") ? quotaId.substring(4) : quotaId;
        return "sef.limit." + normalized.replace('_', '.').replace(':', '.');
    }
}
