package com.enviouse.sef.kernel.policy;

import com.enviouse.sef.kernel.ActionResult;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Map;
import java.util.UUID;

public interface CostService {
    String providerId();

    ActionResult<Reservation> reserve(UUID actorId, String actionId, BigDecimal amount);

    default ActionResult<Reservation> reserve(
            UUID actorId,
            String actionId,
            BigDecimal amount,
            UUID operationId
    ) {
        Objects.requireNonNull(operationId, "operationId");
        return reserve(actorId, actionId, amount);
    }

    interface Reservation extends AutoCloseable {
        BigDecimal amount();

        ActionResult<Void> commit();

        ActionResult<Void> refund();

        default Map<String, String> auditContext() {
            return Map.of("cost_amount", amount().toPlainString());
        }

        @Override
        void close();
    }

    final class Disabled implements CostService {
        @Override
        public String providerId() {
            return "disabled";
        }

        @Override
        public ActionResult<Reservation> reserve(UUID actorId, String actionId, BigDecimal amount) {
            Objects.requireNonNull(actorId, "actorId");
            Objects.requireNonNull(actionId, "actionId");
            Objects.requireNonNull(amount, "amount");
            if (amount.signum() < 0) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "cost cannot be negative");
            }
            if (amount.signum() == 0) {
                return ActionResult.success(new FreeReservation());
            }
            return ActionResult.failure(ActionResult.ReasonCode.COST_UNAVAILABLE, "economy provider is disabled");
        }
    }

    final class FreeReservation implements Reservation {
        private boolean closed;

        @Override
        public BigDecimal amount() {
            return BigDecimal.ZERO;
        }

        @Override
        public synchronized ActionResult<Void> commit() {
            if (closed) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "cost reservation is closed");
            }
            closed = true;
            return ActionResult.success(null);
        }

        @Override
        public synchronized ActionResult<Void> refund() {
            closed = true;
            return ActionResult.success(null);
        }

        @Override
        public synchronized void close() {
            closed = true;
        }
    }
}
