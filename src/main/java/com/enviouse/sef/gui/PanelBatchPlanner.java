package com.enviouse.sef.gui;

import com.enviouse.sef.kernel.command.PanelContracts;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PanelBatchPlanner {
    private final int sameTickMaximum;
    private final int absoluteMaximum;
    private final int pacedTargetsPerTick;
    private final Set<String> sameTickAllowlist;

    public PanelBatchPlanner(
            int sameTickMaximum,
            int absoluteMaximum,
            int pacedTargetsPerTick,
            Set<String> sameTickAllowlist
    ) {
        if (sameTickMaximum < 1 || sameTickMaximum > 32
                || absoluteMaximum < sameTickMaximum || absoluteMaximum > 512
                || pacedTargetsPerTick < 1 || pacedTargetsPerTick > 64) {
            throw new IllegalArgumentException("Panel batch limits are outside hard bounds");
        }
        this.sameTickMaximum = sameTickMaximum;
        this.absoluteMaximum = absoluteMaximum;
        this.pacedTargetsPerTick = pacedTargetsPerTick;
        this.sameTickAllowlist = Set.copyOf(Objects.requireNonNull(sameTickAllowlist, "sameTickAllowlist"));
    }

    public Decision plan(
            String actionId,
            PanelContracts.ExecutionContext context,
            List<UUID> requestedTargets,
            boolean broadAudienceAuthorized,
            boolean executionContextAuthorized
    ) {
        String normalizedAction = Objects.requireNonNull(actionId, "actionId").trim().toLowerCase(java.util.Locale.ROOT);
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(requestedTargets, "requestedTargets");
        if (context != PanelContracts.ExecutionContext.NATIVE_BULK
                && context != PanelContracts.ExecutionContext.AS_EACH_PARTICIPANT) {
            return Decision.denied("execution context is not a batch context");
        }
        if (!broadAudienceAuthorized || !executionContextAuthorized) {
            return Decision.denied("missing exact audience or execution context permission");
        }
        LinkedHashSet<UUID> unique = new LinkedHashSet<>();
        for (UUID target : requestedTargets) {
            if (target == null) {
                return Decision.denied("target set contains an invalid identity");
            }
            unique.add(target);
            if (unique.size() > absoluteMaximum) {
                return Decision.denied("target cohort exceeds the hard limit");
            }
        }
        if (unique.isEmpty()) {
            return Decision.denied("target cohort is empty");
        }
        List<UUID> frozen = List.copyOf(unique);
        if (frozen.size() <= sameTickMaximum && sameTickAllowlist.contains(normalizedAction)) {
            return new Decision(true, Mode.SAME_TICK, frozen, frozen.size(), "small allowlisted frozen cohort");
        }
        int ticks = Math.max(1, (frozen.size() + pacedTargetsPerTick - 1) / pacedTargetsPerTick);
        return new Decision(true, Mode.PACED, frozen, ticks, "bounded paced job");
    }

    public record Decision(boolean accepted, Mode mode, List<UUID> frozenTargets, int estimatedTicks, String detail) {
        public Decision {
            Objects.requireNonNull(mode, "mode");
            frozenTargets = List.copyOf(Objects.requireNonNull(frozenTargets, "frozenTargets"));
            detail = Objects.requireNonNull(detail, "detail").trim();
        }

        private static Decision denied(String detail) {
            return new Decision(false, Mode.DENIED, List.of(), 0, detail);
        }
    }

    public enum Mode {
        DENIED,
        SAME_TICK,
        PACED
    }
}
