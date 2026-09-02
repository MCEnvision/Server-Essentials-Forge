package com.enviouse.sef.gui.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.gui.protocol.ClientProtocolState;
import com.enviouse.sef.gui.protocol.SefGuiServer;
import com.enviouse.sef.gui.protocol.SefPayloads;
import com.enviouse.sef.gui.protocol.SefProtocol;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.util.TriState;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ServerEssentialsForge.MODID, value = Dist.CLIENT)
public final class SefClientEvents {
    private static final Map<UUID, DisguiseRenderProxy> DISGUISE_PROXIES = new LinkedHashMap<>();
    private static final Map<UUID, Long> FAILED_DISGUISE_REVISIONS = new LinkedHashMap<>();
    private static boolean renderingDisguiseProxy;

    private SefClientEvents() {
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (SefClientModEvents.OPEN_DASHBOARD.consumeClick()) {
            if (minecraft.player != null
                    && ClientProtocolState.negotiated(SefProtocol.Feature.DASHBOARD)) {
                SefClientTransport.open(SefGuiServer.DASHBOARD, 1, "");
            }
        }
        while (SefClientModEvents.DISGUISE_PRIMARY.consumeClick()) {
            if (minecraft.player != null
                    && ClientProtocolState.negotiated(SefProtocol.Feature.DISGUISE_ABILITY_INPUT)) {
                SefClientTransport.disguiseAbility("primary");
            }
        }
        while (SefClientModEvents.DISGUISE_SECONDARY.consumeClick()) {
            if (minecraft.player != null
                    && ClientProtocolState.negotiated(SefProtocol.Feature.DISGUISE_ABILITY_INPUT)) {
                SefClientTransport.disguiseAbility("secondary");
            }
        }
        while (SefClientModEvents.DISGUISE_UTILITY.consumeClick()) {
            if (minecraft.player != null
                    && ClientProtocolState.negotiated(SefProtocol.Feature.DISGUISE_ABILITY_INPUT)) {
                SefClientTransport.disguiseAbility("utility");
            }
        }
        while (SefClientModEvents.OPEN_FANCY_TAGS_STUDIO.consumeClick()) {
            if (minecraft.player != null) {
                minecraft.setScreen(new FancyTagStudioScreen(minecraft.screen));
            }
        }
        ClientProtocolState.takeFancyTagsStudioSection().ifPresent(section -> {
            if (minecraft.player != null) {
                minecraft.setScreen(FancyTagStudioScreen.open(minecraft.screen, section));
            }
        });
        if ((minecraft.screen instanceof SefPanelScreen
                || minecraft.screen instanceof SefControlEditorScreen
                || minecraft.screen instanceof SefWorkflowScreen
                || minecraft.screen instanceof SefPlayerPickerScreen
                || minecraft.screen instanceof SefItemPickerScreen
                || minecraft.screen instanceof SefSuggestionPickerScreen)
                && !ClientProtocolState.negotiated(SefProtocol.Feature.DASHBOARD)) {
            minecraft.screen.onClose();
        }
        if ((minecraft.screen instanceof SefWorkflowScreen
                || minecraft.screen instanceof SefPlayerPickerScreen
                || minecraft.screen instanceof SefItemPickerScreen
                || minecraft.screen instanceof SefSuggestionPickerScreen)
                && !ClientProtocolState.negotiated(SefProtocol.Feature.GUI_WORKFLOW)) {
            minecraft.screen.onClose();
        }
        ClientProtocolState.takePanel().ifPresent(snapshot -> {
            if (minecraft.player == null) {
                return;
            }
            minecraft.setScreen(new SefPanelScreen(minecraft.screen, snapshot));
        });
        ClientProtocolState.takeControlEditor().ifPresent(snapshot -> {
            if (minecraft.player == null) {
                return;
            }
            minecraft.setScreen(new SefControlEditorScreen(minecraft.screen, snapshot));
        });
        ClientProtocolState.takeWorkflow().ifPresent(snapshot -> {
            if (minecraft.player == null) {
                return;
            }
            minecraft.setScreen(new SefWorkflowScreen(minecraft.screen, snapshot));
        });
        ClientProtocolState.takeWorkflowSuggestions().ifPresent(suggestions -> {
            if (minecraft.screen instanceof SefWorkflowScreen workflow) {
                workflow.acceptSuggestions(suggestions);
            } else if (minecraft.screen instanceof SefPlayerPickerScreen picker) {
                picker.acceptSuggestions(suggestions);
            } else if (minecraft.screen instanceof SefSuggestionPickerScreen picker) {
                picker.acceptSuggestions(suggestions);
            }
        });
        ClientProtocolState.takeWorkflowProgress().ifPresent(progress -> {
            if (minecraft.screen instanceof SefWorkflowScreen workflow) {
                workflow.acceptProgress(progress);
            }
        });
        ClientProtocolState.takeWorkflowResult().ifPresent(result -> {
            if (minecraft.screen instanceof SefWorkflowScreen workflow) {
                workflow.acceptResult(result);
            }
        });
        ClientProtocolState.takeWorkflowInvalidation().ifPresent(invalidation -> {
            if (minecraft.screen instanceof SefWorkflowScreen workflow) {
                workflow.acceptInvalidation(invalidation);
            } else if (minecraft.screen instanceof SefPlayerPickerScreen picker) {
                picker.acceptInvalidation(invalidation);
            } else if (minecraft.screen instanceof SefItemPickerScreen picker) {
                picker.acceptInvalidation(invalidation);
            } else if (minecraft.screen instanceof SefSuggestionPickerScreen picker) {
                picker.acceptInvalidation(invalidation);
            } else if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                        Component.literal(invalidation.reason()),
                        false);
            }
        });
        if (minecraft.player != null) {
            FancyTagClientCache.tick(minecraft);
            FancyTagGlyphBridge.refresh();
        }
    }

    @SubscribeEvent
    public static void openingScreen(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof ContainerScreen container)
                || !(container.getMenu() instanceof ChestMenu menu)
                || !(container.getTitle().getContents() instanceof TranslatableContents title)
                || !title.getKey().equals("gui.sef.invsee.title")
                || !ClientProtocolState.negotiated(SefProtocol.Feature.INVENTORY_VIEW)) {
            return;
        }
        Object[] arguments = title.getArgs();
        String targetName = arguments.length > 0 ? String.valueOf(arguments[0]) : "Player";
        int page = arguments.length > 1 && arguments[1] instanceof Number number
                ? number.intValue()
                : 0;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            event.setNewScreen(new SefInvSeeScreen(
                    menu,
                    minecraft.player.getInventory(),
                    container.getTitle(),
                    targetName,
                    page));
        }
    }

    @SubscribeEvent
    public static void chat(ClientChatReceivedEvent event) {
        if (event.isSystem()
                || !ClientProtocolState.negotiated(SefProtocol.Feature.FANCY_TAGS_STATIC)) {
            return;
        }
        Component prefix = FancyTagGlyphBridge.chatPrefix(event.getSender());
        Component suffix = FancyTagGlyphBridge.chatSuffix(event.getSender());
        if (prefix.getString().isEmpty() && suffix.getString().isEmpty()) {
            return;
        }
        event.setMessage(Component.empty()
                .append(prefix.getString().isEmpty()
                        ? Component.empty()
                        : prefix.copy().append(Component.literal(" ")))
                .append(event.getMessage())
                .append(suffix.getString().isEmpty() ? Component.empty() : Component.literal(" ").append(suffix)));
    }

    @SubscribeEvent
    public static void pauseScreen(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen)
                || !ClientProtocolState.negotiated(SefProtocol.Feature.PAUSE_BUTTON)) {
            return;
        }
        Placement placement = findPausePlacement(event);
        if (placement == null) {
            return;
        }
        Button button = Button.builder(
                        Component.translatable("gui.sef.pause_button"),
                        ignored -> SefClientTransport.open(SefGuiServer.DASHBOARD, 1, ""))
                .bounds(placement.x(), placement.y(), 100, 20)
                .tooltip(Tooltip.create(Component.translatable("gui.sef.pause_tooltip")))
                .build();
        event.addListener(button);
    }

    @SubscribeEvent
    public static void renderHud(RenderGuiEvent.Post event) {
        List<SefPayloads.HudTile> tiles = ClientProtocolState.hudTiles();
        GuiGraphics graphics = event.getGuiGraphics();
        Minecraft minecraft = Minecraft.getInstance();
        int right = graphics.guiWidth() - 6;
        int y = 6;
        for (SefPayloads.HudTile tile : tiles) {
            int color = switch (tile.severity()) {
                case INFO -> 0xffbdbdbd;
                case NOTICE -> 0xff55ffff;
                case WARNING -> 0xffffaa00;
                case CRITICAL -> 0xffff5555;
            };
            int width = minecraft.font.width(tile.text()) + 8;
            int height = tile.surface() == SefPayloads.HudSurface.PROGRESS ? 18 : 13;
            int background = tile.surface() == SefPayloads.HudSurface.ALERT
                    ? 0xc0402000
                    : 0xb0000000;
            graphics.fill(right - width, y, right, y + height, background);
            graphics.drawString(minecraft.font, tile.text(), right - width + 4, y + 3, color, true);
            if (tile.surface() == SefPayloads.HudSurface.PROGRESS) {
                int progressWidth = Math.max(0, width * tile.progressPercent() / 100);
                graphics.fill(right - width, y + 14, right - width + progressWidth, y + 17, color);
            }
            y += height + 2;
        }
        int tagY = y;
        if (ClientProtocolState.negotiated(SefProtocol.Feature.FANCY_TAGS_STATIC)) {
            boolean rendered = false;
            if (minecraft.player != null) {
                for (SefPayloads.TagAssignmentProjection assignment : ClientProtocolState.tagAssignments()) {
                    if (!assignment.subjectId().equals(minecraft.player.getUUID())
                            || !assignment.slot().equals("hud")) {
                        continue;
                    }
                    var texture = FancyTagClientCache.texture(assignment.tagId());
                    var facts = FancyTagClientCache.facts(assignment.tagId());
                    if (texture.isPresent() && facts.isPresent()) {
                        renderTag(
                                graphics,
                                texture.orElseThrow(),
                                right - 20,
                                tagY,
                                facts.orElseThrow().width(),
                                facts.orElseThrow().height());
                        tagY += 20;
                        rendered = true;
                    }
                }
            }
            if (!rendered) {
                int fallbackTagY = tagY;
                FancyTagClientCache.texture().ifPresent(texture -> renderTag(
                        graphics,
                        texture,
                        right - 20,
                        fallbackTagY,
                        FancyTagClientCache.textureWidth(),
                        FancyTagClientCache.textureHeight()));
            }
        }
        FancyTagLocalOverlay.render(graphics, minecraft, tagY);
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        Minecraft minecraft = Minecraft.getInstance();
        FancyTagClientCache.close(minecraft);
        FancyTagLocalOverlay.release(minecraft);
        FancyTagGlyphBridge.clear();
        DISGUISE_PROXIES.clear();
        FAILED_DISGUISE_REVISIONS.clear();
        renderingDisguiseProxy = false;
        ClientProtocolState.reset();
    }

    @SubscribeEvent
    public static void renderDisguise(RenderPlayerEvent.Pre event) {
        if (renderingDisguiseProxy
                || !ClientProtocolState.negotiated(SefProtocol.Feature.DISGUISE_PROJECTION)) {
            return;
        }
        SefPayloads.DisguiseProjection projection =
                ClientProtocolState.disguise(event.getEntity().getUUID()).orElse(null);
        if (projection == null
                || FAILED_DISGUISE_REVISIONS.getOrDefault(projection.subjectId(), -1L)
                == projection.disguiseRevision()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        try {
            DisguiseRenderProxy cached = DISGUISE_PROXIES.get(projection.subjectId());
            if (cached == null
                    || cached.revision() != projection.disguiseRevision()
                    || cached.entity().level() != minecraft.level) {
                Entity entity;
                if (projection.kind().equals("player")) {
                    if (projection.profileId() == null || projection.profileName().isBlank()) {
                        return;
                    }
                    GameProfile profile = new GameProfile(projection.profileId(), projection.profileName());
                    if (!projection.texturesValue().isBlank() && !projection.texturesSignature().isBlank()) {
                        profile.getProperties().put(
                                "textures",
                                new Property(
                                        "textures",
                                        projection.texturesValue(),
                                        projection.texturesSignature()));
                    }
                    entity = new RemotePlayer(minecraft.level, profile);
                } else {
                    ResourceLocation typeId = ResourceLocation.tryParse(projection.reference());
                    entity = typeId == null
                            ? null
                            : BuiltInRegistries.ENTITY_TYPE.getOptional(typeId)
                            .map(type -> type.create(minecraft.level))
                            .orElse(null);
                }
                if (entity == null) {
                    return;
                }
                entity.setSilent(true);
                entity.noPhysics = true;
                cached = new DisguiseRenderProxy(
                        projection.disguiseRevision(),
                        entity,
                        Integer.MIN_VALUE,
                        true);
                DISGUISE_PROXIES.put(projection.subjectId(), cached);
            }
            Entity proxy = cached.entity();
            Player player = event.getEntity();
            boolean advanceAnimation = cached.animationTick() != player.tickCount;
            boolean entityAnimationAdvanced = false;
            if (advanceAnimation
                    && cached.tickAnimations()
                    && !(proxy instanceof Player)) {
                prepareAnimationTick(proxy, player);
                try {
                    proxy.tick();
                    entityAnimationAdvanced = true;
                } catch (RuntimeException exception) {
                    cached = new DisguiseRenderProxy(
                            cached.revision(),
                            cached.entity(),
                            cached.animationTick(),
                            false);
                    ServerEssentialsForge.LOGGER.warn(
                            "Could not tick disguise animation for {}",
                            projection.reference(),
                            exception);
                }
            }
            proxy.setPos(player.getX(), player.getY(), player.getZ());
            proxy.setYRot(player.getYRot());
            proxy.setXRot(player.getXRot());
            proxy.xo = player.xo;
            proxy.yo = player.yo;
            proxy.zo = player.zo;
            proxy.xOld = player.xOld;
            proxy.yOld = player.yOld;
            proxy.zOld = player.zOld;
            proxy.yRotO = player.yRotO;
            proxy.xRotO = player.xRotO;
            proxy.tickCount = player.tickCount;
            proxy.walkDistO = player.walkDistO;
            proxy.walkDist = player.walkDist;
            proxy.moveDist = player.moveDist;
            proxy.flyDist = player.flyDist;
            proxy.setPose(player.getPose());
            proxy.setOnGround(player.onGround());
            proxy.setDeltaMovement(player.getDeltaMovement());
            proxy.setSprinting(player.isSprinting());
            proxy.setShiftKeyDown(player.isShiftKeyDown());
            proxy.setSwimming(player.isSwimming());
            proxy.setInvisible(player.isInvisible());
            proxy.setGlowingTag(player.isCurrentlyGlowing());
            ClientProtocolState.identity(player.getUUID()).ifPresent(identity -> {
                proxy.setCustomName(identity.displayName());
                proxy.setCustomNameVisible(true);
            });
            if (proxy instanceof LivingEntity living) {
                living.setYHeadRot(player.getYHeadRot());
                living.setYBodyRot(player.yBodyRot);
                living.yHeadRotO = player.yHeadRotO;
                living.yBodyRotO = player.yBodyRotO;
                living.attackAnim = player.attackAnim;
                living.swinging = player.swinging;
                living.swingingArm = player.swingingArm;
                living.swingTime = player.swingTime;
                if (advanceAnimation) {
                    if (entityAnimationAdvanced) {
                        living.walkAnimation.setSpeed(player.walkAnimation.speed());
                    } else {
                        living.walkAnimation.update(player.walkAnimation.speed(), 1.0F);
                    }
                    cached = new DisguiseRenderProxy(
                            cached.revision(),
                            cached.entity(),
                            player.tickCount,
                            cached.tickAnimations());
                    DISGUISE_PROXIES.put(projection.subjectId(), cached);
                }
            }
            renderingDisguiseProxy = true;
            try {
                minecraft.getEntityRenderDispatcher().render(
                        proxy,
                        0.0D,
                        0.0D,
                        0.0D,
                        player.getYRot(),
                        event.getPartialTick(),
                        event.getPoseStack(),
                        event.getMultiBufferSource(),
                        event.getPackedLight());
            } finally {
                renderingDisguiseProxy = false;
            }
            event.setCanceled(true);
        } catch (RuntimeException exception) {
            DISGUISE_PROXIES.remove(event.getEntity().getUUID());
            FAILED_DISGUISE_REVISIONS.put(
                    event.getEntity().getUUID(),
                    projection.disguiseRevision());
            ServerEssentialsForge.LOGGER.warn(
                    "Could not render disguise projection {}",
                    projection.reference(),
                    exception);
        }
    }

    private static void prepareAnimationTick(Entity proxy, Player player) {
        proxy.setPos(player.getX(), player.getY(), player.getZ());
        proxy.xo = player.xo;
        proxy.yo = player.yo;
        proxy.zo = player.zo;
        proxy.xOld = player.xOld;
        proxy.yOld = player.yOld;
        proxy.zOld = player.zOld;
        proxy.setYRot(player.getYRot());
        proxy.setXRot(player.getXRot());
        proxy.yRotO = player.yRotO;
        proxy.xRotO = player.xRotO;
        proxy.tickCount = Math.max(0, player.tickCount - 1);
        proxy.setPose(player.getPose());
        proxy.setOnGround(player.onGround());
        proxy.setDeltaMovement(player.getDeltaMovement());
        proxy.setSprinting(player.isSprinting());
        proxy.setShiftKeyDown(player.isShiftKeyDown());
        proxy.setSwimming(player.isSwimming());
        if (proxy instanceof LivingEntity living) {
            living.setYHeadRot(player.getYHeadRot());
            living.setYBodyRot(player.yBodyRot);
            living.yHeadRotO = player.yHeadRotO;
            living.yBodyRotO = player.yBodyRotO;
        }
    }

    @SubscribeEvent
    public static void renderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || !ClientProtocolState.negotiated(SefProtocol.Feature.IDENTITY_PROJECTION)) {
            return;
        }
        ClientProtocolState.identity(player.getUUID()).ifPresentOrElse(
                identity -> event.setContent(identity.displayName()),
                () -> event.setCanRender(TriState.FALSE));
    }

    private static void renderTag(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int width,
            int height
    ) {
        int sourceWidth = Math.max(1, width);
        int sourceHeight = Math.max(1, height);
        graphics.fill(x - 2, y - 2, x + 18, y + 18, 0xb0000000);
        graphics.blit(texture, x, y, 0.0F, 0.0F, 16, 16, sourceWidth, sourceHeight);
    }

    private static Placement findPausePlacement(ScreenEvent.Init.Post event) {
        int right = Math.max(5, event.getScreen().width - 105);
        int bottom = Math.max(5, event.getScreen().height - 25);
        for (int x : new int[]{right, 5}) {
            for (int y = 5; y <= bottom; y += 22) {
                int candidateY = y;
                boolean occupied = event.getListenersList().stream()
                        .filter(AbstractWidget.class::isInstance)
                        .map(AbstractWidget.class::cast)
                        .anyMatch(widget -> intersects(x, candidateY, 100, 20, widget));
                if (!occupied) {
                    return new Placement(x, candidateY);
                }
            }
        }
        return null;
    }

    private static boolean intersects(int x, int y, int width, int height, AbstractWidget widget) {
        return x < widget.getX() + widget.getWidth()
                && x + width > widget.getX()
                && y < widget.getY() + widget.getHeight()
                && y + height > widget.getY();
    }

    private record Placement(int x, int y) {
    }

    private record DisguiseRenderProxy(
            long revision,
            Entity entity,
            int animationTick,
            boolean tickAnimations
    ) {
    }
}
