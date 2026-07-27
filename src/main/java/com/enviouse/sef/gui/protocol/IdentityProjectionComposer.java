package com.enviouse.sef.gui.protocol;

import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class IdentityProjectionComposer {
    private IdentityProjectionComposer() {
    }

    public static Optional<Update> compose(
            UUID sessionId,
            State previous,
            Map<UUID, Component> requested
    ) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(requested, "requested");
        if (requested.size() > SefProtocol.MAXIMUM_IDENTITY_PROJECTIONS) {
            throw new IllegalArgumentException("Identity projection count exceeds protocol bounds");
        }
        boolean reset = previous == null || !previous.sessionId().equals(sessionId);
        long revision = reset ? 1L : previous.revision() + 1L;
        Map<UUID, SefPayloads.ProjectedIdentity> prior =
                reset ? Map.of() : previous.identities();
        Map<UUID, SefPayloads.ProjectedIdentity> next = new LinkedHashMap<>();
        for (Map.Entry<UUID, Component> entry : requested.entrySet()) {
            UUID playerId = Objects.requireNonNull(entry.getKey(), "playerId");
            Component displayName = Objects.requireNonNull(entry.getValue(), "displayName");
            SefPayloads.ProjectedIdentity existing = prior.get(playerId);
            long targetRevision = existing == null
                    ? 1L
                    : existing.displayName().equals(displayName)
                    ? existing.revision()
                    : existing.revision() + 1L;
            next.put(playerId, new SefPayloads.ProjectedIdentity(
                    playerId,
                    targetRevision,
                    displayName));
        }
        List<SefPayloads.ProjectedIdentity> upserts = next.values().stream()
                .filter(identity -> !identity.equals(prior.get(identity.playerId())))
                .toList();
        List<UUID> removals = prior.keySet().stream()
                .filter(playerId -> !next.containsKey(playerId))
                .toList();
        if (!reset && upserts.isEmpty() && removals.isEmpty()) {
            return Optional.empty();
        }
        State nextState = new State(sessionId, revision, next);
        return Optional.of(new Update(
                nextState,
                new SefPayloads.IdentityProjection(
                        sessionId,
                        revision,
                        reset,
                        upserts,
                        removals)));
    }

    public record State(
            UUID sessionId,
            long revision,
            Map<UUID, SefPayloads.ProjectedIdentity> identities
    ) {
        public State {
            Objects.requireNonNull(sessionId, "sessionId");
            identities = java.util.Collections.unmodifiableMap(
                    new LinkedHashMap<>(Objects.requireNonNull(identities, "identities")));
            if (revision < 1L || identities.size() > SefProtocol.MAXIMUM_IDENTITY_PROJECTIONS) {
                throw new IllegalArgumentException("Identity projection state is invalid");
            }
        }
    }

    public record Update(State state, SefPayloads.IdentityProjection payload) {
        public Update {
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(payload, "payload");
        }
    }
}
