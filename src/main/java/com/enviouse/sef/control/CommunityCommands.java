package com.enviouse.sef.control;

import com.enviouse.sef.TextFormatter;
import com.enviouse.sef.automation.BundleService;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.kernel.ActionResult;
import com.enviouse.sef.kernel.KernelCommandExecutor;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.teleport.SafeTeleportService;
import com.enviouse.sef.teleport.SavedLocation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CommunityCommands {
    private static final int PAGE_SIZE = 10;

    private CommunityCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerRules(dispatcher);
        registerReward(dispatcher, "rewards", "playtime_rewards");
        registerReward(dispatcher, "daily", "daily_rewards");
        registerReward(dispatcher, "weekly", "weekly_rewards");
        registerReports(dispatcher);
        registerFriends(dispatcher);
        registerBlocks(dispatcher);
        registerWaypoints(dispatcher);
        registerPolls(dispatcher);
        registerEvents(dispatcher);
        registerKnowledge(dispatcher);
        registerInvites(dispatcher);
        registerMentions(dispatcher);
        registerOnboarding(dispatcher);
        registerSleepVote(dispatcher);
        registerDeathLocation(dispatcher);
        registerReviewRequests(dispatcher);
        registerCalendar(dispatcher);
    }

    private static void registerRules(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rules")
                .requires(source -> can(source, "sef:control.rules.view"))
                .executes(context -> showRules(context.getSource()))
                .then(Commands.literal("accept")
                        .requires(source -> can(source, "sef:control.rules.accept"))
                        .executes(context -> acceptRules(context.getSource()))));
    }

    private static void registerReward(
            CommandDispatcher<CommandSourceStack> dispatcher,
            String root,
            String feature
    ) {
        dispatcher.register(Commands.literal(root)
                .requires(source -> can(source, "sef:control." + feature + ".view"))
                .executes(context -> rewardStatus(context.getSource(), feature))
                .then(Commands.literal("claim")
                        .requires(source -> can(source, "sef:control." + feature + ".claim"))
                        .executes(context -> claimReward(context.getSource(), feature))));
    }

    private static void registerReports(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("report")
                .requires(source -> can(source, "sef:control.reports.submit"))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("description", StringArgumentType.greedyString())
                                .executes(context -> submitReport(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player"),
                                        StringArgumentType.getString(context, "description"))))));
        dispatcher.register(Commands.literal("ticket")
                .requires(source -> can(source, "sef:control.tickets.submit"))
                .then(Commands.argument("description", StringArgumentType.greedyString())
                        .executes(context -> submitTicket(
                                context.getSource(),
                                StringArgumentType.getString(context, "description")))));
    }

    private static void registerFriends(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("friend")
                .requires(source -> can(source, "sef:control.friends.view"))
                .executes(context -> listRelationships(context.getSource()))
                .then(Commands.literal("add")
                        .requires(source -> can(source, "sef:control.friends.request"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> requestFriend(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("accept")
                        .requires(source -> can(source, "sef:control.friends.accept"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> acceptFriend(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("remove")
                        .requires(source -> can(source, "sef:control.friends.remove"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> removeFriend(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "player"))))));
    }

    private static void registerBlocks(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("blocks")
                .requires(source -> can(source, "sef:control.interaction_blocks.view"))
                .executes(context -> listBlocks(context.getSource()))
                .then(Commands.literal("add")
                        .requires(source -> can(source, "sef:control.interaction_blocks.set"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("interaction", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            interactionTypes()
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> setBlock(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "interaction"),
                                                true)))))
                .then(Commands.literal("remove")
                        .requires(source -> can(source, "sef:control.interaction_blocks.set"))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("interaction", StringArgumentType.word())
                                        .executes(context -> setBlock(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "interaction"),
                                                false))))));
    }

    private static void registerWaypoints(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("waypoint")
                .requires(source -> can(source, "sef:control.waypoints.view"))
                .executes(context -> listWaypoints(context.getSource()))
                .then(Commands.literal("set")
                        .requires(source -> can(source, "sef:control.waypoints.set"))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> setWaypoint(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("remove")
                        .requires(source -> can(source, "sef:control.waypoints.remove"))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> removeWaypoint(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("go")
                        .requires(source -> can(source, "sef:control.waypoints.go"))
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> goWaypoint(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "name"))))));
    }

    private static void registerPolls(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("poll")
                .requires(source -> can(source, "sef:control.polls.view"))
                .executes(context -> listControlRecords(context.getSource(), "polls"))
                .then(Commands.literal("vote")
                        .requires(source -> can(source, "sef:control.polls.vote"))
                        .then(Commands.argument("poll", StringArgumentType.word())
                                .then(Commands.argument("choice", StringArgumentType.greedyString())
                                        .executes(context -> vote(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "poll"),
                                                StringArgumentType.getString(context, "choice")))))));
    }

    private static void registerEvents(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("events")
                .requires(source -> can(source, "sef:control.community_events.view"))
                .executes(context -> listControlRecords(context.getSource(), "community_events"))
                .then(Commands.literal("join")
                        .requires(source -> can(source, "sef:control.community_events.join"))
                        .then(Commands.argument("event", StringArgumentType.word())
                                .executes(context -> eventRegistration(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "event"),
                                        true))))
                .then(Commands.literal("leave")
                        .requires(source -> can(source, "sef:control.community_events.leave"))
                        .then(Commands.argument("event", StringArgumentType.word())
                                .executes(context -> eventRegistration(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "event"),
                                        false)))));
    }

    private static void registerKnowledge(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("knowledge")
                .requires(source -> can(source, "sef:control.knowledge.view"))
                .executes(context -> listControlRecords(context.getSource(), "knowledge"))
                .then(Commands.literal("read")
                        .then(Commands.argument("article", StringArgumentType.word())
                                .executes(context -> readArticle(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "article")))))
                .then(Commands.literal("bookmark")
                        .requires(source -> can(source, "sef:control.knowledge.bookmark"))
                        .then(Commands.argument("article", StringArgumentType.word())
                                .executes(context -> bookmarkArticle(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "article"))))));
    }

    private static void registerInvites(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("invite")
                .requires(source -> can(source, "sef:control.invites.redeem"))
                .then(Commands.literal("redeem")
                        .then(Commands.argument("code", StringArgumentType.word())
                                .executes(context -> redeemInvite(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "code"))))));
    }

    private static void registerMentions(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mentions")
                .requires(source -> can(source, "sef:control.mentions.view"))
                .executes(context -> mentionStatus(context.getSource()))
                .then(Commands.literal("mode")
                        .requires(source -> can(source, "sef:control.mentions.set"))
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    List.of("all", "friends", "staff", "off").forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> setMentionMode(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "mode")))))
                .then(Commands.literal("sound")
                        .requires(source -> can(source, "sef:control.mentions.set"))
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> setMentionSound(
                                        context.getSource(),
                                        BoolArgumentType.getBool(context, "enabled"))))));
    }

    private static void registerOnboarding(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("onboarding")
                .requires(source -> can(source, "sef:control.onboarding.view"))
                .executes(context -> onboardingStatus(context.getSource()))
                .then(Commands.literal("step")
                        .requires(source -> can(source, "sef:control.onboarding.complete"))
                        .then(Commands.argument("step", StringArgumentType.word())
                                .executes(context -> completeOnboardingStep(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "step")))))
                .then(Commands.literal("dismiss")
                        .requires(source -> can(source, "sef:control.onboarding.dismiss"))
                        .executes(context -> dismissOnboarding(context.getSource()))));
    }

    private static void registerSleepVote(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sleepvote")
                .requires(source -> can(source, "sef:control.sleep_vote.view"))
                .executes(context -> sleepVoteStatus(context.getSource()))
                .then(Commands.literal("status")
                        .executes(context -> sleepVoteStatus(context.getSource())))
                .then(Commands.literal("yes")
                        .requires(source -> can(source, "sef:control.sleep_vote.vote"))
                        .executes(context -> setSleepVote(context.getSource(), true)))
                .then(Commands.literal("no")
                        .requires(source -> can(source, "sef:control.sleep_vote.vote"))
                        .executes(context -> setSleepVote(context.getSource(), false))));
    }

    private static void registerDeathLocation(CommandDispatcher<CommandSourceStack> dispatcher) {
        var death = Commands.literal("deathlocation")
                .requires(source -> can(source, "sef:control.death_compass.view"))
                .executes(context -> deathLocation(context.getSource()))
                .then(Commands.literal("clear")
                        .requires(source -> can(source, "sef:control.death_compass.clear"))
                        .executes(context -> clearDeathLocation(context.getSource())));
        dispatcher.register(death);
        dispatcher.register(Commands.literal("deathcompass")
                .requires(source -> can(source, "sef:control.death_compass.view"))
                .executes(context -> deathLocation(context.getSource())));
    }

    private static void registerReviewRequests(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("appeal")
                .requires(source -> can(source, "sef:control.appeals.submit"))
                .then(Commands.argument("description", StringArgumentType.greedyString())
                        .executes(context -> submitOwnQueue(
                                context.getSource(),
                                "appeals",
                                "other",
                                "moderation appeal",
                                StringArgumentType.getString(context, "description")))));
        dispatcher.register(Commands.literal("accessapply")
                .requires(source -> can(source, "sef:control.access_applications.submit"))
                .then(Commands.argument("description", StringArgumentType.greedyString())
                        .executes(context -> submitOwnQueue(
                                context.getSource(),
                                "access_applications",
                                "member",
                                "access application",
                                StringArgumentType.getString(context, "description")))));
        dispatcher.register(Commands.literal("privacy")
                .requires(source -> can(source, "sef:control.privacy.view"))
                .executes(context -> ownQueueStatus(context.getSource(), "privacy"))
                .then(Commands.literal("request")
                        .requires(source -> can(source, "sef:control.privacy.request"))
                        .then(Commands.argument("category", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    List.of("export", "correction", "deletion", "consent", "other")
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("description", StringArgumentType.greedyString())
                                        .executes(context -> submitOwnQueue(
                                                context.getSource(),
                                                "privacy",
                                                StringArgumentType.getString(context, "category"),
                                                "privacy request",
                                                StringArgumentType.getString(context, "description")))))));
    }

    private static void registerCalendar(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("calendar")
                .requires(source -> can(source, "sef:control.server_calendar.view"))
                .executes(context -> listControlRecords(context.getSource(), "server_calendar"))
                .then(Commands.literal("subscribe")
                        .requires(source -> can(source, "sef:control.server_calendar.subscribe"))
                        .then(Commands.argument("event", StringArgumentType.word())
                                .executes(context -> calendarSubscription(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "event"),
                                        true))))
                .then(Commands.literal("unsubscribe")
                        .requires(source -> can(source, "sef:control.server_calendar.subscribe"))
                        .then(Commands.argument("event", StringArgumentType.word())
                                .executes(context -> calendarSubscription(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "event"),
                                        false)))));
    }

    private static int showRules(CommandSourceStack source) {
        ServerControlRepository.ControlRecord rules = latestActive("rules").orElse(null);
        if (rules == null) {
            return fail(source, "No server rules are currently published.");
        }
        info(source, "Server rules, revision " + field(rules, "revision", Long.toString(rules.revision())) + ".");
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(field(rules, "content", "")), false);
        boolean accepted = acceptedRules(source.getPlayer(), rules);
        info(source, accepted ? "You accepted this revision." : "Run /rules accept to accept this revision.");
        return 1;
    }

    private static int acceptRules(CommandSourceStack source) {
        ServerPlayer player = player(source);
        ServerControlRepository.ControlRecord rules = latestActive("rules").orElse(null);
        if (player == null || rules == null) {
            return fail(source, "No published rules are available.");
        }
        return execute(source, "sef:control.rules.accept", Map.of(
                "record", rules.id().toString(),
                "revision", field(rules, "revision", Long.toString(rules.revision()))), () -> {
            KernelServices.communityState().put(
                    "rules_acceptance",
                    player.getUUID(),
                    null,
                    rules.id().toString(),
                    field(rules, "revision", Long.toString(rules.revision())),
                    null);
            success(source, "Rules accepted.");
            return 1;
        });
    }

    public static boolean acceptedRules(ServerPlayer player, ServerControlRepository.ControlRecord rules) {
        if (player == null || rules == null
                || !Boolean.parseBoolean(field(rules, "acceptance_required", "true"))) {
            return true;
        }
        return KernelServices.communityState()
                .find("rules_acceptance", player.getUUID(), rules.id().toString())
                .map(entry -> entry.value().equals(field(
                        rules,
                        "revision",
                        Long.toString(rules.revision()))))
                .orElse(false);
    }

    private static int rewardStatus(CommandSourceStack source, String feature) {
        ServerPlayer player = player(source);
        ServerControlRepository.ControlRecord reward = latestActive(feature).orElse(null);
        if (player == null || reward == null) {
            return fail(source, "No reward profile is currently available.");
        }
        Claim claim = claim(player, feature, reward);
        long next = Math.max(0L, claim.lastClaim() + number(reward, "period_seconds", 1L)
                - Instant.now().getEpochSecond());
        info(source, title(feature) + ", claims " + claim.count()
                + " of " + number(reward, "maximum_claims", 1L)
                + ", next claim in " + next + " seconds.");
        return 1;
    }

    private static int claimReward(CommandSourceStack source, String feature) {
        ServerPlayer player = player(source);
        ServerControlRepository.ControlRecord reward = latestActive(feature).orElse(null);
        if (player == null || reward == null) {
            return fail(source, "No reward profile is currently available.");
        }
        Claim current = claim(player, feature, reward);
        long now = Instant.now().getEpochSecond();
        long period = number(reward, "period_seconds", 1L);
        long maximum = number(reward, "maximum_claims", 1L);
        if (current.count() >= maximum) {
            return fail(source, "This reward has reached its claim limit.");
        }
        if (current.lastClaim() > 0L && current.lastClaim() + period > now) {
            return fail(source, "This reward is not ready yet.");
        }
        String bundleId = field(reward, "reward_bundle", "");
        var bundle = KernelServices.bundles().find(bundleId).orElse(null);
        if (bundle == null || !bundle.enabled()) {
            return fail(source, "The configured reward bundle is unavailable.");
        }
        return execute(source, "sef:control." + feature + ".claim", Map.of(
                "record", reward.id().toString(),
                "bundle", bundle.id()), () -> {
            ActionResult<BundleService.RuntimeJob> queued = KernelServices.bundles().enqueue(
                    bundle.id(),
                    bundle.revision(),
                    player.getUUID(),
                    List.of(player.getUUID()),
                    Instant.now());
            if (!queued.successful()) {
                return fail(source, queued.detail());
            }
            KernelServices.communityState().put(
                    "reward_claim",
                    player.getUUID(),
                    null,
                    feature + ":" + reward.id(),
                    now + "," + (current.count() + 1L),
                    null);
            success(source, "Reward queued as " + queued.value().jobId() + ".");
            return 1;
        });
    }

    private static Claim claim(
            ServerPlayer player,
            String feature,
            ServerControlRepository.ControlRecord reward
    ) {
        return KernelServices.communityState()
                .find("reward_claim", player.getUUID(), feature + ":" + reward.id())
                .map(entry -> {
                    String[] values = entry.value().split(",", 2);
                    try {
                        return new Claim(Long.parseLong(values[0]), Long.parseLong(values[1]));
                    } catch (RuntimeException exception) {
                        return new Claim(0L, 0L);
                    }
                })
                .orElseGet(() -> new Claim(0L, 0L));
    }

    private static int mentionStatus(CommandSourceStack source) {
        ServerPlayer player = player(source);
        if (player == null) {
            return 0;
        }
        String mode = preference(player.getUUID(), "mention_mode", "all");
        String sound = preference(player.getUUID(), "mention_sound", "true");
        info(source, "Mentions, mode " + mode + ", sound " + sound + ".");
        return 1;
    }

    private static int setMentionMode(CommandSourceStack source, String modeInput) {
        ServerPlayer player = player(source);
        String mode = modeInput.toLowerCase(Locale.ROOT);
        if (player == null || !List.of("all", "friends", "staff", "off").contains(mode)) {
            return fail(source, "Mention mode is invalid.");
        }
        return execute(source, "sef:control.mentions.set", Map.of("mode", mode), () -> {
            KernelServices.communityState().put(
                    "mention_mode",
                    player.getUUID(),
                    null,
                    "preference",
                    mode,
                    null);
            success(source, "Mention mode changed to " + mode + ".");
            return 1;
        });
    }

    private static int setMentionSound(CommandSourceStack source, boolean enabled) {
        ServerPlayer player = player(source);
        if (player == null) {
            return 0;
        }
        return execute(source, "sef:control.mentions.set", Map.of(
                "sound", Boolean.toString(enabled)), () -> {
            KernelServices.communityState().put(
                    "mention_sound",
                    player.getUUID(),
                    null,
                    "preference",
                    Boolean.toString(enabled),
                    null);
            success(source, "Mention sound " + (enabled ? "enabled." : "disabled."));
            return 1;
        });
    }

    private static int onboardingStatus(CommandSourceStack source) {
        ServerPlayer player = player(source);
        ServerControlRepository.ControlRecord onboarding = latestActive("onboarding").orElse(null);
        if (player == null || onboarding == null) {
            return fail(source, "No onboarding checklist is active.");
        }
        List<String> steps = checklist(onboarding);
        long completed = steps.stream()
                .filter(step -> onboardingStepComplete(player.getUUID(), onboarding.id(), step))
                .count();
        boolean dismissed = KernelServices.communityState().find(
                "onboarding_dismissed",
                player.getUUID(),
                onboarding.id().toString()).isPresent();
        info(source, "Onboarding, " + completed + " of " + steps.size()
                + " steps complete" + (dismissed ? ", reminders dismissed." : "."));
        for (String step : steps) {
            info(source, (onboardingStepComplete(player.getUUID(), onboarding.id(), step)
                    ? "complete, "
                    : "pending, ") + step + ".");
        }
        return 1;
    }

    private static int completeOnboardingStep(CommandSourceStack source, String stepInput) {
        ServerPlayer player = player(source);
        ServerControlRepository.ControlRecord onboarding = latestActive("onboarding").orElse(null);
        String step = key(stepInput);
        if (player == null || onboarding == null || step == null
                || !checklist(onboarding).contains(step)) {
            return fail(source, "Onboarding step is invalid.");
        }
        return execute(source, "sef:control.onboarding.complete", Map.of(
                "record", onboarding.id().toString(),
                "step", step), () -> {
            KernelServices.communityState().put(
                    "onboarding_step",
                    player.getUUID(),
                    null,
                    onboarding.id() + ":" + step,
                    "complete",
                    onboarding.expiresAt());
            List<String> steps = checklist(onboarding);
            boolean complete = steps.stream().allMatch(value ->
                    onboardingStepComplete(player.getUUID(), onboarding.id(), value));
            if (complete && KernelServices.communityState().find(
                    "onboarding_complete",
                    player.getUUID(),
                    onboarding.id().toString()).isEmpty()) {
                String bundleId = field(onboarding, "completion_bundle", "");
                if (!bundleId.isBlank()) {
                    var bundle = KernelServices.bundles().find(bundleId).orElse(null);
                    if (bundle == null || !bundle.enabled()) {
                        return fail(source, "The onboarding completion bundle is unavailable.");
                    }
                    ActionResult<BundleService.RuntimeJob> queued = KernelServices.bundles().enqueue(
                            bundle.id(),
                            bundle.revision(),
                            player.getUUID(),
                            List.of(player.getUUID()),
                            Instant.now());
                    if (!queued.successful()) {
                        return fail(source, queued.detail());
                    }
                }
                KernelServices.communityState().put(
                        "onboarding_complete",
                        player.getUUID(),
                        null,
                        onboarding.id().toString(),
                        "complete",
                        onboarding.expiresAt());
            }
            success(source, complete ? "Onboarding complete." : "Onboarding step completed.");
            return 1;
        });
    }

    private static int dismissOnboarding(CommandSourceStack source) {
        ServerPlayer player = player(source);
        ServerControlRepository.ControlRecord onboarding = latestActive("onboarding").orElse(null);
        if (player == null || onboarding == null) {
            return fail(source, "No onboarding checklist is active.");
        }
        return execute(source, "sef:control.onboarding.dismiss", Map.of(
                "record", onboarding.id().toString()), () -> {
            KernelServices.communityState().put(
                    "onboarding_dismissed",
                    player.getUUID(),
                    null,
                    onboarding.id().toString(),
                    "dismissed",
                    onboarding.expiresAt());
            success(source, "Onboarding reminders dismissed. The checklist remains available.");
            return 1;
        });
    }

    private static int sleepVoteStatus(CommandSourceStack source) {
        ServerControlRepository.ControlRecord policy = latestActive("sleep_vote").orElse(null);
        if (policy == null) {
            return fail(source, "Sleep voting is unavailable.");
        }
        MinecraftServerControlRuntime.SleepVoteSnapshot tally =
                MinecraftServerControlRuntime.sleepVoteSnapshot(source.getServer(), policy);
        info(source, "Sleep vote, " + tally.yes() + " yes of " + tally.eligible()
                + " eligible, " + tally.required() + " required.");
        return 1;
    }

    private static int setSleepVote(CommandSourceStack source, boolean yes) {
        ServerPlayer player = player(source);
        ServerControlRepository.ControlRecord policy = latestActive("sleep_vote").orElse(null);
        if (player == null || policy == null || player.isSpectator()) {
            return fail(source, "You are not eligible for the current sleep vote.");
        }
        return execute(source, "sef:control.sleep_vote.vote", Map.of(
                "record", policy.id().toString(),
                "vote", Boolean.toString(yes)), () -> {
            KernelServices.communityState().put(
                    "sleep_vote",
                    player.getUUID(),
                    null,
                    policy.id().toString(),
                    Boolean.toString(yes),
                    policy.expiresAt());
            MinecraftServerControlRuntime.SleepVoteSnapshot tally =
                    MinecraftServerControlRuntime.sleepVoteSnapshot(source.getServer(), policy);
            success(source, "Sleep vote recorded. " + tally.yes() + " of "
                    + tally.required() + " required.");
            return 1;
        });
    }

    private static int deathLocation(CommandSourceStack source) {
        ServerPlayer player = player(source);
        ServerControlRepository.ControlRecord policy = latestActive("death_compass").orElse(null);
        if (player == null || policy == null) {
            return fail(source, "Death-location tracking is unavailable.");
        }
        Instant clearedAt = KernelServices.communityState().find(
                        "death_location_clear",
                        player.getUUID(),
                        "latest")
                .flatMap(entry -> parseInstant(entry.value()))
                .orElse(Instant.EPOCH);
        Instant cutoff = Instant.now().minusSeconds(number(policy, "retention_seconds", 60L));
        var latest = KernelServices.locationHistory().history(player.getUUID()).stream()
                .filter(record -> record.reason().equals("death"))
                .filter(record -> record.recordedAt().isAfter(cutoff))
                .filter(record -> record.recordedAt().isAfter(clearedAt))
                .max(Comparator.comparing(
                        com.enviouse.sef.storage.repository.LocationHistoryRepository.LocationRecord::recordedAt))
                .orElse(null);
        if (latest == null) {
            return fail(source, "No retained death location is available.");
        }
        boolean crossDimension = Boolean.parseBoolean(field(policy, "cross_dimension", "true"));
        if (!crossDimension
                && !latest.dimensionId().equals(player.level().dimension().location().toString())) {
            return fail(source, "The latest death location is in another dimension.");
        }
        info(source, "Latest death, " + latest.dimensionId() + ", "
                + Math.floor(latest.x()) + ", " + Math.floor(latest.y()) + ", "
                + Math.floor(latest.z()) + ", " + latest.recordedAt() + ".");
        return 1;
    }

    private static int clearDeathLocation(CommandSourceStack source) {
        ServerPlayer player = player(source);
        if (player == null) {
            return 0;
        }
        return execute(source, "sef:control.death_compass.clear", Map.of(), () -> {
            KernelServices.communityState().put(
                    "death_location_clear",
                    player.getUUID(),
                    null,
                    "latest",
                    Instant.now().toString(),
                    null);
            success(source, "Retained death locations hidden.");
            return 1;
        });
    }

    private static int submitOwnQueue(
            CommandSourceStack source,
            String feature,
            String category,
            String title,
            String description
    ) {
        ServerPlayer player = player(source);
        if (player == null) {
            return 0;
        }
        return execute(source, "sef:control." + feature
                + (feature.equals("privacy") ? ".request" : ".submit"), Map.of(
                "category", category), () -> createQueueRecord(
                source,
                feature,
                player,
                player.getUUID(),
                title,
                category,
                description));
    }

    private static int ownQueueStatus(CommandSourceStack source, String feature) {
        ServerPlayer player = player(source);
        if (player == null) {
            return 0;
        }
        List<ServerControlRepository.ControlRecord> records = KernelServices.serverControls()
                .recordsFor(player.getUUID()).stream()
                .filter(record -> record.featureId().equals(feature))
                .limit(PAGE_SIZE)
                .toList();
        info(source, title(feature) + ", " + records.size() + " retained requests.");
        records.forEach(record -> info(source, record.id() + ", "
                + record.state().name().toLowerCase(Locale.ROOT) + "."));
        return 1;
    }

    private static int calendarSubscription(
            CommandSourceStack source,
            String recordInput,
            boolean subscribe
    ) {
        ServerPlayer player = player(source);
        ServerControlRepository.ControlRecord event = record(recordInput, "server_calendar");
        if (player == null || event == null || !open(event)) {
            return fail(source, "Calendar event is unavailable.");
        }
        return execute(source, "sef:control.server_calendar.subscribe", Map.of(
                "event", event.id().toString(),
                "subscribe", Boolean.toString(subscribe)), () -> {
            if (subscribe) {
                KernelServices.communityState().put(
                        "calendar_subscription",
                        player.getUUID(),
                        null,
                        event.id().toString(),
                        "subscribed",
                        event.expiresAt());
            } else if (!KernelServices.communityState().remove(
                    "calendar_subscription",
                    player.getUUID(),
                    event.id().toString())) {
                return fail(source, "You are not subscribed to that event.");
            }
            success(source, subscribe ? "Calendar subscription saved." : "Calendar subscription removed.");
            return 1;
        });
    }

    private static int submitReport(
            CommandSourceStack source,
            ServerPlayer subject,
            String description
    ) {
        ServerPlayer reporter = player(source);
        if (reporter == null || reporter.getUUID().equals(subject.getUUID())) {
            return fail(source, "You cannot report yourself.");
        }
        return execute(source, "sef:control.reports.submit", Map.of(
                "subject", subject.getUUID().toString()), () -> createQueueRecord(
                source,
                "reports",
                reporter,
                subject.getUUID(),
                "player report for " + subject.getGameProfile().getName(),
                "conduct",
                description));
    }

    private static int submitTicket(CommandSourceStack source, String description) {
        ServerPlayer reporter = player(source);
        if (reporter == null) {
            return 0;
        }
        return execute(source, "sef:control.tickets.submit", Map.of(), () -> createQueueRecord(
                source,
                "tickets",
                reporter,
                reporter.getUUID(),
                "support ticket",
                "support",
                description));
    }

    private static int createQueueRecord(
            CommandSourceStack source,
            String feature,
            ServerPlayer owner,
            UUID subject,
            String title,
            String category,
            String description
    ) {
        try {
            String validated = ServerControlSchemaRegistry.validate(feature, "description", description);
            ActionResult<ServerControlRepository.ControlRecord> result = KernelServices.serverControls().create(
                    feature,
                    owner.getUUID(),
                    subject,
                    title,
                    "",
                    Instant.now().plusSeconds(2_592_000L),
                    Map.of(
                            "field.category", category,
                            "field.description", validated,
                            "field.priority", "normal"));
            if (!result.successful()) {
                return fail(source, result.detail());
            }
            success(source, title(feature) + " submitted as " + result.value().id() + ".");
            return 1;
        } catch (IllegalArgumentException exception) {
            return fail(source, exception.getMessage());
        }
    }

    private static int requestFriend(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer actor = player(source);
        if (actor == null || actor.getUUID().equals(target.getUUID())) {
            return fail(source, "You cannot add yourself.");
        }
        return execute(source, "sef:control.friends.request", Map.of(
                "target", target.getUUID().toString()), () -> {
            if (KernelServices.communityState().find(
                    "friend",
                    actor.getUUID(),
                    target.getUUID().toString()).isPresent()) {
                return fail(source, "That player is already your friend.");
            }
            KernelServices.communityState().put(
                    "friend_request",
                    target.getUUID(),
                    actor.getUUID(),
                    actor.getUUID().toString(),
                    "pending",
                    Instant.now().plusSeconds(604_800L));
            target.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&e" + actor.getGameProfile().getName()
                            + " sent a friend request. Use /friend accept "
                            + actor.getGameProfile().getName() + "."));
            success(source, "Friend request sent.");
            return 1;
        });
    }

    private static int acceptFriend(CommandSourceStack source, ServerPlayer requester) {
        ServerPlayer actor = player(source);
        if (actor == null) {
            return 0;
        }
        return execute(source, "sef:control.friends.accept", Map.of(
                "requester", requester.getUUID().toString()), () -> {
            var request = KernelServices.communityState().find(
                    "friend_request",
                    actor.getUUID(),
                    requester.getUUID().toString()).orElse(null);
            if (request == null) {
                return fail(source, "No pending request from that player exists.");
            }
            CommunityStateRepository.MutationResult accepted =
                    KernelServices.communityState().mutateAtomically(List.of(
                            new CommunityStateRepository.CompareAndRemove(
                                    "friend_request",
                                    actor.getUUID(),
                                    requester.getUUID().toString(),
                                    request.revision()),
                            new CommunityStateRepository.Write(
                                    "friend",
                                    actor.getUUID(),
                                    requester.getUUID(),
                                    requester.getUUID().toString(),
                                    "friend",
                                    null),
                            new CommunityStateRepository.Write(
                                    "friend",
                                    requester.getUUID(),
                                    actor.getUUID(),
                                    actor.getUUID().toString(),
                                    "friend",
                                    null)));
            if (!accepted.successful()) {
                return fail(source, "The friend request changed. Try again.");
            }
            requester.sendSystemMessage(TextFormatter.stringToFormattedText(
                    "&a" + actor.getGameProfile().getName() + " accepted your friend request."));
            success(source, "Friend request accepted.");
            return 1;
        });
    }

    private static int removeFriend(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer actor = player(source);
        if (actor == null) {
            return 0;
        }
        return execute(source, "sef:control.friends.remove", Map.of(
                "target", target.getUUID().toString()), () -> {
            CommunityStateRepository.MutationResult removed =
                    KernelServices.communityState().mutateAtomically(List.of(
                            new CommunityStateRepository.Remove(
                                    "friend",
                                    actor.getUUID(),
                                    target.getUUID().toString()),
                            new CommunityStateRepository.Remove(
                                    "friend",
                                    target.getUUID(),
                                    actor.getUUID().toString())));
            if (removed.removed() == 0) {
                return fail(source, "That player is not in your friend list.");
            }
            success(source, "Friend removed.");
            return 1;
        });
    }

    private static int listRelationships(CommandSourceStack source) {
        ServerPlayer actor = player(source);
        if (actor == null) {
            return 0;
        }
        List<CommunityStateRepository.Entry> friends =
                KernelServices.communityState().entries("friend", actor.getUUID());
        info(source, "Friends, " + friends.size() + ".");
        friends.stream().limit(PAGE_SIZE).forEach(entry -> info(
                source,
                name(source, entry.subjectId()) + ", " + entry.value() + "."));
        return 1;
    }

    private static int setBlock(
            CommandSourceStack source,
            ServerPlayer target,
            String interactionInput,
            boolean blocked
    ) {
        ServerPlayer actor = player(source);
        String interaction = interactionInput.toLowerCase(Locale.ROOT);
        if (actor == null || actor.getUUID().equals(target.getUUID())
                || !interactionTypes().contains(interaction)) {
            return fail(source, "Interaction type or player is invalid.");
        }
        return execute(source, "sef:control.interaction_blocks.set", Map.of(
                "target", target.getUUID().toString(),
                "interaction", interaction,
                "blocked", Boolean.toString(blocked)), () -> {
            String key = target.getUUID() + ":" + interaction;
            if (blocked) {
                KernelServices.communityState().put(
                        "interaction_block",
                        actor.getUUID(),
                        target.getUUID(),
                        key,
                        interaction,
                        null);
            } else if (!KernelServices.communityState().remove(
                    "interaction_block",
                    actor.getUUID(),
                    key)) {
                return fail(source, "That interaction block does not exist.");
            }
            success(source, blocked ? "Interaction blocked." : "Interaction block removed.");
            return 1;
        });
    }

    public static boolean interactionBlocked(UUID owner, UUID other, String interaction) {
        return KernelServices.communityState().find(
                "interaction_block",
                owner,
                other + ":all").isPresent()
                || KernelServices.communityState().find(
                "interaction_block",
                owner,
                other + ":" + interaction).isPresent();
    }

    private static int listBlocks(CommandSourceStack source) {
        ServerPlayer actor = player(source);
        if (actor == null) {
            return 0;
        }
        List<CommunityStateRepository.Entry> blocks =
                KernelServices.communityState().entries("interaction_block", actor.getUUID());
        info(source, "Interaction blocks, " + blocks.size() + ".");
        blocks.stream().limit(PAGE_SIZE).forEach(entry -> info(
                source,
                name(source, entry.subjectId()) + ", " + entry.value() + "."));
        return 1;
    }

    private static int setWaypoint(CommandSourceStack source, String nameInput) {
        ServerPlayer player = player(source);
        String name = key(nameInput);
        if (player == null || name == null) {
            return fail(source, "Waypoint name is invalid.");
        }
        SavedLocation location = SavedLocation.from(player);
        return execute(source, "sef:control.waypoints.set", Map.of("name", name), () -> {
            KernelServices.communityState().put(
                    "waypoint",
                    player.getUUID(),
                    null,
                    name,
                    encode(location),
                    null);
            success(source, "Waypoint " + name + " saved.");
            return 1;
        });
    }

    private static int removeWaypoint(CommandSourceStack source, String nameInput) {
        ServerPlayer player = player(source);
        String name = key(nameInput);
        if (player == null || name == null) {
            return fail(source, "Waypoint name is invalid.");
        }
        return execute(source, "sef:control.waypoints.remove", Map.of("name", name), () -> {
            if (!KernelServices.communityState().remove("waypoint", player.getUUID(), name)) {
                return fail(source, "Waypoint not found.");
            }
            success(source, "Waypoint removed.");
            return 1;
        });
    }

    private static int goWaypoint(CommandSourceStack source, String nameInput) {
        ServerPlayer player = player(source);
        String name = key(nameInput);
        var entry = player == null || name == null
                ? null
                : KernelServices.communityState().find("waypoint", player.getUUID(), name).orElse(null);
        SavedLocation destination = entry == null ? null : decode(entry.value());
        if (destination == null) {
            return fail(source, "Waypoint not found or invalid.");
        }
        return execute(source, "sef:control.waypoints.go", Map.of(
                "name", name,
                "revision", Long.toString(entry.revision())), () -> {
            SafeTeleportService.TeleportResult result = KernelServices.safeTeleports().teleport(
                    source.getServer(),
                    player,
                    player,
                    destination,
                    "community waypoint",
                    KernelServices.teleportSettings().userPolicy(),
                    () -> KernelServices.communityState()
                            .find("waypoint", player.getUUID(), name)
                            .map(current -> current.revision() == entry.revision())
                            .orElse(false));
            if (!result.successful()) {
                return fail(source, result.detail());
            }
            success(source, "Teleported to waypoint " + name + ".");
            return 1;
        });
    }

    private static int listWaypoints(CommandSourceStack source) {
        ServerPlayer player = player(source);
        if (player == null) {
            return 0;
        }
        List<CommunityStateRepository.Entry> waypoints =
                KernelServices.communityState().entries("waypoint", player.getUUID());
        info(source, "Waypoints, " + waypoints.size() + ".");
        waypoints.stream().limit(PAGE_SIZE).forEach(entry -> info(source, entry.key() + "."));
        return 1;
    }

    private static int vote(CommandSourceStack source, String recordInput, String choiceInput) {
        ServerPlayer player = player(source);
        ServerControlRepository.ControlRecord poll = record(recordInput, "polls");
        if (player == null || poll == null || !open(poll)) {
            return fail(source, "Poll is unavailable.");
        }
        String choice = choiceInput.strip();
        List<String> choices = List.of(field(poll, "choices", "").split(",", -1)).stream()
                .map(String::strip)
                .filter(value -> !value.isBlank())
                .toList();
        if (!choices.contains(choice)) {
            return fail(source, "Choice is invalid. Available choices are " + String.join(", ", choices) + ".");
        }
        return execute(source, "sef:control.polls.vote", Map.of(
                "poll", poll.id().toString(),
                "choice", choice), () -> {
            KernelServices.communityState().put(
                    "poll_ballot",
                    player.getUUID(),
                    null,
                    poll.id().toString(),
                    choice,
                    instant(poll, "closes_at").orElse(poll.expiresAt()));
            success(source, "Vote recorded.");
            return 1;
        });
    }

    private static int eventRegistration(
            CommandSourceStack source,
            String recordInput,
            boolean joining
    ) {
        ServerPlayer player = player(source);
        ServerControlRepository.ControlRecord event = record(recordInput, "community_events");
        if (player == null || event == null || !open(event)) {
            return fail(source, "Event is unavailable.");
        }
        return execute(source, "sef:control.community_events." + (joining ? "join" : "leave"), Map.of(
                "event", event.id().toString()), () -> {
            if (joining) {
                long capacity = number(event, "capacity", 1L);
                if (KernelServices.communityState().count(
                        "event_registration",
                        event.id().toString()) >= capacity) {
                    return fail(source, "Event capacity is full.");
                }
                KernelServices.communityState().put(
                        "event_registration",
                        player.getUUID(),
                        null,
                        event.id().toString(),
                        event.id().toString(),
                        event.expiresAt());
                success(source, "Event registration saved.");
            } else if (!KernelServices.communityState().remove(
                    "event_registration",
                    player.getUUID(),
                    event.id().toString())) {
                return fail(source, "You are not registered for this event.");
            } else {
                success(source, "Event registration removed.");
            }
            return 1;
        });
    }

    private static int readArticle(CommandSourceStack source, String articleInput) {
        ServerControlRepository.ControlRecord article = findArticle(articleInput);
        if (article == null) {
            return fail(source, "Article not found.");
        }
        info(source, field(article, "article_id", article.title()) + ", " + field(article, "locale", "") + ".");
        source.sendSuccess(() -> TextFormatter.stringToFormattedText(field(article, "content", "")), false);
        return 1;
    }

    private static int bookmarkArticle(CommandSourceStack source, String articleInput) {
        ServerPlayer player = player(source);
        ServerControlRepository.ControlRecord article = findArticle(articleInput);
        if (player == null || article == null) {
            return fail(source, "Article not found.");
        }
        return execute(source, "sef:control.knowledge.bookmark", Map.of(
                "article", article.id().toString()), () -> {
            KernelServices.communityState().put(
                    "knowledge_bookmark",
                    player.getUUID(),
                    null,
                    article.id().toString(),
                    field(article, "article_id", article.title()),
                    null);
            success(source, "Article bookmarked.");
            return 1;
        });
    }

    private static int redeemInvite(CommandSourceStack source, String codeInput) {
        ServerPlayer player = player(source);
        String code = codeInput.strip();
        ServerControlRepository.ControlRecord invite = KernelServices.serverControls().records("invites").stream()
                .filter(CommunityCommands::open)
                .filter(record -> field(record, "code", "").equals(code))
                .max(Comparator.comparing(ServerControlRepository.ControlRecord::updatedAt))
                .orElse(null);
        if (player == null || invite == null) {
            return fail(source, "Invite is invalid or expired.");
        }
        long maximum = number(invite, "uses", 1L);
        if (KernelServices.communityState().count(
                "invite_redemption",
                invite.id().toString()) >= maximum
                || KernelServices.communityState().find(
                "invite_redemption",
                player.getUUID(),
                invite.id().toString()).isPresent()) {
            return fail(source, "Invite has no uses remaining for you.");
        }
        String profile = field(invite, "grant_profile", "");
        var bundle = KernelServices.bundles().find(profile).orElse(null);
        if (bundle == null || !bundle.enabled()) {
            return fail(source, "Invite grant profile is unavailable.");
        }
        return execute(source, "sef:control.invites.redeem", Map.of(
                "invite", invite.id().toString()), () -> {
            ActionResult<BundleService.RuntimeJob> queued = KernelServices.bundles().enqueue(
                    bundle.id(),
                    bundle.revision(),
                    player.getUUID(),
                    List.of(player.getUUID()),
                    Instant.now());
            if (!queued.successful()) {
                return fail(source, queued.detail());
            }
            KernelServices.communityState().put(
                    "invite_redemption",
                    player.getUUID(),
                    null,
                    invite.id().toString(),
                    invite.id().toString(),
                    null);
            success(source, "Invite redeemed.");
            return 1;
        });
    }

    private static int listControlRecords(CommandSourceStack source, String feature) {
        List<ServerControlRepository.ControlRecord> records = KernelServices.serverControls()
                .records(feature)
                .stream()
                .filter(CommunityCommands::open)
                .toList();
        info(source, title(feature) + ", " + records.size() + ".");
        records.stream().limit(PAGE_SIZE).forEach(record -> info(
                source,
                record.id() + ", " + record.title() + "."));
        return 1;
    }

    private static Optional<ServerControlRepository.ControlRecord> latestActive(String feature) {
        return KernelServices.serverControls().records(feature).stream()
                .filter(CommunityCommands::open)
                .max(Comparator.comparing(ServerControlRepository.ControlRecord::updatedAt));
    }

    private static ServerControlRepository.ControlRecord record(String id, String feature) {
        try {
            return KernelServices.serverControls().find(UUID.fromString(id))
                    .filter(record -> record.featureId().equals(feature))
                    .orElse(null);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static ServerControlRepository.ControlRecord findArticle(String input) {
        ServerControlRepository.ControlRecord byId = record(input, "knowledge");
        if (byId != null) {
            return byId;
        }
        return KernelServices.serverControls().records("knowledge").stream()
                .filter(CommunityCommands::open)
                .filter(record -> field(record, "article_id", "").equalsIgnoreCase(input))
                .max(Comparator.comparing(ServerControlRepository.ControlRecord::updatedAt))
                .orElse(null);
    }

    private static boolean open(ServerControlRepository.ControlRecord record) {
        if (record.expiresAt() != null && !record.expiresAt().isAfter(Instant.now())) {
            return false;
        }
        return record.state() == ServerControlRepository.RecordState.ACTIVE
                || record.state() == ServerControlRepository.RecordState.APPROVED;
    }

    private static String field(
            ServerControlRepository.ControlRecord record,
            String field,
            String fallback
    ) {
        return record.metadata().getOrDefault("field." + field, fallback);
    }

    private static long number(
            ServerControlRepository.ControlRecord record,
            String field,
            long fallback
    ) {
        try {
            return Long.parseLong(field(record, field, Long.toString(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static Optional<Instant> instant(
            ServerControlRepository.ControlRecord record,
            String field
    ) {
        try {
            String value = field(record, field, "");
            return value.isBlank() ? Optional.empty() : Optional.of(Instant.parse(value));
        } catch (DateTimeParseException exception) {
            return Optional.empty();
        }
    }

    private static Optional<Instant> parseInstant(String value) {
        try {
            return Optional.of(Instant.parse(value));
        } catch (DateTimeParseException exception) {
            return Optional.empty();
        }
    }

    private static String preference(UUID playerId, String type, String fallback) {
        return KernelServices.communityState().find(type, playerId, "preference")
                .map(CommunityStateRepository.Entry::value)
                .orElse(fallback);
    }

    private static List<String> checklist(ServerControlRepository.ControlRecord onboarding) {
        return List.of(field(onboarding, "checklist", "").split("[,|\\n]", -1)).stream()
                .map(CommunityCommands::key)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .limit(64)
                .toList();
    }

    private static boolean onboardingStepComplete(UUID playerId, UUID recordId, String step) {
        return KernelServices.communityState().find(
                "onboarding_step",
                playerId,
                recordId + ":" + step).isPresent();
    }

    private static String encode(SavedLocation location) {
        return location.dimensionId() + "|" + location.x() + "|" + location.y() + "|"
                + location.z() + "|" + location.yaw() + "|" + location.pitch();
    }

    private static SavedLocation decode(String value) {
        String[] parts = value.split("\\|", -1);
        if (parts.length != 6) {
            return null;
        }
        try {
            return new SavedLocation(
                    parts[0],
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3]),
                    Float.parseFloat(parts[4]),
                    Float.parseFloat(parts[5]));
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static String key(String input) {
        String normalized = input.strip().toLowerCase(Locale.ROOT);
        return normalized.matches("[a-z0-9][a-z0-9_.-]{0,63}") ? normalized : null;
    }

    private static List<String> interactionTypes() {
        return List.of(
                "messages",
                "teleports",
                "mail",
                "payments",
                "mentions",
                "friends",
                "home_invites",
                "event_invites",
                "trade",
                "parcels",
                "all");
    }

    private static ServerPlayer player(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            fail(source, "This command requires a player.");
        }
        return player;
    }

    private static String name(CommandSourceStack source, UUID playerId) {
        if (playerId == null) {
            return "unknown";
        }
        ServerPlayer online = source.getServer().getPlayerList().getPlayer(playerId);
        return online == null
                ? KernelServices.profiles().find(playerId)
                .map(profile -> profile.authenticatedUsername())
                .orElse(playerId.toString())
                : online.getGameProfile().getName();
    }

    private static boolean can(CommandSourceStack source, String action) {
        return KernelCommandExecutor.canUse(source, action);
    }

    private static int execute(
            CommandSourceStack source,
            String action,
            Map<String, String> parameters,
            java.util.function.IntSupplier operation
    ) {
        return KernelCommandExecutor.execute(source, action, parameters, operation);
    }

    private static int fail(CommandSourceStack source, String message) {
        source.sendFailure(TextFormatter.stringToFormattedText("&c" + message));
        return 0;
    }

    private static void info(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&7" + message), false);
    }

    private static void success(CommandSourceStack source, String message) {
        source.sendSuccess(() -> TextFormatter.stringToFormattedText("&a" + message), false);
    }

    private static String title(String value) {
        String[] parts = value.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }

    private record Claim(long lastClaim, long count) {
    }

}
