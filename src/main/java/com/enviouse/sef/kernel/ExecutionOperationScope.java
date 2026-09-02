package com.enviouse.sef.kernel;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ExecutionOperationScope implements AutoCloseable {
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    private final Context previous;
    private boolean closed;

    private ExecutionOperationScope(Context context) {
        previous = CURRENT.get();
        if (previous != null && !previous.operationId().equals(context.operationId())) {
            throw new IllegalStateException("A different execution operation is already active");
        }
        CURRENT.set(context);
    }

    public static ExecutionOperationScope open(
            UUID operationId,
            String idempotencyKey,
            UUID actorId
    ) {
        return new ExecutionOperationScope(new Context(operationId, idempotencyKey, actorId));
    }

    public static Optional<Context> current() {
        return Optional.ofNullable(CURRENT.get());
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

    public record Context(
            UUID operationId,
            String idempotencyKey,
            UUID actorId
    ) {
        public Context {
            Objects.requireNonNull(operationId, "operationId");
            Objects.requireNonNull(idempotencyKey, "idempotencyKey");
            Objects.requireNonNull(actorId, "actorId");
            if (idempotencyKey.isBlank() || idempotencyKey.length() > 128) {
                throw new IllegalArgumentException("Execution idempotency key is outside bounds");
            }
        }
    }
}
