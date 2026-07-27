package com.enviouse.sef.vanish.mixin.client;

import com.enviouse.sef.gui.client.FancyTagClientCache;
import com.enviouse.sef.gui.protocol.ClientProtocolState;
import com.enviouse.sef.gui.protocol.SefPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private Component header;

    @Invoker("getPlayerInfos")
    public abstract List<PlayerInfo> sef$getPlayerInfos();

    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void sef$reserveFancyTagSpace(
            PlayerInfo playerInfo,
            CallbackInfoReturnable<Component> callback
    ) {
        List<SefPayloads.TagAssignmentProjection> prefix = assignments(
                playerInfo.getProfile().getId(),
                "tab_prefix");
        List<SefPayloads.TagAssignmentProjection> suffix = assignments(
                playerInfo.getProfile().getId(),
                "tab_suffix");
        if (prefix.isEmpty() && suffix.isEmpty()) {
            return;
        }
        Component prefixSpace = Component.literal("  ".repeat(prefix.size()));
        Component suffixSpace = Component.literal("  ".repeat(suffix.size()));
        callback.setReturnValue(Component.empty()
                .append(prefixSpace)
                .append(callback.getReturnValue())
                .append(suffixSpace));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void sef$renderFancyTags(
            GuiGraphics graphics,
            int screenWidth,
            Scoreboard scoreboard,
            Objective objective,
            CallbackInfo callback
    ) {
        List<PlayerInfo> players = sef$getPlayerInfos();
        if (players.isEmpty()) {
            return;
        }
        int maximumNameWidth = 0;
        int maximumScoreWidth = 0;
        int spaceWidth = minecraft.font.width(" ");
        for (PlayerInfo player : players) {
            maximumNameWidth = Math.max(
                    maximumNameWidth,
                    minecraft.font.width(((PlayerTabOverlay) (Object) this).getNameForDisplay(player)));
            if (objective != null && objective.getRenderType() != ObjectiveCriteria.RenderType.HEARTS) {
                var score = scoreboard.getPlayerScoreInfo(
                        net.minecraft.world.scores.ScoreHolder.fromGameProfile(player.getProfile()),
                        objective);
                if (score != null) {
                    var formatted = net.minecraft.world.scores.ReadOnlyScoreInfo.safeFormatValue(
                            score,
                            objective.numberFormatOrDefault(
                                    net.minecraft.network.chat.numbers.StyledFormat.PLAYER_LIST_DEFAULT));
                    maximumScoreWidth = Math.max(maximumScoreWidth, minecraft.font.width(formatted));
                }
            }
        }
        int playerCount = players.size();
        int rows = playerCount;
        int columns = 1;
        while (rows > PlayerTabOverlay.MAX_ROWS_PER_COL) {
            columns++;
            rows = (playerCount + columns - 1) / columns;
        }
        boolean showFaces = minecraft.isLocalServer()
                || minecraft.getConnection().getConnection().isEncrypted();
        int scoreWidth = objective == null
                ? 0
                : objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS
                ? 90
                : maximumScoreWidth > 0 ? spaceWidth + maximumScoreWidth : 0;
        int columnWidth = Math.min(
                columns * ((showFaces ? 9 : 0) + maximumNameWidth + scoreWidth + 13),
                screenWidth - 50) / columns;
        int left = screenWidth / 2 - (columnWidth * columns + (columns - 1) * 5) / 2;
        int top = 10;
        int contentWidth = columnWidth * columns + (columns - 1) * 5;
        if (header != null) {
            List<net.minecraft.util.FormattedCharSequence> lines =
                    minecraft.font.split(header, screenWidth - 50);
            for (net.minecraft.util.FormattedCharSequence line : lines) {
                contentWidth = Math.max(contentWidth, minecraft.font.width(line));
            }
            top += lines.size() * 9 + 1;
        }
        for (int index = 0; index < playerCount; index++) {
            PlayerInfo player = players.get(index);
            List<SefPayloads.TagAssignmentProjection> prefix =
                    assignments(player.getProfile().getId(), "tab_prefix");
            List<SefPayloads.TagAssignmentProjection> suffix =
                    assignments(player.getProfile().getId(), "tab_suffix");
            if (prefix.isEmpty() && suffix.isEmpty()) {
                continue;
            }
            int column = index / rows;
            int row = index % rows;
            int nameX = left + column * columnWidth + column * 5 + (showFaces ? 9 : 0);
            int y = top + row * 9;
            int prefixX = nameX;
            for (SefPayloads.TagAssignmentProjection assignment : prefix) {
                renderTag(graphics, assignment, prefixX, y);
                prefixX += minecraft.font.width("  ");
            }
            Component display = ((PlayerTabOverlay) (Object) this).getNameForDisplay(player);
            int suffixX = nameX + minecraft.font.width(display)
                    - suffix.size() * minecraft.font.width("  ");
            for (SefPayloads.TagAssignmentProjection assignment : suffix) {
                renderTag(graphics, assignment, suffixX, y);
                suffixX += minecraft.font.width("  ");
            }
        }
    }

    private void renderTag(
            GuiGraphics graphics,
            SefPayloads.TagAssignmentProjection assignment,
            int x,
            int y
    ) {
        var texture = FancyTagClientCache.texture(assignment.tagId());
        var facts = FancyTagClientCache.facts(assignment.tagId());
        if (texture.isEmpty() || facts.isEmpty()) {
            return;
        }
        int width = Math.clamp(
                Math.round(8.0F * facts.orElseThrow().width()
                        / Math.max(1.0F, facts.orElseThrow().height())),
                2,
                8);
        graphics.blit(
                texture.orElseThrow(),
                x,
                y,
                0.0F,
                0.0F,
                width,
                8,
                facts.orElseThrow().width(),
                facts.orElseThrow().height());
    }

    private static List<SefPayloads.TagAssignmentProjection> assignments(UUID subjectId, String slot) {
        List<SefPayloads.TagAssignmentProjection> result = new ArrayList<>();
        for (SefPayloads.TagAssignmentProjection assignment : ClientProtocolState.tagAssignments()) {
            if (assignment.subjectId().equals(subjectId)
                    && assignment.slot().equals(slot)
                    && FancyTagClientCache.texture(assignment.tagId()).isPresent()) {
                result.add(assignment);
            }
        }
        result.sort(Comparator.comparingInt(
                SefPayloads.TagAssignmentProjection::priority).reversed());
        return List.copyOf(result);
    }
}
