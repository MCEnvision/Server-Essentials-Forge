package com.enviouse.sef.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.storage.StorageService;
import com.enviouse.sef.storage.StorageLifecycle;
import com.enviouse.sef.storage.repository.StorageRepository;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Op-bulletin system: stores messages that are shown to operators on login.
 */
public class OpBulletinHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAXIMUM_BULLETINS = 1_000;
    private static final int MAXIMUM_MESSAGE_LENGTH = 1_024;
    private static final List<String> bulletins = new ArrayList<>();
    private static Path filePath;
    private static StorageService.Document document;
    private static StorageRepository.RepositoryState state = StorageRepository.RepositoryState.NEW;

    public static void init(MinecraftServer server) {
        Path dir = server.getServerDirectory().resolve("serverconfig").resolve("sef");
        init(dir);
    }

    static synchronized void init(Path dir) {
        filePath = dir.resolve("bulletin.json");
        load();
    }

    private static synchronized void load() {
        if (filePath == null) return;
        StorageService.Document candidate =
                StorageService.read(filePath, "operator bulletins", 1).orElse(null);
        if (candidate == null) {
            StorageRepository.RepositoryState detected = StorageLifecycle.stateFor(filePath);
            state = detected == StorageRepository.RepositoryState.MISSING && bulletins.isEmpty()
                    ? detected
                    : StorageRepository.RepositoryState.RECOVERY;
            return;
        }
        try {
            List<String> validated = validateSnapshot(candidate.data());
            bulletins.clear();
            bulletins.addAll(validated);
            document = candidate;
            state = StorageRepository.RepositoryState.READY;
            ServerEssentialsForge.LOGGER.info("[SEF] Loaded {} bulletin(s)", bulletins.size());
            if (candidate.migrated() && !save()) {
                state = StorageRepository.RepositoryState.ERROR;
            }
        } catch (RuntimeException exception) {
            state = StorageRepository.RepositoryState.RECOVERY;
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to load bulletins", exception);
        }
    }

    private static synchronized boolean save() {
        if (filePath == null || !StorageLifecycle.writable(state)) return false;
        try {
            StorageService.write(
                    filePath,
                    "operator bulletins",
                    1,
                    GSON.toJsonTree(bulletins),
                    document);
            document = StorageService.read(filePath, "operator bulletins", 1).orElse(document);
            state = StorageRepository.RepositoryState.READY;
            return true;
        } catch (IOException | RuntimeException exception) {
            state = StorageRepository.RepositoryState.ERROR;
            ServerEssentialsForge.LOGGER.error("[SEF] Failed to save bulletins", exception);
            return false;
        }
    }

    public static synchronized void showBulletins(ServerPlayer player) {
        if (bulletins.isEmpty()) return;
        if (!PermissionService.has(player, PermissionsHandler.opBulletinReceive)) return;
        player.sendSystemMessage(TextFormatter.stringToFormattedText("&6━━━━━━━━ Op Bulletin ━━━━━━━━"));
        for (String b : bulletins) {
            player.sendSystemMessage(TextFormatter.stringToFormattedText("&7• " + b));
        }
        player.sendSystemMessage(TextFormatter.stringToFormattedText("&6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("opbulletin")
            .requires(src -> PermissionService.has(src, PermissionsHandler.opBulletinManage))
            .then(Commands.literal("add")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String msg;
                        try {
                            msg = validateMessage(StringArgumentType.getString(ctx, "message"));
                            writable();
                        } catch (IllegalArgumentException | IllegalStateException exception) {
                            ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(
                                    "&c" + exception.getMessage()));
                            return 0;
                        }
                        if (bulletins.size() >= MAXIMUM_BULLETINS) {
                            ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(
                                    "&cBulletin capacity is full."));
                            return 0;
                        }
                        bulletins.add(msg);
                        if (!save()) {
                            bulletins.remove(bulletins.size() - 1);
                            ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(
                                    "&cThe bulletin could not be persisted."));
                            return 0;
                        }
                        ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
                            "&aAdded bulletin: &7" + msg), false);
                        return 1;
                    })))
            .then(Commands.literal("remove")
                .then(Commands.argument("index", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                    .executes(ctx -> {
                        int index = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "index") - 1;
                        if (index < 0 || index >= bulletins.size()) {
                            ctx.getSource().sendFailure(TextFormatter.stringToFormattedText("&cInvalid index"));
                            return 0;
                        }
                        try {
                            writable();
                        } catch (IllegalStateException exception) {
                            ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(
                                    "&c" + exception.getMessage()));
                            return 0;
                        }
                        String removed = bulletins.remove(index);
                        if (!save()) {
                            bulletins.add(index, removed);
                            ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(
                                    "&cThe bulletin removal could not be persisted."));
                            return 0;
                        }
                        ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText(
                            "&aRemoved bulletin: &7" + removed), false);
                        return 1;
                    })))
            .then(Commands.literal("list")
                .executes(ctx -> {
                    if (bulletins.isEmpty()) {
                        ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&7No bulletins configured"), false);
                        return 1;
                    }
                    ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&6━━━━━━━━ Op Bulletins ━━━━━━━━"), false);
                    for (int i = 0; i < bulletins.size(); i++) {
                        final int idx = i + 1;
                        final String b = bulletins.get(i);
                        ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&e" + idx + ". &7" + b), false);
                    }
                    return 1;
                }))
            .then(Commands.literal("clear")
                .executes(ctx -> {
                    try {
                        writable();
                    } catch (IllegalStateException exception) {
                        ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(
                                "&c" + exception.getMessage()));
                        return 0;
                    }
                    List<String> previous = List.copyOf(bulletins);
                    bulletins.clear();
                    if (!save()) {
                        bulletins.addAll(previous);
                        ctx.getSource().sendFailure(TextFormatter.stringToFormattedText(
                                "&cThe bulletin clear could not be persisted."));
                        return 0;
                    }
                    ctx.getSource().sendSuccess(() -> TextFormatter.stringToFormattedText("&aAll bulletins cleared"), false);
                    return 1;
                })));
    }

    static synchronized StorageRepository.RepositoryState state() {
        return state;
    }

    static synchronized List<String> bulletins() {
        return List.copyOf(bulletins);
    }

    private static List<String> validateSnapshot(JsonElement data) {
        if (data == null || !data.isJsonArray() || data.getAsJsonArray().size() > MAXIMUM_BULLETINS) {
            throw new IllegalStateException("Bulletin snapshot is missing or outside bounds");
        }
        List<String> loaded = new ArrayList<>();
        for (JsonElement element : data.getAsJsonArray()) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new IllegalStateException("Bulletin snapshot contains a non-text entry");
            }
            loaded.add(validateMessage(element.getAsString()));
        }
        return List.copyOf(loaded);
    }

    private static String validateMessage(String value) {
        String normalized = value == null ? "" : value.strip();
        if (normalized.isBlank()
                || normalized.length() > MAXIMUM_MESSAGE_LENGTH
                || normalized.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Bulletin message is outside bounds.");
        }
        return normalized;
    }

    private static void writable() {
        if (!StorageLifecycle.writable(state)) {
            throw new IllegalStateException("Bulletin storage is unavailable in " + state + " state.");
        }
    }
}
