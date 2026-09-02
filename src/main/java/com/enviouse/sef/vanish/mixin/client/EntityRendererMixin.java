package com.enviouse.sef.vanish.mixin.client;

import com.enviouse.sef.gui.client.FancyTagWorldRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "renderNameTag", at = @At("TAIL"))
    private void sef$renderFancyTags(
            Entity entity,
            Component displayName,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            float partialTick,
            CallbackInfo callback
    ) {
        FancyTagWorldRenderer.renderNameplate(
                entity,
                displayName,
                poseStack,
                bufferSource,
                packedLight,
                partialTick);
    }
}
