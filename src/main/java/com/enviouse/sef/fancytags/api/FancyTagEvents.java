package com.enviouse.sef.fancytags.api;

import com.enviouse.sef.ServerEssentialsForge;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class FancyTagEvents {
    private static final int MAXIMUM_LISTENERS = 128;
    private static final List<Consumer<LifecycleEvent>> LISTENERS = new CopyOnWriteArrayList<>();

    private FancyTagEvents() {
    }

    public static AutoCloseable subscribe(Consumer<LifecycleEvent> listener) {
        Objects.requireNonNull(listener, "listener");
        if (LISTENERS.size() >= MAXIMUM_LISTENERS) {
            throw new IllegalStateException("Fancy Tags listener capacity is exhausted");
        }
        LISTENERS.add(listener);
        return () -> LISTENERS.remove(listener);
    }

    public static void publish(LifecycleEvent event) {
        Objects.requireNonNull(event, "event");
        for (Consumer<LifecycleEvent> listener : LISTENERS) {
            try {
                listener.accept(event);
            } catch (RuntimeException exception) {
                ServerEssentialsForge.LOGGER.error(
                        "Fancy Tags lifecycle listener failed for {}",
                        event.type(),
                        exception);
            }
        }
    }

    public record LifecycleEvent(
            Type type,
            UUID actorId,
            UUID tagId,
            UUID assignmentId,
            long revision,
            Instant occurredAt
    ) {
        public LifecycleEvent {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(occurredAt, "occurredAt");
            if (revision < 1L) {
                throw new IllegalArgumentException("Fancy Tags event revision is invalid");
            }
        }
    }

    public enum Type {
        CREATED,
        UPDATED,
        ARTWORK_IMPORTED,
        STATUS_CHANGED,
        ASSIGNED,
        UNASSIGNED,
        DELETED,
        CACHE_INVALIDATED
    }
}
