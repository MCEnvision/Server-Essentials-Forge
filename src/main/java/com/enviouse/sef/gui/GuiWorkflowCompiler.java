package com.enviouse.sef.gui;

import com.enviouse.sef.kernel.command.CommandDefinition;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType.StringType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class GuiWorkflowCompiler {
    public static final int MAXIMUM_VARIANTS = 64;
    public static final int MAXIMUM_FIELDS = 24;
    public static final int MAXIMUM_DEPTH = 24;
    public static final int MAXIMUM_VALUE_LENGTH = 4096;

    private GuiWorkflowCompiler() {
    }

    public static <S> WorkflowDefinition compile(
            CommandDefinition command,
            CommandDispatcher<S> dispatcher,
            S source
    ) {
        return compile(command, dispatcher, source, true);
    }

    public static <S> WorkflowDefinition compileStructure(
            CommandDefinition command,
            CommandDispatcher<S> dispatcher
    ) {
        return compile(command, dispatcher, null, false);
    }

    private static <S> WorkflowDefinition compile(
            CommandDefinition command,
            CommandDispatcher<S> dispatcher,
            S source,
            boolean enforceRequirements
    ) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(dispatcher, "dispatcher");
        if (enforceRequirements) {
            Objects.requireNonNull(source, "source");
        }

        CommandNode<S> node = dispatcher.getRoot();
        List<Segment> prefix = new ArrayList<>();
        for (String literal : command.canonicalRoute().split(" ")) {
            CommandNode<S> child = node.getChild(literal);
            if (!(child instanceof LiteralCommandNode<S>)
                    || enforceRequirements && !child.canUse(source)) {
                throw new IllegalArgumentException(
                        "Canonical GUI route is unavailable, " + command.canonicalRoute());
            }
            prefix.add(Segment.literal(literal));
            node = child;
        }

        List<VariantDraft> drafts = new ArrayList<>();
        collect(
                node,
                source,
                enforceRequirements,
                new ArrayList<>(),
                new ArrayList<>(),
                drafts,
                0,
                new LinkedHashSet<>());
        if (drafts.isEmpty()) {
            throw new IllegalArgumentException(
                    "Canonical GUI route has no executable variant, " + command.canonicalRoute());
        }
        drafts.sort(Comparator.comparing(VariantDraft::signature));
        if (drafts.size() > MAXIMUM_VARIANTS) {
            throw new IllegalArgumentException(
                    "Canonical GUI route exceeds the workflow variant limit, " + command.canonicalRoute());
        }

        List<Variant> variants = new ArrayList<>();
        Set<String> signatures = new LinkedHashSet<>();
        for (VariantDraft draft : drafts) {
            if (!signatures.add(draft.signature())) {
                continue;
            }
            List<Segment> segments = new ArrayList<>(prefix);
            segments.addAll(draft.segments());
            variants.add(new Variant(
                    "variant_" + (variants.size() + 1),
                    label(draft.segments()),
                    segments,
                    draft.fields()));
        }
        return new WorkflowDefinition(
                command.id(),
                command.canonicalRoute(),
                command.accessClass().isPrivileged()
                        || command.auditClass()
                        != com.enviouse.sef.audit.AuditService.AuditClass.METADATA_ONLY,
                command.confirmationRequired(),
                variants);
    }

    private static <S> void collect(
            CommandNode<S> node,
            S source,
            boolean enforceRequirements,
            List<Segment> segments,
            List<Field> fields,
            List<VariantDraft> results,
            int depth,
            Set<CommandNode<S>> path
    ) {
        if (depth > MAXIMUM_DEPTH || fields.size() > MAXIMUM_FIELDS || results.size() > MAXIMUM_VARIANTS) {
            throw new IllegalArgumentException("GUI workflow command tree exceeds hard bounds");
        }
        if (!path.add(node)) {
            throw new IllegalArgumentException("GUI workflow command tree contains a redirect cycle");
        }
        if (node.getCommand() != null) {
            results.add(new VariantDraft(List.copyOf(segments), List.copyOf(fields)));
        }

        List<CommandNode<S>> children = node.getChildren().stream()
                .filter(child -> !enforceRequirements || child.canUse(source))
                .sorted(Comparator.comparing(CommandNode::getName))
                .toList();
        for (CommandNode<S> child : children) {
            if (child instanceof LiteralCommandNode<S> literal) {
                segments.add(Segment.literal(literal.getLiteral()));
                collect(
                        child,
                        source,
                        enforceRequirements,
                        segments,
                        fields,
                        results,
                        depth + 1,
                        path);
                segments.removeLast();
                continue;
            }
            if (child instanceof ArgumentCommandNode<S, ?> argument) {
                String fieldId = uniqueFieldId(argument.getName(), fields);
                Field field = field(fieldId, argument);
                fields.add(field);
                segments.add(Segment.field(fieldId));
                collect(
                        child,
                        source,
                        enforceRequirements,
                        segments,
                        fields,
                        results,
                        depth + 1,
                        path);
                segments.removeLast();
                fields.removeLast();
            }
        }
        if (node.getRedirect() != null && node.getChildren().isEmpty()) {
            collect(
                    node.getRedirect(),
                    source,
                    enforceRequirements,
                    segments,
                    fields,
                    results,
                    depth + 1,
                    path);
        }
        path.remove(node);
    }

    private static String uniqueFieldId(String input, List<Field> fields) {
        String base = normalizeFieldId(input);
        String candidate = base;
        int suffix = 2;
        while (containsField(fields, candidate)) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }

    private static boolean containsField(List<Field> fields, String id) {
        return fields.stream().anyMatch(field -> field.id().equals(id));
    }

    private static Field field(String id, ArgumentCommandNode<?, ?> node) {
        Object type = node.getType();
        FieldType fieldType;
        RenderMode renderMode = RenderMode.RAW;
        double minimum = -Double.MAX_VALUE;
        double maximum = Double.MAX_VALUE;
        int maximumLength = MAXIMUM_VALUE_LENGTH;
        List<String> choices = List.of();

        if (type instanceof BoolArgumentType) {
            fieldType = FieldType.BOOLEAN;
            choices = List.of("false", "true");
            maximumLength = 5;
        } else if (type instanceof IntegerArgumentType integer) {
            fieldType = FieldType.INTEGER;
            minimum = integer.getMinimum();
            maximum = integer.getMaximum();
            maximumLength = 32;
        } else if (type instanceof LongArgumentType number) {
            fieldType = FieldType.INTEGER;
            minimum = number.getMinimum();
            maximum = number.getMaximum();
            maximumLength = 32;
        } else if (type instanceof FloatArgumentType number) {
            fieldType = FieldType.DECIMAL;
            minimum = number.getMinimum();
            maximum = number.getMaximum();
            maximumLength = 64;
        } else if (type instanceof DoubleArgumentType number) {
            fieldType = FieldType.DECIMAL;
            minimum = number.getMinimum();
            maximum = number.getMaximum();
            maximumLength = 64;
        } else if (type instanceof StringArgumentType string) {
            fieldType = classifyString(id);
            renderMode = switch (string.getType()) {
                case SINGLE_WORD -> RenderMode.WORD;
                case QUOTABLE_PHRASE -> RenderMode.QUOTED;
                case GREEDY_PHRASE -> RenderMode.GREEDY;
            };
            maximumLength = string.getType() == StringType.SINGLE_WORD ? 256 : MAXIMUM_VALUE_LENGTH;
        } else {
            fieldType = classifyArgument(id, type.getClass().getSimpleName());
        }

        String suggestionKind = node.getCustomSuggestions() != null
                ? "server"
                : suggestionKind(fieldType);
        return new Field(
                id,
                id.replace('_', ' '),
                fieldType,
                renderMode,
                minimum,
                maximum,
                maximumLength,
                choices,
                suggestionKind);
    }

    private static FieldType classifyString(String id) {
        String normalized = id.toLowerCase(Locale.ROOT);
        if (normalized.contains("duration") || normalized.contains("delay") || normalized.contains("cooldown")) {
            return FieldType.DURATION;
        }
        if (normalized.contains("player") || normalized.equals("target")
                || normalized.equals("owner") || normalized.equals("subject")) {
            return FieldType.PLAYER;
        }
        if (normalized.contains("permission")) {
            return FieldType.PERMISSION;
        }
        if (normalized.contains("dimension") || normalized.equals("world")) {
            return FieldType.DIMENSION;
        }
        if (normalized.contains("item")) {
            return FieldType.ITEM;
        }
        if (normalized.contains("uuid") || normalized.endsWith("_id")) {
            return FieldType.IDENTIFIER;
        }
        return FieldType.TEXT;
    }

    private static FieldType classifyArgument(String id, String className) {
        String name = className.toLowerCase(Locale.ROOT);
        String field = id.toLowerCase(Locale.ROOT);
        if (name.contains("entity") || name.contains("gameprofile") || name.contains("profileargument")) {
            return field.endsWith("s") || field.contains("players")
                    ? FieldType.PLAYERS
                    : FieldType.PLAYER;
        }
        if (name.contains("item")) {
            return FieldType.ITEM;
        }
        if (name.contains("enchant")) {
            return FieldType.ENCHANTMENT;
        }
        if (name.contains("dimension")) {
            return FieldType.DIMENSION;
        }
        if (name.contains("blockpos") || name.contains("vec") || name.contains("columnpos")) {
            return FieldType.COORDINATES;
        }
        if (name.contains("component") || name.contains("message")) {
            return FieldType.TEXT;
        }
        if (name.contains("time")) {
            return FieldType.DURATION;
        }
        if (name.contains("resource") || name.contains("registry") || name.contains("key")) {
            return FieldType.RESOURCE_LOCATION;
        }
        if (name.contains("uuid")) {
            return FieldType.IDENTIFIER;
        }
        return FieldType.TEXT;
    }

    private static String suggestionKind(FieldType type) {
        return switch (type) {
            case PLAYER, PLAYERS -> "players";
            case ITEM, ENCHANTMENT, DIMENSION, RESOURCE_LOCATION -> "registry";
            case BOOLEAN -> "choices";
            default -> "";
        };
    }

    private static String label(List<Segment> segments) {
        if (segments.isEmpty()) {
            return "default";
        }
        return segments.stream()
                .map(segment -> segment.literal()
                        ? segment.value()
                        : "{" + segment.value().replace('_', ' ') + "}")
                .reduce((left, right) -> left + " " + right)
                .orElse("default");
    }

    private static String normalizeFieldId(String value) {
        String normalized = Objects.requireNonNull(value, "value")
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "_");
        if (normalized.isBlank() || !Character.isLetter(normalized.charAt(0))) {
            normalized = "field_" + normalized;
        }
        return normalized.length() <= 64 ? normalized : normalized.substring(0, 64);
    }

    public record WorkflowDefinition(
            String actionId,
            String canonicalRoute,
            boolean destructive,
            boolean confirmationRequired,
            List<Variant> variants
    ) {
        public WorkflowDefinition {
            variants = List.copyOf(variants);
        }

        public boolean requiresConfirmation() {
            return destructive || confirmationRequired;
        }

        public Variant requireVariant(String variantId) {
            return variants.stream()
                    .filter(variant -> variant.id().equals(variantId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown workflow variant"));
        }
    }

    public record Variant(
            String id,
            String label,
            List<Segment> segments,
            List<Field> fields
    ) {
        public Variant {
            segments = List.copyOf(segments);
            fields = List.copyOf(fields);
        }
    }

    public record Segment(boolean literal, String value) {
        public static Segment literal(String value) {
            return new Segment(true, value);
        }

        public static Segment field(String value) {
            return new Segment(false, value);
        }
    }

    public record Field(
            String id,
            String label,
            FieldType type,
            RenderMode renderMode,
            double minimum,
            double maximum,
            int maximumLength,
            List<String> choices,
            String suggestionKind
    ) {
        public Field {
            choices = List.copyOf(choices);
        }
    }

    public enum FieldType {
        BOOLEAN,
        INTEGER,
        DECIMAL,
        DURATION,
        PLAYER,
        PLAYERS,
        ITEM,
        ENCHANTMENT,
        DIMENSION,
        COORDINATES,
        PERMISSION,
        RESOURCE_LOCATION,
        IDENTIFIER,
        TEXT
    }

    public enum RenderMode {
        RAW,
        WORD,
        QUOTED,
        GREEDY
    }

    private record VariantDraft(List<Segment> segments, List<Field> fields) {
        private String signature() {
            return segments.stream()
                    .map(segment -> (segment.literal() ? "l:" : "f:") + segment.value())
                    .reduce((left, right) -> left + "/" + right)
                    .orElse("");
        }
    }
}
