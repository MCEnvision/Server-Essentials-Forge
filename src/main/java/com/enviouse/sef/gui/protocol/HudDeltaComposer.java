package com.enviouse.sef.gui.protocol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class HudDeltaComposer {
    private HudDeltaComposer() {
    }

    public static Optional<Update> compose(
            UUID sessionId,
            State previous,
            List<SefPayloads.HudTile> requested
    ) {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(requested, "requested");
        if (requested.size() > SefProtocol.MAXIMUM_HUD_TILES) {
            throw new IllegalArgumentException("HUD tile count exceeds protocol bounds");
        }
        Map<String, SefPayloads.HudTile> nextTiles = new LinkedHashMap<>();
        for (SefPayloads.HudTile tile : requested) {
            if (nextTiles.putIfAbsent(tile.id(), tile) != null) {
                throw new IllegalArgumentException("Duplicate HUD tile identifier");
            }
        }
        boolean reset = previous == null || !previous.sessionId().equals(sessionId);
        long revision = reset ? 1L : previous.revision() + 1L;
        Map<String, SefPayloads.HudTile> priorTiles = reset ? Map.of() : previous.tiles();
        List<SefPayloads.HudTile> upserts = nextTiles.values().stream()
                .filter(tile -> !tile.equals(priorTiles.get(tile.id())))
                .toList();
        List<String> removals = priorTiles.keySet().stream()
                .filter(id -> !nextTiles.containsKey(id))
                .toList();
        if (!reset && upserts.isEmpty() && removals.isEmpty()) {
            return Optional.empty();
        }
        State next = new State(sessionId, revision, nextTiles);
        return Optional.of(new Update(
                next,
                new SefPayloads.HudDelta(sessionId, revision, reset, upserts, removals)));
    }

    public record State(UUID sessionId, long revision, Map<String, SefPayloads.HudTile> tiles) {
        public State {
            Objects.requireNonNull(sessionId, "sessionId");
            tiles = java.util.Collections.unmodifiableMap(
                    new LinkedHashMap<>(Objects.requireNonNull(tiles, "tiles")));
            if (revision < 1L || tiles.size() > SefProtocol.MAXIMUM_HUD_TILES) {
                throw new IllegalArgumentException("HUD state is invalid");
            }
        }
    }

    public record Update(State state, SefPayloads.HudDelta delta) {
        public Update {
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(delta, "delta");
        }
    }
}
