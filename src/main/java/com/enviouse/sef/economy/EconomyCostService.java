package com.enviouse.sef.economy;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.policy.CostService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Map;
import java.util.UUID;

public final class EconomyCostService implements CostService {
    private final EconomyService economy;

    public EconomyCostService(EconomyService economy) {
        this.economy = Objects.requireNonNull(economy, "economy");
    }

    @Override
    public String providerId() {
        return economy.provider().map(EconomyProvider::id).orElse("unavailable");
    }

    @Override
    public ActionResult<Reservation> reserve(UUID actorId, String actionId, BigDecimal amount) {
        return reserve(actorId, actionId, amount, UUID.randomUUID());
    }

    @Override
    public ActionResult<Reservation> reserve(
            UUID actorId,
            String actionId,
            BigDecimal amount,
            UUID operationId
    ) {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(operationId, "operationId");
        if (amount.signum() < 0) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, "cost cannot be negative");
        }
        if (amount.signum() == 0) {
            return ActionResult.success(new CostService.FreeReservation());
        }
        EconomyProvider provider = economy.provider().orElse(null);
        if (provider == null) {
            return ActionResult.failure(ActionResult.ReasonCode.COST_UNAVAILABLE, "economy provider is unavailable");
        }
        final long minor;
        try {
            minor = EconomyMoney.toMinorUnits(
                    amount,
                    provider.minorUnits(),
                    1L,
                    economy.settings().maximumTransaction());
        } catch (IllegalArgumentException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_INPUT, exception.getMessage());
        }
        if (!(provider instanceof EconomyRepository repository)) {
            if (!(provider instanceof EconomyProvider.CostReservationProvider reservations)) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.COST_UNAVAILABLE,
                        "external economy provider does not expose crash safe reservations");
            }
            ActionResult<EconomyProvider.EconomyCostReservation> external =
                    reservations.reserveCost(new EconomyProvider.CostRequest(
                            "command.cost." + operationId,
                            actorId,
                            actionId,
                            provider.currency(),
                            minor,
                            Map.of("action", actionId)));
            if (!external.successful()) {
                return ActionResult.failure(external.reason(), external.detail());
            }
            EconomyProvider.EconomyCostReservation reservation = external.value();
            if (reservation == null
                    || reservation.reservationId() == null
                    || reservation.transactionId() == null
                    || reservation.amount() != minor) {
                if (reservation != null) {
                    reservation.refund();
                }
                return ActionResult.failure(
                        ActionResult.ReasonCode.COST_UNAVAILABLE,
                        "external economy reservation contract is invalid");
            }
            return ActionResult.success(new ExternalReservation(
                    reservation,
                    provider.minorUnits()));
        }
        try {
            EconomyRepository.CostHold hold = repository.reserveCost(
                    operationId,
                    actorId,
                    actionId,
                    minor);
            try {
                repository.flush();
            } catch (IOException exception) {
                repository.refundCost(hold.reservationId());
                try {
                    repository.flush();
                } catch (IOException rollbackFailure) {
                    exception.addSuppressed(rollbackFailure);
                }
                return ActionResult.failure(
                        ActionResult.ReasonCode.COST_UNAVAILABLE,
                        "cost reservation could not be persisted");
            }
            return ActionResult.success(new NativeReservation(repository, hold));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return ActionResult.failure(ActionResult.ReasonCode.COST_UNAVAILABLE, exception.getMessage());
        }
    }

    private static final class ExternalReservation implements Reservation {
        private final EconomyProvider.EconomyCostReservation reservation;
        private final int minorUnits;
        private boolean closed;

        private ExternalReservation(
                EconomyProvider.EconomyCostReservation reservation,
                int minorUnits
        ) {
            this.reservation = reservation;
            this.minorUnits = minorUnits;
        }

        @Override
        public BigDecimal amount() {
            return EconomyMoney.toMajorUnits(reservation.amount(), minorUnits);
        }

        @Override
        public synchronized ActionResult<Void> commit() {
            if (closed) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "cost reservation is closed");
            }
            ActionResult<Void> result = reservation.commit();
            if (result.successful()) {
                closed = true;
            }
            return result;
        }

        @Override
        public synchronized ActionResult<Void> refund() {
            if (closed) {
                return ActionResult.success(null);
            }
            ActionResult<Void> result = reservation.refund();
            if (result.successful()) {
                closed = true;
            }
            return result;
        }

        @Override
        public Map<String, String> auditContext() {
            return Map.of(
                    "cost_amount", amount().toPlainString(),
                    "cost_reservation_id", reservation.reservationId().toString(),
                    "cost_transaction_id", reservation.transactionId().toString());
        }

        @Override
        public synchronized void close() {
            if (!closed) {
                refund();
            }
        }
    }

    private static final class NativeReservation implements Reservation {
        private final EconomyRepository repository;
        private final EconomyRepository.CostHold hold;
        private boolean closed;

        private NativeReservation(EconomyRepository repository, EconomyRepository.CostHold hold) {
            this.repository = repository;
            this.hold = hold;
        }

        @Override
        public BigDecimal amount() {
            return EconomyMoney.toMajorUnits(hold.amount(), repository.minorUnits());
        }

        @Override
        public synchronized ActionResult<Void> commit() {
            if (closed) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "cost reservation is closed");
            }
            ActionResult<Void> result = repository.commitCost(hold.reservationId());
            if (!result.successful()) {
                return result;
            }
            try {
                repository.flush();
                closed = true;
                return result;
            } catch (IOException exception) {
                try {
                    repository.restorePendingCost(hold);
                    repository.refundCost(hold.reservationId());
                    repository.flush();
                } catch (IOException | RuntimeException rollbackFailure) {
                    exception.addSuppressed(rollbackFailure);
                }
                closed = true;
                return ActionResult.failure(
                        ActionResult.ReasonCode.COST_UNAVAILABLE,
                        "cost commit could not be persisted");
            }
        }

        @Override
        public synchronized ActionResult<Void> refund() {
            if (closed) {
                return ActionResult.success(null);
            }
            ActionResult<Void> result = repository.refundCost(hold.reservationId());
            if (!result.successful()) {
                return result;
            }
            try {
                repository.flush();
                closed = true;
                return result;
            } catch (IOException exception) {
                closed = true;
                return ActionResult.failure(
                        ActionResult.ReasonCode.COST_UNAVAILABLE,
                        "cost refund could not be persisted");
            }
        }

        @Override
        public Map<String, String> auditContext() {
            return Map.of(
                    "cost_amount", amount().toPlainString(),
                    "cost_reservation_id", hold.reservationId().toString(),
                    "cost_transaction_id", hold.transactionId().toString());
        }

        @Override
        public synchronized void close() {
            if (!closed) {
                refund();
            }
        }
    }
}
