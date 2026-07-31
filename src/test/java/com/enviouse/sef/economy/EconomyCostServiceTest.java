package com.enviouse.sef.economy;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.policy.CostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class EconomyCostServiceTest {
    private static final String EXTERNAL_ID = "test:reservable";

    @AfterEach
    void unregisterProvider() {
        EconomyProviderRegistry.unregister(EXTERNAL_ID);
    }

    @Test
    void reservationAndCommitCrossSynchronousStorageBoundaries(
            @TempDir Path temporaryDirectory
    ) throws Exception {
        UUID player = UUID.randomUUID();
        EconomyRepository repository = repository();
        repository.load(temporaryDirectory);
        assertTrue(repository.createAccount(new EconomyProvider.MutationRequest(
                "create",
                player,
                player,
                "test",
                repository.currency(),
                1_000L,
                Map.of(),
                false)).successful());
        repository.flush();
        EconomyCostService service = new EconomyCostService(new EconomyService(
                repository,
                settings(EconomyService.Mode.NATIVE, "")));

        ActionResult<CostService.Reservation> reserved =
                service.reserve(player, "sef:test", new BigDecimal("1.25"));
        assertTrue(reserved.successful());

        EconomyRepository crashView = repository();
        crashView.load(temporaryDirectory);
        assertEquals(1_000L, crashView.account(player).orElseThrow().balance());

        assertTrue(reserved.value().commit().successful());
        EconomyRepository committedView = repository();
        committedView.load(temporaryDirectory);
        assertEquals(875L, committedView.account(player).orElseThrow().balance());
    }

    @Test
    void externalCrashSafeReservationContractIsUsed() {
        EconomyProvider provider = mock(
                EconomyProvider.class,
                withSettings().extraInterfaces(EconomyProvider.CostReservationProvider.class));
        EconomyProvider.EconomyCostReservation reservation =
                mock(EconomyProvider.EconomyCostReservation.class);
        UUID reservationId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        when(provider.currency()).thenReturn("coin");
        when(provider.minorUnits()).thenReturn(2);
        when(provider.maximumBalance()).thenReturn(1_000_000L);
        when(reservation.reservationId()).thenReturn(reservationId);
        when(reservation.transactionId()).thenReturn(transactionId);
        when(reservation.amount()).thenReturn(125L);
        when(reservation.commit()).thenReturn(ActionResult.success(null));
        when(((EconomyProvider.CostReservationProvider) provider).reserveCost(
                org.mockito.ArgumentMatchers.any())).thenReturn(ActionResult.success(reservation));
        assertTrue(EconomyProviderRegistry.register(EXTERNAL_ID, 10, provider, null));
        EconomyService economy = new EconomyService(
                repository(),
                settings(EconomyService.Mode.EXTERNAL, EXTERNAL_ID));
        EconomyCostService costs = new EconomyCostService(economy);

        ActionResult<CostService.Reservation> result =
                costs.reserve(UUID.randomUUID(), "sef:test", new BigDecimal("1.25"));

        assertTrue(result.successful());
        assertEquals(new BigDecimal("1.25"), result.value().amount());
        assertEquals(reservationId.toString(), result.value().auditContext().get("cost_reservation_id"));
        assertTrue(result.value().commit().successful());
    }

    @Test
    void repeatedStableOperationReturnsOneNativeReservation(
            @TempDir Path temporaryDirectory
    ) {
        UUID player = UUID.randomUUID();
        UUID operation = UUID.randomUUID();
        EconomyRepository repository = repository();
        repository.load(temporaryDirectory);
        assertTrue(repository.createAccount(new EconomyProvider.MutationRequest(
                "create.stable",
                player,
                player,
                "test",
                repository.currency(),
                1_000L,
                Map.of(),
                false)).successful());
        EconomyCostService service = new EconomyCostService(new EconomyService(
                repository,
                settings(EconomyService.Mode.NATIVE, "")));

        ActionResult<CostService.Reservation> first =
                service.reserve(player, "economy sign purchase", new BigDecimal("1.25"), operation);
        ActionResult<CostService.Reservation> repeated =
                service.reserve(player, "economy sign purchase", new BigDecimal("1.25"), operation);

        assertTrue(first.successful());
        assertTrue(repeated.successful());
        assertEquals(
                first.value().auditContext().get("cost_reservation_id"),
                repeated.value().auditContext().get("cost_reservation_id"));
        assertEquals(875L, repository.account(player).orElseThrow().balance());
        assertTrue(first.value().refund().successful());
        assertEquals(1_000L, repository.account(player).orElseThrow().balance());
    }

    private static EconomyService.Settings settings(
            EconomyService.Mode mode,
            String providerId
    ) {
        return new EconomyService.Settings(
                true,
                mode,
                providerId,
                "$",
                true,
                false,
                10_000L,
                100_000L,
                10,
                10,
                1_000);
    }

    private static EconomyRepository repository() {
        return new EconomyRepository(new EconomyRepository.Settings(
                "coin",
                2,
                0L,
                0L,
                1_000_000L,
                100_000L,
                1_000,
                10_000,
                1_000,
                1_000));
    }
}
