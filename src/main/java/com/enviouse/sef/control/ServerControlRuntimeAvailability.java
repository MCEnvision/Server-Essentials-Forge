package com.enviouse.sef.control;

import java.util.List;
import java.util.Locale;
import java.util.Set;

final class ServerControlRuntimeAvailability {
    private static final Set<String> UNAVAILABLE = Set.of(
            "resource_governor",
            "chat_channels",
            "afk_zones",
            "resource_worlds",
            "admin_journal",
            "rollouts",
            "waypoints",
            "portal_policy",
            "staff_duty",
            "approvals",
            "capability_leases",
            "server_presentation",
            "spawn_ecology",
            "display_profiles",
            "display_ownership",
            "player_warp_review");

    private ServerControlRuntimeAvailability() {
    }

    static boolean unavailable(String featureId) {
        return UNAVAILABLE.contains(featureId.trim().toLowerCase(Locale.ROOT));
    }

    static List<String> features() {
        return UNAVAILABLE.stream().sorted().toList();
    }
}
