package com.enviouse.sef.economy;

import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.storage.AtomicFileStore;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class EconomyService {
    private final EconomyRepository nativeRepository;
    private final Settings settings;
    private final EconomyProviderRegistry.Registration external;

    public EconomyService(EconomyRepository nativeRepository, Settings settings) {
        this.nativeRepository = Objects.requireNonNull(nativeRepository, "nativeRepository");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.external = settings.mode() == Mode.EXTERNAL || settings.mode() == Mode.IMPORT_ONCE
                ? EconomyProviderRegistry.select(settings.externalProviderId()).orElse(null)
                : null;
        if (settings.enabled() && settings.mode() == Mode.DISABLED) {
            throw new IllegalStateException("Economy features are enabled while the provider mode is disabled");
        }
        if (settings.enabled() && settings.mode() == Mode.EXTERNAL && external == null) {
            throw new IllegalStateException("The selected external economy provider is unavailable");
        }
        if (settings.mode() == Mode.IMPORT_ONCE
                && (external == null || external.importer() == null)) {
            throw new IllegalStateException("Import once mode requires an available economy import adapter");
        }
    }

    public Settings settings() {
        return settings;
    }

    public EconomyRepository nativeRepository() {
        return nativeRepository;
    }

    public Optional<EconomyProvider> provider() {
        if (!settings.enabled()) {
            return Optional.empty();
        }
        return switch (settings.mode()) {
            case NATIVE -> Optional.of(nativeRepository);
            case EXTERNAL -> Optional.ofNullable(external).map(EconomyProviderRegistry.Registration::provider);
            case IMPORT_ONCE -> nativeRepository.imports().isEmpty()
                    ? Optional.empty()
                    : Optional.of(nativeRepository);
            case DISABLED -> Optional.empty();
        };
    }

    public EconomyProvider requireProvider() {
        return provider().orElseThrow(() ->
                new IllegalStateException("The economy provider is unavailable"));
    }

    public boolean importPending() {
        return settings.mode() == Mode.IMPORT_ONCE && nativeRepository.imports().isEmpty();
    }

    public EconomyProviderRegistry.ImportPreview importPreview() {
        if (!importPending() || external == null || external.importer() == null) {
            throw new IllegalStateException("No economy import is pending");
        }
        EconomyProviderRegistry.ImportPreview preview = external.importer().preview();
        if (preview == null
                || preview.accounts() < 0
                || preview.accounts() > settings.maximumImportAccounts()
                || preview.detail().length() > 512
                || preview.detail().codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalStateException("Economy import preview is invalid");
        }
        return preview;
    }

    public EconomyRepository.ImportRecord executeImport(UUID actorId, String confirmation) {
        if (!"confirm".equalsIgnoreCase(Objects.requireNonNullElse(confirmation, "").strip())) {
            throw new IllegalArgumentException("Import execution requires the literal confirm token");
        }
        EconomyProviderRegistry.ImportPreview preview = importPreview();
        List<EconomyProviderRegistry.ImportAccount> accounts = external.importer().exportAccounts();
        if (accounts == null
                || accounts.size() != preview.accounts()
                || accounts.size() > settings.maximumImportAccounts()) {
            throw new IllegalStateException("Economy import export does not match its preview");
        }
        long exportedTotal = 0L;
        try {
            for (EconomyProviderRegistry.ImportAccount account : accounts) {
                if (account == null) {
                    throw new IllegalStateException("Economy import export contains a null account");
                }
                exportedTotal = Math.addExact(exportedTotal, account.balance());
            }
        } catch (ArithmeticException exception) {
            throw new IllegalStateException("Economy import export total overflows", exception);
        }
        if (exportedTotal != preview.totalMinorUnits()) {
            throw new IllegalStateException("Economy import export total does not match its preview");
        }
        try {
            nativeRepository.prepareImportBackup();
            nativeRepository.flush();
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("The pre import economy backup could not be written", exception);
        }
        final Path backup;
        try {
            Path economyPath = nativeRepository.path();
            if (economyPath == null || economyPath.getParent() == null) {
                throw new IOException("Economy repository path is unavailable");
            }
            backup = AtomicFileStore.backup(
                    economyPath,
                    economyPath.getParent().resolve("backups"),
                    "preimport",
                    Clock.systemUTC());
        } catch (IOException exception) {
            throw new IllegalStateException("The pre import economy backup could not be written", exception);
        }
        EconomyRepository.ImportRecord record = nativeRepository.importAccounts(
                external.id(),
                accounts,
                Objects.requireNonNull(actorId, "actorId"),
                "import." + UUID.randomUUID());
        Path report = null;
        try {
            report = writeImportReport(record, actorId, backup);
            nativeRepository.flush();
            return record;
        } catch (IOException | RuntimeException exception) {
            try {
                nativeRepository.rollbackImport(record);
            } catch (RuntimeException rollbackFailure) {
                exception.addSuppressed(rollbackFailure);
            }
            if (report != null) {
                try {
                    Files.deleteIfExists(report);
                } catch (IOException cleanupFailure) {
                    exception.addSuppressed(cleanupFailure);
                }
            }
            throw new IllegalStateException("Economy import could not commit its report and storage", exception);
        }
    }

    private Path writeImportReport(
            EconomyRepository.ImportRecord record,
            UUID actorId,
            Path backup
    ) throws IOException {
        Path economyPath = nativeRepository.path();
        if (economyPath == null || economyPath.getParent() == null) {
            throw new IOException("Economy repository path is unavailable");
        }
        Path reports = economyPath.getParent().resolve("economy-import-reports").normalize();
        Files.createDirectories(reports);
        Path destination = reports.resolve(record.reportHash() + ".json").normalize();
        if (!destination.startsWith(reports)) {
            throw new IOException("Economy import report path escaped its managed directory");
        }
        JsonObject document = new JsonObject();
        document.addProperty("schemaVersion", 1);
        document.addProperty("source", record.sourceId());
        document.addProperty("importedAtEpochMillis", record.importedAtEpochMillis());
        document.addProperty("accounts", record.accounts());
        document.addProperty("totalMinorUnits", record.totalMinorUnits());
        document.addProperty("reportHash", record.reportHash());
        document.addProperty("actorId", actorId.toString());
        document.addProperty("backup", backup.getFileName().toString());
        String json = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create()
                .toJson(document);
        Path temporary = Files.createTempFile(reports, record.reportHash() + ".", ".tmp");
        try {
            Files.writeString(temporary, json, StandardCharsets.UTF_8);
            try {
                Files.move(
                        temporary,
                        destination,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(
                        temporary,
                        destination,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
        return destination;
    }

    public String format(long amount) {
        EconomyProvider provider = provider().orElse(nativeRepository);
        return EconomyMoney.format(amount, provider.minorUnits(), settings.symbol());
    }

    public long parsePositive(String amount) {
        EconomyProvider provider = requireProvider();
        return EconomyMoney.parsePositive(
                amount,
                provider.minorUnits(),
                Math.min(provider.maximumBalance(), settings.maximumTransaction()));
    }

    public long parseBalance(String amount) {
        EconomyProvider provider = requireProvider();
        return EconomyMoney.parse(
                amount,
                provider.minorUnits(),
                provider.minimumBalance(),
                provider.maximumBalance(),
                true);
    }

    public ActionResult<EconomyProvider.Account> getOrCreate(UUID playerId, UUID actorId) {
        EconomyProvider provider = requireProvider();
        Optional<EconomyProvider.Account> existing = provider.account(playerId);
        if (existing.isPresent()) {
            return ActionResult.success(existing.orElseThrow());
        }
        long opening = provider == nativeRepository ? nativeRepository.settings().defaultBalance() : 0L;
        return provider.createAccount(new EconomyProvider.MutationRequest(
                "account.create." + playerId,
                actorId,
                playerId,
                "account creation",
                provider.currency(),
                opening,
                java.util.Map.of(),
                false));
    }

    public enum Mode {
        NATIVE,
        EXTERNAL,
        DISABLED,
        IMPORT_ONCE;

        public static Mode parse(String value) {
            return switch (Objects.requireNonNullElse(value, "").strip().toLowerCase(Locale.ROOT)) {
                case "native" -> NATIVE;
                case "external" -> EXTERNAL;
                case "import_once", "importonce" -> IMPORT_ONCE;
                default -> DISABLED;
            };
        }
    }

    public record Settings(
            boolean enabled,
            Mode mode,
            String externalProviderId,
            String symbol,
            boolean allowOfflinePayments,
            boolean allowSelfPayments,
            long confirmationThreshold,
            long maximumTransaction,
            int balanceTopPageSize,
            int historyPageSize,
            int maximumImportAccounts
    ) {
        public Settings {
            Objects.requireNonNull(mode, "mode");
            externalProviderId = Objects.requireNonNullElse(externalProviderId, "").strip().toLowerCase(Locale.ROOT);
            symbol = Objects.requireNonNullElse(symbol, "");
            if (externalProviderId.length() > 128
                    || symbol.length() > 16
                    || symbol.codePoints().anyMatch(Character::isISOControl)
                    || confirmationThreshold < 0L
                    || maximumTransaction < 1L
                    || balanceTopPageSize < 1
                    || balanceTopPageSize > 100
                    || historyPageSize < 1
                    || historyPageSize > 100
                    || maximumImportAccounts < 1
                    || maximumImportAccounts > 1_000_000) {
                throw new IllegalArgumentException("Economy service settings are outside hard bounds");
            }
        }
    }
}
