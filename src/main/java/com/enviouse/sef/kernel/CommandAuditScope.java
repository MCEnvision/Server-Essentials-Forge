package com.enviouse.sef.kernel;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Marks the synchronous domain work owned by the shared command executor.
 * Domain services use this marker to avoid emitting a second audit event for
 * the same command while retaining their direct API audit behavior.
 */
public final class CommandAuditScope implements AutoCloseable {
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    private final Context previous;
    private boolean closed;

    private CommandAuditScope(String actionId, UUID correlationId) {
        previous = CURRENT.get();
        CURRENT.set(new Context(actionId, correlationId));
    }

    static CommandAuditScope open(String actionId) {
        return open(actionId, null);
    }

    static CommandAuditScope open(String actionId, UUID correlationId) {
        return new CommandAuditScope(Objects.requireNonNull(actionId, "actionId"), correlationId);
    }

    public static Optional<String> currentActionId() {
        return Optional.ofNullable(CURRENT.get()).map(Context::actionId);
    }

    public static Optional<UUID> currentCorrelationId() {
        return Optional.ofNullable(CURRENT.get()).map(Context::correlationId);
    }

    public static boolean active() {
        return CURRENT.get() != null;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previous);
        }
    }

    private record Context(String actionId, UUID correlationId) {
    }
}
