package com.enviouse.sef.utils.moddeps;

import com.enviouse.sef.fancytags.FancyTagGroupResolver;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class LuckPermsFancyTagGroupProvider implements FancyTagGroupResolver.Provider {
    private final LuckPerms luckPerms;

    public LuckPermsFancyTagGroupProvider(LuckPerms luckPerms) {
        this.luckPerms = Objects.requireNonNull(luckPerms, "luckPerms");
    }

    @Override
    public String id() {
        return "luckperms";
    }

    @Override
    public Set<String> groups(UUID playerId) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) {
            return Set.of();
        }
        QueryOptions options = luckPerms.getContextManager()
                .getQueryOptions(user)
                .orElseGet(QueryOptions::defaultContextualOptions);
        Set<String> groups = new LinkedHashSet<>();
        groups.add(user.getPrimaryGroup());
        user.getInheritedGroups(options).forEach(group -> groups.add(group.getName()));
        return Set.copyOf(groups);
    }
}
