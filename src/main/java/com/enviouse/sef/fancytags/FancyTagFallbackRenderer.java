package com.enviouse.sef.fancytags;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.config.PermissionsHandler;
import com.enviouse.sef.gui.protocol.SefProtocol;
import com.enviouse.sef.gui.protocol.SefSessionManager;
import com.enviouse.sef.kernel.KernelServices;
import com.enviouse.sef.permissions.PermissionService;
import com.enviouse.sef.vanish.VanishUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class FancyTagFallbackRenderer {
    private FancyTagFallbackRenderer() {
    }

    public static MutableComponent decorateChat(
            MutableComponent message,
            ServerPlayer sender,
            ServerPlayer viewer
    ) {
        if (!KernelServices.fancyTags().settings().enabled()
                || SefSessionManager.instance().session(viewer)
                .map(session -> session.supports(SefProtocol.Feature.FANCY_TAGS_STATIC))
                .orElse(false)) {
            return message;
        }
        var receivePermission = PermissionsHandler.phasePermission("tags.render.receive");
        if (receivePermission == null || !PermissionService.has(viewer, receivePermission)) {
            return message;
        }
        Set<String> groups = FancyTagGroupResolver.groups(sender.getUUID());
        String team = sender.getTeam() == null ? "" : sender.getTeam().getName();
        List<FancyTagService.ResolvedTag> tags = KernelServices.fancyTags().resolve(
                new FancyTagService.ViewerContext(
                        viewer.getUUID(),
                        sender.getUUID(),
                        groups,
                        team,
                        sender != viewer && VanishUtil.isVanished(sender, viewer)),
                FancyTagService.RenderContext.CHAT,
                permission -> {
                    var node = KernelServices.permissionNode(permission);
                    return node != null && PermissionService.has(viewer, node);
                });
        MutableComponent prefix = Component.empty();
        MutableComponent suffix = Component.empty();
        for (FancyTagService.ResolvedTag tag : tags) {
            if (tag.alternativeText().isBlank()) {
                continue;
            }
            MutableComponent fallback = Component.literal("[" + tag.alternativeText() + "]")
                    .withStyle(ChatFormatting.GRAY);
            if (tag.slot() == FancyTagService.TagSlot.CHAT_PREFIX) {
                prefix.append(fallback).append(" ");
            } else if (tag.slot() == FancyTagService.TagSlot.CHAT_SUFFIX) {
                suffix.append(" ").append(fallback);
            }
        }
        return Component.empty().append(prefix).append(message).append(suffix);
    }
}
