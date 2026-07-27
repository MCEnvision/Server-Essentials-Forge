package com.enviouse.sef.disguise;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.kernel.policy.PlayerTargetPolicy;
import com.enviouse.sef.permissions.PermissionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.IntSupplier;

public final class DisguiseCommands {
    private DisguiseCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!ConfigHandler.config.enableDisguises.get()) {
            return;
        }
        dispatcher.register(root());
        dispatcher.register(Commands.literal("undisguise")
                .requires(source -> has(source, "commands.disguise.clear"))
                .executes(context -> execute(context.getSource(), "clear",
                        () -> clear(context.getSource(), player(context.getSource())))));
        dispatcher.register(abilityRoot("dability"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("disguise")
                .requires(source -> has(source, "commands.disguise"))
                .executes(context -> execute(context.getSource(), "status",
                        () -> status(context.getSource(), player(context.getSource()))));
        root.then(Commands.argument("entity_type", StringArgumentType.word())
                .requires(source -> has(source, "commands.disguise.mob"))
                .suggests((context, builder) -> {
                    KernelServices.disguises().supportedMobs()
                            .forEach(adapter -> builder.suggest(adapter.entityType()));
                    return builder.buildFuture();
                })
                .executes(context -> execute(context.getSource(), "set.mob",
                        () -> setMob(
                                context.getSource(),
                                player(context.getSource()),
                                StringArgumentType.getString(context, "entity_type")))));
        root.then(Commands.literal("mob")
                .requires(source -> has(source, "commands.disguise.mob"))
                .then(Commands.argument("entity_type", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            KernelServices.disguises().supportedMobs()
                                    .forEach(adapter -> builder.suggest(adapter.entityType()));
                            return builder.buildFuture();
                        })
                        .executes(context -> execute(context.getSource(), "set.mob",
                                () -> setMob(
                                        context.getSource(),
                                        player(context.getSource()),
                                        StringArgumentType.getString(context, "entity_type"))))));
        root.then(Commands.literal("player")
                .requires(source -> has(source, "commands.disguise.player"))
                .then(Commands.argument("profile", StringArgumentType.word())
                        .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                context.getSource().getServer().getPlayerNames(),
                                builder))
                        .executes(context -> execute(context.getSource(), "set.player",
                                () -> setPlayer(
                                        context.getSource(),
                                        player(context.getSource()),
                                        StringArgumentType.getString(context, "profile"))))));
        root.then(Commands.literal("preset")
                .requires(source -> has(source, "commands.disguise.preset"))
                .then(Commands.argument("preset_id", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            KernelServices.disguises().presets().forEach(value -> builder.suggest(value.id()));
                            return builder.buildFuture();
                        })
                        .executes(context -> execute(context.getSource(), "set.preset",
                                () -> setPreset(
                                        context.getSource(),
                                        player(context.getSource()),
                                        StringArgumentType.getString(context, "preset_id"))))));
        root.then(Commands.literal("clear")
                .requires(source -> has(source, "commands.disguise.clear"))
                .executes(context -> execute(context.getSource(), "clear",
                        () -> clear(context.getSource(), player(context.getSource()))))
                .then(Commands.argument("player", EntityArgument.player())
                        .requires(source -> has(source, "commands.disguise.clear.others"))
                        .executes(context -> execute(context.getSource(), "clear",
                                () -> clear(context.getSource(), target(context, "player"))))));
        root.then(Commands.literal("status")
                .requires(source -> has(source, "commands.disguise.status"))
                .executes(context -> execute(context.getSource(), "status",
                        () -> status(context.getSource(), player(context.getSource()))))
                .then(Commands.argument("player", EntityArgument.player())
                        .requires(source -> has(source, "commands.disguise.status.others"))
                        .executes(context -> execute(context.getSource(), "status",
                                () -> status(context.getSource(), target(context, "player"))))));
        root.then(Commands.literal("list")
                .requires(source -> has(source, "commands.disguise.list"))
                .executes(context -> execute(context.getSource(), "list",
                        () -> list(context.getSource()))));
        root.then(Commands.literal("preview")
                .requires(source -> has(source, "commands.disguise.preview"))
                .then(Commands.argument("entity_type", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            KernelServices.disguises().supportedMobs()
                                    .forEach(adapter -> builder.suggest(adapter.entityType()));
                            return builder.buildFuture();
                        })
                        .executes(context -> execute(context.getSource(), "preview",
                                () -> preview(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "entity_type"))))));
        root.then(presetManagementRoot());
        root.then(Commands.literal("set")
                .requires(source -> has(source, "commands.disguise.set.others"))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("entity_type", StringArgumentType.word())
                                .executes(context -> execute(context.getSource(), "set.mob",
                                        () -> setMob(
                                                context.getSource(),
                                                target(context, "player"),
                                                StringArgumentType.getString(context, "entity_type")))))));
        root.then(abilityRoot("ability"));
        root.then(Commands.literal("inspect")
                .requires(source -> has(source, "commands.disguise.inspect"))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> execute(context.getSource(), "inspect",
                                () -> inspect(context.getSource(), target(context, "player"))))));
        root.then(Commands.literal("conflicts")
                .requires(source -> has(source, "commands.disguise.conflicts"))
                .executes(context -> execute(context.getSource(), "conflicts",
                        () -> conflicts(context.getSource()))));
        root.then(optionsRoot());
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> presetManagementRoot() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("presets")
                .requires(source -> has(source, "commands.disguise.preset.manage"))
                .executes(context -> execute(context.getSource(), "preset.manage",
                        () -> listPresets(context.getSource())));
        root.then(Commands.literal("create")
                .then(Commands.argument("preset_id", StringArgumentType.word())
                        .then(Commands.argument("entity_type", StringArgumentType.word())
                                .executes(context -> execute(context.getSource(), "preset.manage",
                                        () -> createPreset(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "preset_id"),
                                                StringArgumentType.getString(context, "entity_type")))))));
        root.then(Commands.literal("delete")
                .then(Commands.argument("preset_id", StringArgumentType.word())
                        .executes(context -> execute(context.getSource(), "preset.manage",
                                () -> deletePreset(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "preset_id"))))));
        root.then(Commands.literal("enable")
                .then(Commands.argument("preset_id", StringArgumentType.word())
                        .then(Commands.argument("enabled", com.mojang.brigadier.arguments.BoolArgumentType.bool())
                                .executes(context -> execute(context.getSource(), "preset.manage",
                                        () -> setPresetEnabled(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "preset_id"),
                                                com.mojang.brigadier.arguments.BoolArgumentType.getBool(
                                                        context,
                                                        "enabled")))))));
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> optionsRoot() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("options")
                .requires(source -> has(source, "commands.disguise.options"))
                .executes(context -> execute(context.getSource(), "options",
                        () -> options(context.getSource())));
        root.then(Commands.literal("equipment")
                .requires(source -> has(source, "disguise.options.equipment"))
                .then(Commands.argument("policy", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (DisguiseService.EquipmentPolicy policy :
                                    DisguiseService.EquipmentPolicy.values()) {
                                builder.suggest(policy.name().toLowerCase(Locale.ROOT));
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> execute(context.getSource(), "options",
                                () -> setEquipmentOption(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "policy"))))));
        root.then(Commands.literal("label")
                .requires(source -> has(source, "disguise.options.name"))
                .then(Commands.argument("mode", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (DisguiseService.DisplayLabelMode mode :
                                    DisguiseService.DisplayLabelMode.values()) {
                                builder.suggest(mode.name().toLowerCase(Locale.ROOT));
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> execute(context.getSource(), "options",
                                () -> setLabelOption(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "mode"))))));
        root.then(Commands.literal("persistence")
                .requires(source -> has(source, "disguise.persist"))
                .then(Commands.argument("mode", StringArgumentType.word())
                        .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                List.of("none", "death", "reconnect", "restart"),
                                builder))
                        .executes(context -> execute(context.getSource(), "options",
                                () -> setPersistenceOption(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "mode"))))));
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> abilityRoot(String literal) {
        return Commands.literal(literal)
                .requires(source -> has(source, "commands.disguise.ability"))
                .then(Commands.argument("slot", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (DisguiseService.AbilitySlot slot : DisguiseService.AbilitySlot.values()) {
                                builder.suggest(slot.name().toLowerCase(Locale.ROOT));
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> execute(context.getSource(), "ability",
                                () -> ability(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "slot")))));
    }

    private static int setMob(CommandSourceStack source, ServerPlayer subject, String entityType) {
        if (subject == null) {
            return fail(source, "a player source is required");
        }
        if (!mayTarget(source, subject)) {
            return 0;
        }
        String normalizedType = entityType.contains(":")
                ? entityType.toLowerCase(Locale.ROOT)
                : "minecraft:" + entityType.toLowerCase(Locale.ROOT);
        if (!has(source, "disguise.type." + normalizedType.replace(':', '.'))) {
            return fail(source, "you do not have permission for that disguise type");
        }
        ActionResult<DisguiseService.DisguiseRecord> result = KernelServices.disguises().setMob(
                subject.getUUID(),
                normalizedType,
                actorId(source),
                has(source, "disguise.traits") && ConfigHandler.config.disguiseTraitsEnabled.get(),
                has(source, "disguise.abilities") && ConfigHandler.config.disguiseAbilitiesEnabled.get(),
                null);
        if (!result.successful()) {
            return fail(source, result.detail());
        }
        refresh(subject);
        success(source, subject.getGameProfile().getName() + " is disguised as " + result.value().reference());
        return 1;
    }

    private static int setPlayer(
            CommandSourceStack source,
            ServerPlayer subject,
            String profileName
    ) {
        if (subject == null) {
            return fail(source, "a player source is required");
        }
        if (!mayTarget(source, subject)) {
            return 0;
        }
        ServerPlayer profilePlayer = source.getServer().getPlayerList().getPlayerByName(profileName);
        DisguiseService.ProfileSnapshot profile;
        if (profilePlayer != null) {
            if ((profilePlayer.hasPermissions(2)
                || PermissionService.has(
                profilePlayer,
                PermissionsHandler.phasePermission("disguise.exempt")))
                && !has(source, "disguise.protected_identity")) {
                return fail(source, "that player profile is protected from impersonation");
            }
            com.mojang.authlib.properties.Property textures = profilePlayer.getGameProfile()
                    .getProperties()
                    .get("textures")
                    .stream()
                    .filter(com.mojang.authlib.properties.Property::hasSignature)
                    .findFirst()
                    .orElse(null);
            if (textures == null) {
                return fail(source, "profile does not have a trusted signed texture");
            }
            profile = new DisguiseService.ProfileSnapshot(
                    profilePlayer.getUUID(),
                    profilePlayer.getGameProfile().getName(),
                    textures.value(),
                    textures.signature(),
                    true,
                    Instant.now(),
                    Instant.now().plusSeconds(3600));
            KernelServices.disguises().cacheProfile(profile);
        } else {
            if (!has(source, "disguise.protected_identity")) {
                return fail(source, "offline profile disguises require protected identity permission");
            }
            profile = KernelServices.disguises().profile(profileName).orElse(null);
            if (profile == null) {
                return fail(source, "profile is not present in the trusted server cache");
            }
        }
        ActionResult<DisguiseService.DisguiseRecord> result = KernelServices.disguises().setPlayer(
                subject.getUUID(),
                profile,
                actorId(source),
                null);
        if (!result.successful()) {
            return fail(source, result.detail());
        }
        refresh(subject);
        success(source, subject.getGameProfile().getName()
                + " is disguised as player " + profile.profileName());
        return 1;
    }

    private static int setPreset(CommandSourceStack source, ServerPlayer subject, String presetId) {
        if (subject == null) {
            return fail(source, "a player source is required");
        }
        if (!mayTarget(source, subject)) {
            return 0;
        }
        DisguiseService.DisguisePreset preset =
                KernelServices.disguises().preset(presetId).orElse(null);
        if (preset == null || !preset.enabled()) {
            return fail(source, "disguise preset not found");
        }
        if (preset.kind() == DisguiseService.DisguiseKind.MOB) {
            String typePermission = "disguise.type." + preset.reference().replace(':', '.');
            if (!has(source, typePermission)) {
                return fail(source, "you do not have permission for that preset disguise type");
            }
        } else if (!has(source, "disguise.protected_identity")) {
            return fail(source, "player profile presets require protected identity permission");
        }
        ActionResult<DisguiseService.DisguiseRecord> result = KernelServices.disguises().setPreset(
                subject.getUUID(),
                presetId,
                actorId(source),
                null);
        if (!result.successful()) {
            return fail(source, result.detail());
        }
        refresh(subject);
        success(source, subject.getGameProfile().getName() + " is using disguise preset " + presetId);
        return 1;
    }

    private static int clear(CommandSourceStack source, ServerPlayer subject) {
        if (subject == null) {
            return fail(source, "a player source is required");
        }
        if (!mayTarget(source, subject)) {
            return 0;
        }
        ActionResult<Void> result = KernelServices.disguises().clear(
                subject.getUUID(),
                actorId(source),
                DisguiseService.ClearReason.COMMAND);
        if (!result.successful()) {
            return fail(source, result.detail());
        }
        KernelServices.disguiseProxyIds().releaseSubject(subject.getUUID());
        refresh(subject);
        success(source, subject.getGameProfile().getName() + " is no longer disguised");
        return 1;
    }

    private static int status(CommandSourceStack source, ServerPlayer subject) {
        if (subject == null) {
            return fail(source, "a player source is required");
        }
        DisguiseService.DisguiseRecord record =
                KernelServices.disguises().active(subject.getUUID()).orElse(null);
        if (record == null) {
            info(source, subject.getGameProfile().getName() + " is not disguised");
            return 1;
        }
        info(source, subject.getGameProfile().getName() + " is disguised as " + record.reference()
                + ", kind " + record.kind().name().toLowerCase(Locale.ROOT)
                + ", revision " + record.revision());
        info(source, "enhanced clients can render self disguise in third person");
        info(source, "vanilla clients receive abilities and public projection but cannot reliably see their own full model");
        return 1;
    }

    private static int list(CommandSourceStack source) {
        List<DisguiseService.MobAdapter> adapters = KernelServices.disguises().supportedMobs();
        adapters.forEach(adapter -> info(source,
                adapter.entityType()
                        + ", enhanced " + adapter.enhancedSupported()
                        + ", vanilla proxy " + adapter.vanillaProxySupported()));
        return Math.max(1, adapters.size());
    }

    private static int preview(CommandSourceStack source, String entityType) {
        String normalized = entityType.contains(":")
                ? entityType.toLowerCase(Locale.ROOT)
                : "minecraft:" + entityType.toLowerCase(Locale.ROOT);
        DisguiseService.MobAdapter adapter = KernelServices.disguises().supportedMobs().stream()
                .filter(value -> value.entityType().equals(normalized))
                .findFirst()
                .orElse(null);
        if (adapter == null) {
            return fail(source, "entity type is not a supported disguise");
        }
        if (!has(source, "disguise.type." + normalized.replace(':', '.'))) {
            return fail(source, "you do not have permission for that disguise type");
        }
        info(source, adapter.displayName() + ", " + adapter.entityType()
                + ", enhanced " + adapter.enhancedSupported()
                + ", vanilla proxy " + adapter.vanillaProxySupported());
        info(source, "traits " + adapter.traits().stream()
                .map(value -> value.name().toLowerCase(Locale.ROOT))
                .toList());
        info(source, "abilities " + adapter.abilities().values().stream()
                .map(DisguiseService.AbilityDefinition::id)
                .toList());
        return 1;
    }

    private static int listPresets(CommandSourceStack source) {
        List<DisguiseService.DisguisePreset> presets = KernelServices.disguises().presets();
        presets.forEach(preset -> info(source,
                preset.id() + ", " + preset.reference()
                        + ", enabled " + preset.enabled()
                        + ", revision " + preset.revision()));
        return Math.max(1, presets.size());
    }

    private static int createPreset(
            CommandSourceStack source,
            String presetId,
            String entityType
    ) {
        String normalized = entityType.contains(":")
                ? entityType.toLowerCase(Locale.ROOT)
                : "minecraft:" + entityType.toLowerCase(Locale.ROOT);
        UUID actor = actorId(source);
        DisguiseService.DisguisePreset requested = new DisguiseService.DisguisePreset(
                presetId,
                presetId,
                DisguiseService.DisguiseKind.MOB,
                normalized,
                null,
                DisguiseService.DisplayLabelMode.NICKNAME_PLUS_DISGUISE,
                DisguiseService.EquipmentPolicy.SHOW_REAL_EQUIPMENT,
                DisguiseService.HitboxPolicy.PLAYER,
                DisguiseService.ViewerPolicy.EVERYONE,
                false,
                false,
                DisguiseService.PersistencePolicy.defaults(),
                true,
                1L,
                actor,
                Instant.now());
        return result(
                source,
                KernelServices.disguises().savePreset(requested, 0L, actor),
                "disguise preset created");
    }

    private static int deletePreset(CommandSourceStack source, String presetId) {
        DisguiseService.DisguisePreset preset = KernelServices.disguises().presets().stream()
                .filter(value -> value.id().equalsIgnoreCase(presetId))
                .findFirst()
                .orElse(null);
        if (preset == null) {
            return fail(source, "disguise preset not found");
        }
        return result(
                source,
                KernelServices.disguises().deletePreset(
                        preset.id(),
                        actorId(source),
                        preset.revision()),
                "disguise preset deleted");
    }

    private static int setPresetEnabled(
            CommandSourceStack source,
            String presetId,
            boolean enabled
    ) {
        DisguiseService.DisguisePreset current = KernelServices.disguises().presets().stream()
                .filter(value -> value.id().equalsIgnoreCase(presetId))
                .findFirst()
                .orElse(null);
        if (current == null) {
            return fail(source, "disguise preset not found");
        }
        DisguiseService.DisguisePreset requested = new DisguiseService.DisguisePreset(
                current.id(),
                current.displayName(),
                current.kind(),
                current.reference(),
                current.profileId(),
                current.labelMode(),
                current.equipmentPolicy(),
                current.hitboxPolicy(),
                current.viewerPolicy(),
                current.traitsEnabled(),
                current.abilitiesEnabled(),
                current.persistence(),
                enabled,
                current.revision(),
                actorId(source),
                Instant.now());
        return result(
                source,
                KernelServices.disguises().savePreset(
                        requested,
                        current.revision(),
                        actorId(source)),
                "disguise preset updated");
    }

    private static int inspect(CommandSourceStack source, ServerPlayer subject) {
        if (subject == null) {
            return fail(source, "player is unavailable");
        }
        info(source, "real identity " + subject.getGameProfile().getName() + ", " + subject.getUUID());
        return status(source, subject);
    }

    private static int conflicts(CommandSourceStack source) {
        info(source, "vanish visibility is evaluated before disguise projection");
        info(source, "nickname projection owns visible labels and signed chat keeps the authenticated player");
        info(source, "the default hitbox remains the real player hitbox");
        return 1;
    }

    private static int options(CommandSourceStack source) {
        DisguiseService.Settings settings = KernelServices.disguises().settings();
        info(source, "traits " + settings.traitsEnabled()
                + ", abilities " + settings.abilitiesEnabled()
                + ", clear on logout " + settings.clearOnLogout()
                + ", clear on death " + settings.clearOnDeath());
        return 1;
    }

    private static int setEquipmentOption(CommandSourceStack source, String policyName) {
        DisguiseService.EquipmentPolicy policy;
        try {
            policy = DisguiseService.EquipmentPolicy.valueOf(policyName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fail(source, "unknown disguise equipment policy");
        }
        return updateSelfOptions(source, null, policy, null);
    }

    private static int setLabelOption(CommandSourceStack source, String modeName) {
        DisguiseService.DisplayLabelMode mode;
        try {
            mode = DisguiseService.DisplayLabelMode.valueOf(modeName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fail(source, "unknown disguise label mode");
        }
        return updateSelfOptions(source, mode, null, null);
    }

    private static int setPersistenceOption(CommandSourceStack source, String modeName) {
        DisguiseService.PersistencePolicy persistence = switch (modeName.toLowerCase(Locale.ROOT)) {
            case "none" -> DisguiseService.PersistencePolicy.defaults();
            case "death" -> new DisguiseService.PersistencePolicy(true, false, false);
            case "reconnect" -> new DisguiseService.PersistencePolicy(true, true, false);
            case "restart" -> new DisguiseService.PersistencePolicy(true, true, true);
            default -> null;
        };
        if (persistence == null) {
            return fail(source, "unknown disguise persistence mode");
        }
        return updateSelfOptions(source, null, null, persistence);
    }

    private static int updateSelfOptions(
            CommandSourceStack source,
            DisguiseService.DisplayLabelMode label,
            DisguiseService.EquipmentPolicy equipment,
            DisguiseService.PersistencePolicy persistence
    ) {
        ServerPlayer subject = player(source);
        if (subject == null) {
            return fail(source, "disguise options require a player");
        }
        DisguiseService.DisguiseRecord current =
                KernelServices.disguises().active(subject.getUUID()).orElse(null);
        if (current == null) {
            return fail(source, "player is not disguised");
        }
        ActionResult<DisguiseService.DisguiseRecord> result =
                KernelServices.disguises().updateOptions(
                        subject.getUUID(),
                        label,
                        equipment,
                        null,
                        persistence,
                        subject.getUUID(),
                        current.revision());
        if (result.successful()) {
            refresh(subject);
        }
        return result(source, result, "disguise options updated");
    }

    private static int ability(CommandSourceStack source, String slotName) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            return fail(source, "disguise abilities require a player");
        }
        final DisguiseService.AbilitySlot slot;
        try {
            slot = DisguiseService.AbilitySlot.valueOf(slotName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fail(source, "unknown disguise ability slot");
        }
        ActionResult<Void> operation = DisguiseAbilityExecutor.activate(player, slot);
        if (!operation.successful()) {
            return fail(source, operation.detail());
        }
        success(source, "disguise ability activated");
        return 1;
    }

    private static void refresh(ServerPlayer subject) {
        subject.refreshTabListName();
        com.enviouse.sef.gui.protocol.SefGuiRuntime.refreshIdentityProjections(subject.getServer());
        com.enviouse.sef.gui.protocol.SefGuiServer.sendDisguiseSnapshot(subject.getServer());
    }

    private static int execute(CommandSourceStack source, String action, IntSupplier operation) {
        String actionId = "sef:disguise." + action;
        String permission = switch (action) {
            case "set.mob" -> "commands.disguise.mob";
            case "set.player" -> "commands.disguise.player";
            case "set.preset" -> "commands.disguise.preset";
            default -> "commands.disguise." + action;
        };
        return KernelCommandExecutor.execute(
                source,
                actionId,
                Map.of("operation", action),
                operation,
                PermissionsHandler.phasePermission(permission));
    }

    private static boolean has(CommandSourceStack source, String permission) {
        String normalized = permission.startsWith("sef.") ? permission : "sef." + permission;
        var node = PermissionsHandler.phasePermission(normalized);
        return node != null && PermissionService.has(source, node);
    }

    private static UUID actorId(CommandSourceStack source) {
        return source.getEntity() == null ? new UUID(0L, 0L) : source.getEntity().getUUID();
    }

    private static ServerPlayer player(CommandSourceStack source) {
        return source.getEntity() instanceof ServerPlayer player ? player : null;
    }

    private static ServerPlayer target(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
            String name
    ) {
        try {
            return EntityArgument.getPlayer(context, name);
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            return null;
        }
    }

    private static boolean mayTarget(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer actor = source.getPlayer();
        if (actor != null && actor.getUUID().equals(target.getUUID())) {
            return true;
        }
        var decision = PlayerTargetPolicy.decide(
                source,
                target,
                PermissionsHandler.phasePermission("disguise.hierarchy.bypass"),
                PermissionsHandler.phasePermission("disguise.exempt"),
                PermissionsHandler.phasePermission("disguise.exemption.bypass"),
                false,
                false);
        if (!decision.allowed()) {
            fail(source, decision.exempt()
                    ? "that player is exempt from disguise changes"
                    : "you cannot change a player at or above your hierarchy");
            return false;
        }
        return true;
    }

    private static int fail(CommandSourceStack source, String message) {
        source.sendFailure(TextFormatter.stringToFormattedText("&c" + message));
        return 0;
    }

    private static int result(CommandSourceStack source, ActionResult<?> result, String message) {
        if (!result.successful()) {
            return fail(source, result.detail().isBlank()
                    ? result.reason().name().toLowerCase(Locale.ROOT)
                    : result.detail());
        }
        success(source, message);
        return 1;
    }

    private static void info(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&7" + message), false);
    }

    private static void success(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&a" + message), false);
    }
}
