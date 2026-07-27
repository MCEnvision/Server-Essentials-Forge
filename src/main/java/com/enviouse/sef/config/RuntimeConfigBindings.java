package com.enviouse.sef.config;

import com.enviouse.sef.config.modules.ModuleConfigRegistry;
import com.enviouse.sef.config.modules.ModuleConfigService;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class RuntimeConfigBindings {
    private static final Set<String> UNBOUND_SECRETS = Set.of("discordBotToken");
    private static final Map<String, String> TOGGLE_OWNERS = Map.ofEntries(
            Map.entry("enableChatFormatting", "messages"),
            Map.entry("enableMessagingSystem", "private_messages"),
            Map.entry("enableChatReplies", "private_messages"),
            Map.entry("enableHelpOp", "social"),
            Map.entry("enableAdminChat", "social"),
            Map.entry("enableOpBulletin", "social"),
            Map.entry("enableFilterSystem", "messages"),
            Map.entry("enableColorsCommand", "messages"),
            Map.entry("enableTabListIntegration", "hud"),
            Map.entry("enableCustomTabHeaderFooter", "hud"),
            Map.entry("enableWhoisCommand", "nicknames"),
            Map.entry("enableChatNicknameCommand", "nicknames"),
            Map.entry("enableDiscordBotIntegration", "integrations"),
            Map.entry("enableVanishSystem", "vanish"),
            Map.entry("enableMuteSystem", "mutes"),
            Map.entry("enableWarnSystem", "warnings"),
            Map.entry("enableFreezeSystem", "freeze"),
            Map.entry("enableCheckAlts", "privacy"),
            Map.entry("enableBannedItems", "moderation"),
            Map.entry("enableDisableBuilding", "building_control"),
            Map.entry("enableInvLock", "inventory_lock"),
            Map.entry("enableSudo", "sudo"),
            Map.entry("enableInvSee", "inventory"),
            Map.entry("enableClearChat", "moderation"),
            Map.entry("enableCountdown", "displays"),
            Map.entry("enableMotdSystem", "displays"),
            Map.entry("enableAnnouncements", "displays"),
            Map.entry("enableCraftingTableCommand", "craft"),
            Map.entry("enableAnvilCommand", "anvil"),
            Map.entry("enableEnchantingTableCommand", "enchanting"),
            Map.entry("enableSuperEnchantingTableCommand", "super_enchanting"),
            Map.entry("enableRepairCommand", "repair"),
            Map.entry("enableTeleportEssentials", "direct_teleport"),
            Map.entry("enableHomes", "homes"),
            Map.entry("enableTeleportRequests", "teleport_requests"),
            Map.entry("enableBack", "back"),
            Map.entry("enableSpawnCommands", "spawn"),
            Map.entry("enableServerWarps", "warps"),
            Map.entry("enablePlayerWarps", "player_warps"),
            Map.entry("enableRandomTeleport", "random_teleport"),
            Map.entry("enableDirectTeleport", "direct_teleport"),
            Map.entry("enableSocialEssentials", "social"),
            Map.entry("enableSocialSpy", "social_spy"),
            Map.entry("enableMail", "mail"),
            Map.entry("enableConnectionMessages", "connection_messages"),
            Map.entry("enableReminders", "reminders"),
            Map.entry("enableCustomText", "community"),
            Map.entry("enableModerationEssentials", "moderation"),
            Map.entry("enableCommandSpy", "command_spy"),
            Map.entry("enableJails", "jails"),
            Map.entry("enableAdditionalWorkstations", "workstations"),
            Map.entry("enableKits", "kits"),
            Map.entry("enableInventoryUtilities", "inventory"),
            Map.entry("enablePlayerUtilities", "player_utilities"),
            Map.entry("enableGamemodeShortcuts", "gamemode"),
            Map.entry("enableItemShortcut", "items"),
            Map.entry("enableEconomy", "economy"),
            Map.entry("enableEconomySigns", "economy_signs"));

    private RuntimeConfigBindings() {
    }

    public static List<Binding> inspect(Object config) {
        Objects.requireNonNull(config, "config");
        List<Binding> bindings = new ArrayList<>();
        for (Field field : config.getClass().getFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    || !RuntimeConfigValue.class.isAssignableFrom(field.getType())
                    || UNBOUND_SECRETS.contains(field.getName())) {
                continue;
            }
            try {
                RuntimeConfigValue<?> value = (RuntimeConfigValue<?>) field.get(config);
                String moduleId = owner(field.getName());
                bindings.add(new Binding(
                        field.getName(),
                        moduleId,
                        "runtime." + snakeCase(field.getName()),
                        value,
                        applyClass(field.getName()),
                        sensitivity(field.getName())));
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("runtime configuration field is inaccessible", exception);
            }
        }
        bindings.sort(Comparator.comparing(Binding::moduleId).thenComparing(Binding::settingPath));
        Map<String, Binding> byTarget = new LinkedHashMap<>();
        for (Binding binding : bindings) {
            String key = binding.moduleId() + "." + binding.settingPath();
            if (byTarget.putIfAbsent(key, binding) != null) {
                throw new IllegalStateException("duplicate runtime configuration binding");
            }
        }
        return List.copyOf(bindings);
    }

    public static void publish(List<Binding> bindings, ModuleConfigService service) {
        for (Binding binding : bindings) {
            binding.value().apply(service.value(binding.moduleId(), binding.settingPath()));
        }
    }

    private static ModuleConfigRegistry.ApplyClass applyClass(String fieldName) {
        String normalized = fieldName.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("enable")
                || normalized.contains("provider")
                || normalized.contains("ownershipmode")
                || normalized.contains("disableftbinvsee")
                || normalized.startsWith("kernel")
                || normalized.startsWith("economy")
                || normalized.startsWith("fancytags")
                || normalized.startsWith("securityaudit")
                || normalized.startsWith("maximumkit")
                || normalized.startsWith("commandspy")) {
            return ModuleConfigRegistry.ApplyClass.RESTART_REQUIRED;
        }
        return ModuleConfigRegistry.ApplyClass.LIVE;
    }

    private static ModuleConfigRegistry.Sensitivity sensitivity(String fieldName) {
        String normalized = fieldName.toLowerCase(Locale.ROOT);
        if (normalized.contains("address")
                || normalized.contains("audit")
                || normalized.contains("sudo")
                || normalized.contains("allowedcommands")
                || normalized.contains("deniedcommands")) {
            return ModuleConfigRegistry.Sensitivity.SENSITIVE;
        }
        return ModuleConfigRegistry.Sensitivity.PUBLIC;
    }

    private static String owner(String fieldName) {
        String toggle = TOGGLE_OWNERS.get(fieldName);
        if (toggle != null) {
            return toggle;
        }
        String value = fieldName.toLowerCase(Locale.ROOT);
        if (value.startsWith("fancytags")) return "fancy_tags";
        if (value.startsWith("disguise")) return "disguise";
        if (value.startsWith("economysign")) return "economy_signs";
        if (value.startsWith("economy")) return "economy";
        if (value.startsWith("invsee")) return "inventory";
        if (value.startsWith("invlock")) return "inventory_lock";
        if (value.startsWith("kit") || value.startsWith("maximumkit")) return "kits";
        if (value.startsWith("itemgive")) return "items";
        if (value.startsWith("maximumfly") || value.startsWith("maximumwalk")
                || value.startsWith("enablesuicide")) return "player_utilities";
        if (value.startsWith("randomteleport")) return "random_teleport";
        if (value.startsWith("playerwarp") || value.startsWith("defaultplayerwarp")) return "player_warps";
        if (value.startsWith("defaulthome")) return "homes";
        if (value.startsWith("teleportrequest")) return "teleport_requests";
        if (value.startsWith("teleport") || value.startsWith("ownvanillateleport")) return "direct_teleport";
        if (value.startsWith("socialspy")) return "social_spy";
        if (value.startsWith("private") || value.startsWith("msg") || value.startsWith("reply")
                || value.startsWith("clicktomessage") || value.startsWith("clicktoreply")
                || value.startsWith("noreply") || value.startsWith("message")) return "private_messages";
        if (value.startsWith("mail")) return "mail";
        if (value.startsWith("defaultjoin") || value.startsWith("defaultleave")) return "connection_messages";
        if (value.startsWith("nickname") || value.startsWith("maximumnickname")
                || value.startsWith("minimumnickname") || value.startsWith("enablewhois")
                || value.startsWith("enablechatnickname")) return "nicknames";
        if (value.startsWith("moderation")) return "moderation";
        if (value.startsWith("commandspy")) return "command_spy";
        if (value.startsWith("filelogging")) return "logger";
        if (value.startsWith("warn")) return "warnings";
        if (value.startsWith("freeze")) return "freeze";
        if (value.startsWith("mute") || value.startsWith("unmute") || value.startsWith("enableftbmute")
                || value.startsWith("sendmuted")) return "mutes";
        if (value.startsWith("db")) return "building_control";
        if (value.startsWith("checkalts") || value.startsWith("alttracking")) return "privacy";
        if (value.startsWith("sudo")) return "sudo";
        if (value.startsWith("run") || value.startsWith("silentactor")) return "run_and_silent";
        if (value.startsWith("fake")) return "fake_actions";
        if (value.startsWith("kernelmaximumaliases")) return "aliases";
        if (value.startsWith("kernelmaximumbundle")) return "bundles";
        if (value.startsWith("kernel")) return "commands";
        if (value.startsWith("gui") || value.startsWith("enableenhancedgui")) return "gui";
        if (value.startsWith("securityaudit")) return "audit";
        if (value.startsWith("tab") || value.startsWith("maxprefix")
                || value.startsWith("maxsuffix")) return "hud";
        if (value.startsWith("superenchanting") || value.startsWith("enableadministrativeenchanting")
                || value.startsWith("enablesuperenchantingtablealias")) return "super_enchanting";
        if (value.startsWith("enableenchantingtablealias")) return "enchanting";
        if (value.startsWith("enableanvilalias")) return "anvil";
        if (value.startsWith("enablecraftalias")) return "craft";
        if (value.startsWith("repair")) return "repair";
        if (value.startsWith("enablecartography") || value.startsWith("enablegrindstone")
                || value.startsWith("enableloom") || value.startsWith("enablesmithing")
                || value.startsWith("enablestonecutter") || value.startsWith("workstation")) {
            return "workstations";
        }
        if (value.startsWith("announcement") || value.startsWith("countdown")
                || value.startsWith("applymotd") || value.startsWith("toggle")) return "displays";
        if (value.startsWith("optionalclient")) return "reminders";
        if (value.startsWith("discord") || value.startsWith("enableftb")
                || value.startsWith("enableluckperms")) return "integrations";
        if (value.contains("format") || value.contains("message") || value.contains("sound")
                || value.contains("hover") || value.startsWith("enabletimestamp")
                || value.startsWith("enablemarkdown") || value.startsWith("metajoin")) return "messages";
        return "core";
    }

    private static String snakeCase(String input) {
        StringBuilder output = new StringBuilder(input.length() + 8);
        for (int index = 0; index < input.length(); index++) {
            char current = input.charAt(index);
            if (Character.isUpperCase(current) && index > 0) {
                output.append('_');
            }
            output.append(Character.toLowerCase(current));
        }
        return output.toString();
    }

    public record Binding(
            String fieldName,
            String moduleId,
            String settingPath,
            RuntimeConfigValue<?> value,
            ModuleConfigRegistry.ApplyClass applyClass,
            ModuleConfigRegistry.Sensitivity sensitivity
    ) {
        public Binding {
            Objects.requireNonNull(fieldName, "fieldName");
            Objects.requireNonNull(moduleId, "moduleId");
            Objects.requireNonNull(settingPath, "settingPath");
            Objects.requireNonNull(value, "value");
            Objects.requireNonNull(applyClass, "applyClass");
            Objects.requireNonNull(sensitivity, "sensitivity");
        }
    }
}
