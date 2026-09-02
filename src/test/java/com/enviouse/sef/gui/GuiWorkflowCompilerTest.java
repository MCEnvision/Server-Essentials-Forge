package com.enviouse.sef.gui;

import com.enviouse.sef.audit.AuditService;
import com.enviouse.sef.kernel.command.CommandDefinition;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiWorkflowCompilerTest {
    @Test
    void compilesEveryExecutableBranchIntoABoundedTypedVariant() {
        CommandDispatcher<String> dispatcher = new CommandDispatcher<>();
        dispatcher.register(literal("access")
                .then(literal("create")
                        .then(argument("player", StringArgumentType.word())
                                .then(argument("duration", IntegerArgumentType.integer(1, 86_400))
                                        .then(argument("reason", StringArgumentType.greedyString())
                                                .executes(context -> 1))))
                        .then(literal("approved")
                                .then(argument("approval", StringArgumentType.word())
                                        .then(argument("player", StringArgumentType.word())
                                                .then(argument("duration", IntegerArgumentType.integer(1, 86_400))
                                                        .then(argument("reason", StringArgumentType.greedyString())
                                                                .executes(context -> 1))))))));

        GuiWorkflowCompiler.WorkflowDefinition workflow = GuiWorkflowCompiler.compile(
                definition("sef:access.create", "access create"),
                dispatcher,
                "source");

        assertEquals(2, workflow.variants().size());
        GuiWorkflowCompiler.Variant direct = workflow.variants().stream()
                .filter(variant -> variant.fields().size() == 3)
                .findFirst()
                .orElseThrow();
        assertEquals(GuiWorkflowCompiler.FieldType.PLAYER, direct.fields().get(0).type());
        assertEquals(GuiWorkflowCompiler.FieldType.INTEGER, direct.fields().get(1).type());
        assertEquals(1.0D, direct.fields().get(1).minimum());
        assertEquals(86_400.0D, direct.fields().get(1).maximum());
        assertEquals(GuiWorkflowCompiler.RenderMode.GREEDY, direct.fields().get(2).renderMode());
        assertTrue(workflow.requiresConfirmation());
    }

    @Test
    void preservesOptionalNoArgumentAndArgumentVariants() {
        CommandDispatcher<String> dispatcher = new CommandDispatcher<>();
        dispatcher.register(literal("toggle")
                .executes(context -> 1)
                .then(argument("enabled", BoolArgumentType.bool())
                        .executes(context -> 1)));

        GuiWorkflowCompiler.WorkflowDefinition workflow = GuiWorkflowCompiler.compile(
                definition("sef:toggle", "toggle"),
                dispatcher,
                "source");

        assertEquals(2, workflow.variants().size());
        assertTrue(workflow.variants().stream().anyMatch(variant -> variant.fields().isEmpty()));
        assertTrue(workflow.variants().stream().anyMatch(variant ->
                variant.fields().size() == 1
                        && variant.fields().getFirst().type() == GuiWorkflowCompiler.FieldType.BOOLEAN));
    }

    @Test
    void rejectsMetadataThatDoesNotResolveToAnExecutableRoute() {
        CommandDispatcher<String> dispatcher = new CommandDispatcher<>();
        dispatcher.register(literal("different").executes(context -> 1));

        assertThrows(
                IllegalArgumentException.class,
                () -> GuiWorkflowCompiler.compile(
                        definition("sef:missing", "missing route"),
                        dispatcher,
                        "source"));
    }

    @Test
    void structuralCompilationIgnoresAuthorizationWithoutWeakeningProductionCompilation() {
        CommandDispatcher<String> dispatcher = new CommandDispatcher<>();
        dispatcher.register(literal("restricted")
                .requires(source -> source.equals("allowed"))
                .then(literal("run")
                        .executes(context -> 1)));

        GuiWorkflowCompiler.WorkflowDefinition workflow = GuiWorkflowCompiler.compileStructure(
                definition("sef:restricted.run", "restricted run"),
                dispatcher);

        assertEquals(1, workflow.variants().size());
        assertThrows(
                IllegalArgumentException.class,
                () -> GuiWorkflowCompiler.compile(
                        definition("sef:restricted.run", "restricted run"),
                        dispatcher,
                        "denied"));
        assertEquals(
                1,
                GuiWorkflowCompiler.compile(
                                definition("sef:restricted.run", "restricted run"),
                                dispatcher,
                                "allowed")
                        .variants()
                        .size());
    }

    @Test
    void multiPlayerFieldsHaveRoomForBoundedGuiSelections() {
        CommandDispatcher<String> dispatcher = new CommandDispatcher<>();
        dispatcher.register(literal("give")
                .then(argument("targets", new FakeEntityArgument())
                        .executes(context -> 1)));

        GuiWorkflowCompiler.WorkflowDefinition workflow = GuiWorkflowCompiler.compile(
                definition("sef:item.give.others", "give"),
                dispatcher,
                "source");

        GuiWorkflowCompiler.Field field = workflow.variants().getFirst().fields().getFirst();
        assertEquals(GuiWorkflowCompiler.FieldType.PLAYERS, field.type());
        assertEquals(GuiWorkflowCompiler.MAXIMUM_MULTI_TARGET_LENGTH, field.maximumLength());
    }

    private static CommandDefinition definition(String id, String route) {
        return new CommandDefinition(
                id,
                route,
                Set.of(),
                "command.test.description",
                "command.test.usage",
                "test",
                "sef.test",
                Set.of("sef.test.use"),
                CommandDefinition.AccessClass.STAFF,
                Set.of(CommandDefinition.SourceType.PLAYER),
                CommandDefinition.TargetBehavior.REQUIRED_PLAYER,
                id,
                true,
                AuditService.AuditClass.ADMIN_ACTION,
                "sef:test",
                "",
                "state is shown through immediate feedback",
                "",
                "domain state is bounded",
                CommandDefinition.ConflictPolicy.CANONICAL_ONLY,
                true,
                true);
    }

    private static LiteralArgumentBuilder<String> literal(String value) {
        return LiteralArgumentBuilder.literal(value);
    }

    private static <T> RequiredArgumentBuilder<String, T> argument(
            String name,
            ArgumentType<T> type
    ) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    private static final class FakeEntityArgument implements ArgumentType<String> {
        @Override
        public String parse(StringReader reader) {
            return reader.readUnquotedString();
        }
    }
}
