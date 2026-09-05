package com.enviouse.sef.kernel;

import java.util.Objects;
import java.util.Optional;

/**
 * Marks the synchronous domain work owned by the shared command executor.
 * Domain services use this marker to avoid emitting a second audit event for
 * the same command while retaining their direct API audit behavior.
 */
public final class CommandAuditScope implements AutoCloseable {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private final String previous;
    private boolean closed;

    private CommandAuditScope(String actionId) {
        previous = CURRENT.get();
        CURRENT.set(actionId);
    }

    public static CommandAuditScope open(String actionId) {
        return new CommandAuditScope(Objects.requireNonNull(actionId, "actionId"));
    }

    public static Optional<String> currentActionId() {
        return Optional.ofNullable(CURRENT.get());
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
}
