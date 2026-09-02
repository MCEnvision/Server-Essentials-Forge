package com.enviouse.sef.kernel.command;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.ActionResult;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class AliasCompiler {
    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9_.-]*:[a-z0-9_./-]+");
    private static final Pattern ROOT = Pattern.compile("[a-z0-9_:-]+");
    private static final Set<ArgumentSchema> SAFE_SCHEMAS = Set.of(
            ArgumentSchema.NONE,
            ArgumentSchema.IDENTITY,
            ArgumentSchema.OPTIONAL_IDENTITY,
            ArgumentSchema.IDENTITY_DURATION,
            ArgumentSchema.IDENTITY_DURATION_REASON,
            ArgumentSchema.ENUM,
            ArgumentSchema.BOUNDED_INTEGER,
            ArgumentSchema.BOUNDED_DECIMAL,
            ArgumentSchema.RESOURCE_LOCATION,
            ArgumentSchema.ITEM_OPTIONAL_AMOUNT,
            ArgumentSchema.HOME_NAME,
            ArgumentSchema.WARP_NAME,
            ArgumentSchema.DIMENSION_COORDINATES,
            ArgumentSchema.TYPED_MESSAGE,
            ArgumentSchema.APPROVED_EXTERNAL_ADAPTER);

    private final CommandCatalog catalog;
    private final CapabilityManifest capabilities;
    private final Supplier<Set<String>> knownBundleIds;
    private final Map<String, ExternalAdapter> externalAdapters;
    private final RootOwnershipResolver rootOwnership;

    public AliasCompiler(
            CommandCatalog catalog,
            CapabilityManifest capabilities,
            Set<String> knownBundleIds,
            Map<String, ExternalAdapter> externalAdapters
    ) {
        this(
                catalog,
                capabilities,
                knownBundleIds,
                externalAdapters,
                root -> catalog.rootOwner(root)
                        .map(owner -> new RootOwnership(RootOwnerKind.CATALOG, owner))
                        .orElse(null));
    }

    public AliasCompiler(
            CommandCatalog catalog,
            CapabilityManifest capabilities,
            Set<String> knownBundleIds,
            Map<String, ExternalAdapter> externalAdapters,
            RootOwnershipResolver rootOwnership
    ) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.capabilities = Objects.requireNonNull(capabilities, "capabilities");
        Set<String> immutableBundleIds = Set.copyOf(Objects.requireNonNull(knownBundleIds, "knownBundleIds"));
        this.knownBundleIds = () -> immutableBundleIds;
        this.externalAdapters = Map.copyOf(Objects.requireNonNull(externalAdapters, "externalAdapters"));
        this.rootOwnership = Objects.requireNonNull(rootOwnership, "rootOwnership");
    }

    public AliasCompiler(
            CommandCatalog catalog,
            CapabilityManifest capabilities,
            Supplier<Set<String>> knownBundleIds,
            Map<String, ExternalAdapter> externalAdapters,
            RootOwnershipResolver rootOwnership
    ) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.capabilities = Objects.requireNonNull(capabilities, "capabilities");
        this.knownBundleIds = Objects.requireNonNull(knownBundleIds, "knownBundleIds");
        this.externalAdapters = Map.copyOf(Objects.requireNonNull(externalAdapters, "externalAdapters"));
        this.rootOwnership = Objects.requireNonNull(rootOwnership, "rootOwnership");
    }

    public ActionResult<CompiledAlias> compile(AliasDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (!ID.matcher(definition.id()).matches() || !ROOT.matcher(definition.root()).matches()) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "invalid alias id or root");
        }
        if (!SAFE_SCHEMAS.contains(definition.argumentSchema())) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "unsafe argument schema");
        }
        if (!definition.additionalPermissionId().isBlank()
                && !capabilities.contains(definition.additionalPermissionId())) {
            return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "unknown additional permission");
        }
        if (definition.id().equals(definition.targetId())) {
            return ActionResult.failure(ActionResult.ReasonCode.RECURSION_DENIED, "alias cannot target itself");
        }
        if (definition.kind() == AliasKind.ACTION) {
            Optional<CommandDefinition> target = catalog.find(definition.targetId());
            if (target.isEmpty()) {
                return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "unknown action");
            }
            if (definition.accessClass().ordinal() < target.get().accessClass().ordinal()) {
                return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "alias weakens target access class");
            }
            if (definition.auditClass().ordinal() < target.get().auditClass().ordinal()) {
                return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "alias weakens target audit policy");
            }
            if (!target.get().sourceTypes().containsAll(definition.sourceTypes())) {
                return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "alias expands source classes");
            }
        } else if (definition.kind() == AliasKind.BUNDLE) {
            if (definition.argumentSchema() != ArgumentSchema.NONE) {
                return ActionResult.failure(
                        ActionResult.ReasonCode.INVALID_DEFINITION,
                        "bundle aliases require the none argument schema");
            }
            if (!Set.copyOf(Objects.requireNonNull(knownBundleIds.get(), "knownBundleIds"))
                    .contains(definition.targetId())) {
                return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "unknown bundle");
            }
        } else {
            ExternalAdapter adapter = externalAdapters.get(definition.targetId());
            if (adapter == null || !adapter.schema().equals(definition.argumentSchema())) {
                return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "unknown or incompatible external adapter");
            }
            if (!adapter.sourceTypes().containsAll(definition.sourceTypes())) {
                return ActionResult.failure(ActionResult.ReasonCode.POLICY_DENIED, "alias expands adapter source classes");
            }
        }
        return ActionResult.success(new CompiledAlias(definition, Instant.now()));
    }

    public ActionResult<CompiledAlias> validatePublication(AliasDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        ActionResult<Void> rootDecision = validatePublicationRoot(definition);
        if (!rootDecision.successful()) {
            return ActionResult.failure(rootDecision.reason(), rootDecision.detail());
        }
        return compile(definition);
    }

    public static final class Registry {
        private final AliasCompiler compiler;
        private final int maximumDefinitions;
        private final Map<String, AliasDefinition> drafts = new LinkedHashMap<>();
        private final Map<String, CompiledAlias> published = new LinkedHashMap<>();

        public Registry(AliasCompiler compiler, int maximumDefinitions) {
            this.compiler = Objects.requireNonNull(compiler, "compiler");
            if (maximumDefinitions < 1 || maximumDefinitions > 1024) {
                throw new IllegalArgumentException("Alias definition limit is outside hard bounds");
            }
            this.maximumDefinitions = maximumDefinitions;
        }

        public synchronized ActionResult<AliasDefinition> saveDraft(AliasDefinition definition) {
            Set<String> definitionIds = new java.util.HashSet<>(drafts.keySet());
            definitionIds.addAll(published.keySet());
            if (!definitionIds.contains(definition.id()) && definitionIds.size() >= maximumDefinitions) {
                return ActionResult.failure(ActionResult.ReasonCode.QUOTA_EXCEEDED, "alias definition limit reached");
            }
            if (definition.state() != DefinitionState.DRAFT) {
                return ActionResult.failure(ActionResult.ReasonCode.INVALID_DEFINITION, "draft state required");
            }
            AliasDefinition previous = drafts.get(definition.id());
            if (previous != null && definition.revision() <= previous.revision()) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "draft revision is stale");
            }
            drafts.put(definition.id(), definition);
            return ActionResult.success(definition);
        }

        public synchronized ActionResult<CompiledAlias> publish(String id, long expectedRevision) {
            AliasDefinition draft = drafts.get(normalize(id));
            if (draft == null) {
                return ActionResult.failure(ActionResult.ReasonCode.NOT_FOUND, "alias draft not found");
            }
            if (draft.revision() != expectedRevision) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "alias draft revision changed");
            }
            CompiledAlias current = published.get(draft.id());
            if (current != null && !current.definition().root().equals(draft.root())) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "root change requires restart");
            }
            ActionResult<Void> rootDecision = compiler.validatePublicationRoot(draft);
            if (!rootDecision.successful()) {
                return ActionResult.failure(rootDecision.reason(), rootDecision.detail());
            }
            boolean ambiguousRoot = published.values().stream().anyMatch(alias ->
                    !alias.definition().id().equals(draft.id())
                            && alias.definition().root().equals(draft.root()));
            if (ambiguousRoot) {
                return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, "alias root is already published");
            }
            AliasDefinition publication = draft.withState(DefinitionState.PUBLISHED);
            ActionResult<CompiledAlias> compiled = compiler.compile(publication);
            if (compiled.successful()) {
                published.put(publication.id(), compiled.value());
            }
            return compiled;
        }

        public synchronized Map<String, CompiledAlias> published() {
            return Map.copyOf(published);
        }
    }

    private ActionResult<Void> validatePublicationRoot(AliasDefinition definition) {
        RootOwnership ownership = rootOwnership.resolve(definition.root());
        if (ownership == null) {
            return ActionResult.success(null);
        }
        if (ownership.kind() == RootOwnerKind.EXTERNAL
                && definition.conflictMode() == CommandDefinition.ConflictPolicy.PREFER_SEF) {
            return ActionResult.success(null);
        }
        String detail = switch (definition.conflictMode()) {
            case PREFER_EXISTING, CANONICAL_ONLY -> "alias root remains owned by " + ownership.ownerId();
            case FAIL -> "alias root conflicts with " + ownership.ownerId();
            case RESTART_REQUIRED -> "alias root collision requires restart";
            case PREFER_SEF -> "alias cannot replace sef root owned by " + ownership.ownerId();
        };
        return ActionResult.failure(ActionResult.ReasonCode.CONFLICT, detail);
    }

    public record AliasDefinition(
            int schemaVersion,
            String id,
            long revision,
            boolean enabled,
            DefinitionState state,
            String root,
            AliasKind kind,
            String targetId,
            ArgumentSchema argumentSchema,
            Map<String, String> fixedArguments,
            String additionalPermissionId,
            Set<CommandDefinition.SourceType> sourceTypes,
            CommandDefinition.AccessClass accessClass,
            CommandDefinition.ConflictPolicy conflictMode,
            AuditService.AuditClass auditClass,
            UUID createdBy,
            Instant createdAt
    ) {
        public AliasDefinition {
            if (schemaVersion != 1) {
                throw new IllegalArgumentException("Unsupported alias schema");
            }
            id = normalize(id);
            root = normalize(root);
            targetId = normalize(targetId);
            additionalPermissionId = additionalPermissionId == null ? "" : normalize(additionalPermissionId);
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(argumentSchema, "argumentSchema");
            fixedArguments = boundedArguments(fixedArguments);
            sourceTypes = Set.copyOf(Objects.requireNonNull(sourceTypes, "sourceTypes"));
            Objects.requireNonNull(accessClass, "accessClass");
            Objects.requireNonNull(conflictMode, "conflictMode");
            Objects.requireNonNull(auditClass, "auditClass");
            Objects.requireNonNull(createdAt, "createdAt");
            if (revision < 1 || sourceTypes.isEmpty()) {
                throw new IllegalArgumentException("Alias revision and sources are required");
            }
        }

        public AliasDefinition withState(DefinitionState newState) {
            return new AliasDefinition(
                    schemaVersion,
                    id,
                    revision,
                    enabled,
                    newState,
                    root,
                    kind,
                    targetId,
                    argumentSchema,
                    fixedArguments,
                    additionalPermissionId,
                    sourceTypes,
                    accessClass,
                    conflictMode,
                    auditClass,
                    createdBy,
                    createdAt);
        }
    }

    public record CompiledAlias(AliasDefinition definition, Instant compiledAt) {
    }

    public record ExternalAdapter(
            String id,
            ArgumentSchema schema,
            Set<CommandDefinition.SourceType> sourceTypes
    ) {
        public ExternalAdapter {
            id = normalize(id);
            Objects.requireNonNull(schema, "schema");
            sourceTypes = Set.copyOf(Objects.requireNonNull(sourceTypes, "sourceTypes"));
        }
    }

    @FunctionalInterface
    public interface RootOwnershipResolver {
        RootOwnership resolve(String root);
    }

    public record RootOwnership(RootOwnerKind kind, String ownerId) {
        public RootOwnership {
            Objects.requireNonNull(kind, "kind");
            ownerId = normalize(ownerId);
        }
    }

    public enum RootOwnerKind {
        CATALOG,
        SHORTCUT,
        EXTERNAL
    }

    public enum AliasKind {
        ACTION,
        BUNDLE,
        EXTERNAL_ACTOR_COMMAND,
        SERVER_COMMAND_PROFILE
    }

    public enum DefinitionState {
        DRAFT,
        PUBLISHED
    }

    public enum ArgumentSchema {
        NONE,
        IDENTITY,
        OPTIONAL_IDENTITY,
        IDENTITY_DURATION,
        IDENTITY_DURATION_REASON,
        ENUM,
        BOUNDED_INTEGER,
        BOUNDED_DECIMAL,
        RESOURCE_LOCATION,
        ITEM_OPTIONAL_AMOUNT,
        HOME_NAME,
        WARP_NAME,
        DIMENSION_COORDINATES,
        TYPED_MESSAGE,
        APPROVED_EXTERNAL_ADAPTER,
        RAW_COMMAND
    }

    private static Map<String, String> boundedArguments(Map<String, String> arguments) {
        Objects.requireNonNull(arguments, "arguments");
        if (arguments.size() > 32) {
            throw new IllegalArgumentException("Too many fixed alias arguments");
        }
        Map<String, String> result = new LinkedHashMap<>();
        arguments.forEach((key, value) -> {
            String normalizedKey = normalize(key);
            String boundedValue = Objects.requireNonNull(value, "value").trim();
            if (boundedValue.length() > 256) {
                throw new IllegalArgumentException("Fixed alias argument is too long");
            }
            result.put(normalizedKey, boundedValue);
        });
        return Map.copyOf(result);
    }

    private static String normalize(String value) {
        return Objects.requireNonNull(value, "value").trim().toLowerCase(Locale.ROOT);
    }
}
