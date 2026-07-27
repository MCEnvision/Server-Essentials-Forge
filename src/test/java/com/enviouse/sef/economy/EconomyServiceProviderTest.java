package com.enviouse.sef.economy;

import com.enviouse.sef.kernel.ActionResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EconomyServiceProviderTest {
    private static final String PROVIDER_ID = "test:failing";

    @AfterEach
    void unregisterProvider() {
        EconomyProviderRegistry.unregister(PROVIDER_ID);
    }

    @Test
    void enabledExternalModeFailsWhenSelectedProviderIsAbsent() {
        assertThrows(
                IllegalStateException.class,
                () -> new EconomyService(repository(), settings(EconomyService.Mode.EXTERNAL, "missing")));
    }

    @Test
    void externalProviderFailurePropagatesWithoutCreatingNativeShadowAccount() {
        EconomyRepository nativeRepository = repository();
        FailingProvider failing = new FailingProvider();
        assertTrue(EconomyProviderRegistry.register(PROVIDER_ID, 10, failing, null));
        EconomyService service =
                new EconomyService(nativeRepository, settings(EconomyService.Mode.EXTERNAL, PROVIDER_ID));
        UUID player = UUID.randomUUID();

        ActionResult<EconomyProvider.Account> result = service.getOrCreate(player, player);

        assertFalse(result.successful());
        assertTrue(nativeRepository.account(player).isEmpty());
    }

    @Test
    void importModeRequiresAnImporterAndRejectsProviderOnlyRegistration() {
        assertTrue(EconomyProviderRegistry.register(PROVIDER_ID, 10, new FailingProvider(), null));
        assertThrows(
                IllegalStateException.class,
                () -> new EconomyService(repository(), settings(EconomyService.Mode.IMPORT_ONCE, PROVIDER_ID)));
    }

    @Test
    void importOnceValidatesPreviewAndSwitchesOwnershipToNativeExactlyOnce(
            @TempDir Path temporaryDirectory
    ) {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        EconomyProviderRegistry.ImportAdapter importer = new EconomyProviderRegistry.ImportAdapter() {
            @Override
            public EconomyProviderRegistry.ImportPreview preview() {
                return new EconomyProviderRegistry.ImportPreview(2, 300L, "test");
            }

            @Override
            public List<EconomyProviderRegistry.ImportAccount> exportAccounts() {
                return List.of(
                        new EconomyProviderRegistry.ImportAccount(first, 100L),
                        new EconomyProviderRegistry.ImportAccount(second, 200L));
            }
        };
        assertTrue(EconomyProviderRegistry.register(PROVIDER_ID, 10, new FailingProvider(), importer));
        EconomyRepository nativeRepository = repository();
        nativeRepository.load(temporaryDirectory);
        EconomyService service =
                new EconomyService(nativeRepository, settings(EconomyService.Mode.IMPORT_ONCE, PROVIDER_ID));

        assertTrue(service.importPending());
        EconomyRepository.ImportRecord record = service.executeImport(UUID.randomUUID(), "confirm");

        assertTrue(!service.importPending());
        assertTrue(service.provider().orElseThrow() == nativeRepository);
        assertTrue(record.accounts() == 2);
        assertTrue(nativeRepository.account(first).orElseThrow().balance() == 100L);
        assertTrue(Files.isRegularFile(
                temporaryDirectory.resolve("economy-import-reports")
                        .resolve(record.reportHash() + ".json")));
        try (var backups = Files.list(temporaryDirectory.resolve("backups"))) {
            assertTrue(backups.anyMatch(path -> path.getFileName().toString().endsWith(".preimport")));
        } catch (java.io.IOException exception) {
            throw new AssertionError(exception);
        }
        assertThrows(
                IllegalStateException.class,
                () -> service.executeImport(UUID.randomUUID(), "confirm"));
    }

    @Test
    void importPreviewRejectsUnsafeAdapterDetail() {
        EconomyProviderRegistry.ImportAdapter importer = new EconomyProviderRegistry.ImportAdapter() {
            @Override
            public EconomyProviderRegistry.ImportPreview preview() {
                return new EconomyProviderRegistry.ImportPreview(0, 0L, "unsafe\nmessage");
            }

            @Override
            public List<EconomyProviderRegistry.ImportAccount> exportAccounts() {
                return List.of();
            }
        };
        assertTrue(EconomyProviderRegistry.register(PROVIDER_ID, 10, new FailingProvider(), importer));
        EconomyService service =
                new EconomyService(repository(), settings(EconomyService.Mode.IMPORT_ONCE, PROVIDER_ID));

        assertThrows(IllegalStateException.class, service::importPreview);
    }

    @Test
    void importExecutionRejectsAnExportTotalThatDoesNotMatchItsPreview(
            @TempDir Path temporaryDirectory
    ) {
        UUID player = UUID.randomUUID();
        EconomyProviderRegistry.ImportAdapter importer = new EconomyProviderRegistry.ImportAdapter() {
            @Override
            public EconomyProviderRegistry.ImportPreview preview() {
                return new EconomyProviderRegistry.ImportPreview(1, 200L, "test");
            }

            @Override
            public List<EconomyProviderRegistry.ImportAccount> exportAccounts() {
                return List.of(new EconomyProviderRegistry.ImportAccount(player, 100L));
            }
        };
        assertTrue(EconomyProviderRegistry.register(PROVIDER_ID, 10, new FailingProvider(), importer));
        EconomyRepository nativeRepository = repository();
        nativeRepository.load(temporaryDirectory);
        EconomyService service =
                new EconomyService(nativeRepository, settings(EconomyService.Mode.IMPORT_ONCE, PROVIDER_ID));

        assertThrows(
                IllegalStateException.class,
                () -> service.executeImport(UUID.randomUUID(), "confirm"));
        assertTrue(service.importPending());
        assertTrue(nativeRepository.account(player).isEmpty());
    }

    private static EconomyService.Settings settings(EconomyService.Mode mode, String providerId) {
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

    private static final class FailingProvider implements EconomyProvider {
        @Override
        public String id() {
            return PROVIDER_ID;
        }

        @Override
        public String currency() {
            return "coin";
        }

        @Override
        public int minorUnits() {
            return 2;
        }

        @Override
        public long minimumBalance() {
            return 0L;
        }

        @Override
        public long maximumBalance() {
            return 1_000_000L;
        }

        @Override
        public Optional<Account> account(UUID playerId) {
            return Optional.empty();
        }

        @Override
        public ActionResult<Account> createAccount(MutationRequest request) {
            return failure();
        }

        @Override
        public ActionResult<Boolean> has(UUID playerId, long amount) {
            return failure();
        }

        @Override
        public ActionResult<Transaction> deposit(MutationRequest request) {
            return failure();
        }

        @Override
        public ActionResult<Transaction> withdraw(MutationRequest request) {
            return failure();
        }

        @Override
        public ActionResult<Transaction> transfer(TransferRequest request) {
            return failure();
        }

        @Override
        public ActionResult<Transaction> setBalance(MutationRequest request) {
            return failure();
        }

        @Override
        public ActionResult<Transaction> freezeAccount(FreezeRequest request) {
            return failure();
        }

        @Override
        public List<Transaction> listTransactions(UUID playerId, int offset, int limit) {
            throw new IllegalStateException("provider unavailable");
        }

        @Override
        public BalanceSnapshot createSnapshot(int limit) {
            throw new IllegalStateException("provider unavailable");
        }

        private static <T> ActionResult<T> failure() {
            return ActionResult.failure(ActionResult.ReasonCode.COST_UNAVAILABLE, "provider unavailable");
        }
    }
}
