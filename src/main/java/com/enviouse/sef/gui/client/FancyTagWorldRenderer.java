package com.enviouse.sef.gui.client;

import com.enviouse.sef.gui.protocol.ClientProtocolState;
import com.enviouse.sef.gui.protocol.SefPayloads;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class FancyTagWorldRenderer {
    private FancyTagWorldRenderer() {
    }

    public static void renderNameplate(
            Entity subject,
            net.minecraft.network.chat.Component name,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            float partialTick
    ) {
        List<SefPayloads.TagAssignmentProjection> prefix = assignments(subject, "nameplate_prefix");
        List<SefPayloads.TagAssignmentProjection> suffix = assignments(subject, "nameplate_suffix");
        if (prefix.isEmpty() && suffix.isEmpty()) {
            return;
        }
        Vec3 attachment = subject.getAttachments().getNullable(
                EntityAttachment.NAME_TAG,
                0,
                subject.getViewYRot(partialTick));
        if (attachment == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(attachment.x, attachment.y + 0.5D, attachment.z);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);
        float halfName = minecraft.font.width(name) / 2.0F;
        float prefixX = -halfName - 2.0F;
        for (int index = prefix.size() - 1; index >= 0; index--) {
            SefPayloads.TagAssignmentProjection assignment = prefix.get(index);
            FancyTagClientCache.TextureFacts facts =
                    FancyTagClientCache.facts(assignment.tagId()).orElse(null);
            if (facts == null) {
                continue;
            }
            float width = tagWidth(facts);
            prefixX -= width;
            renderQuad(
                    poseStack,
                    buffers,
                    assignment,
                    prefixX,
                    -1.0F,
                    width,
                    8.0F,
                    packedLight);
            prefixX -= 1.0F;
        }
        float suffixX = halfName + 2.0F;
        for (SefPayloads.TagAssignmentProjection assignment : suffix) {
            FancyTagClientCache.TextureFacts facts =
                    FancyTagClientCache.facts(assignment.tagId()).orElse(null);
            if (facts == null) {
                continue;
            }
            float width = tagWidth(facts);
            renderQuad(
                    poseStack,
                    buffers,
                    assignment,
                    suffixX,
                    -1.0F,
                    width,
                    8.0F,
                    packedLight);
            suffixX += width + 1.0F;
        }
        poseStack.popPose();
    }

    private static List<SefPayloads.TagAssignmentProjection> assignments(Entity subject, String slot) {
        List<SefPayloads.TagAssignmentProjection> result = new ArrayList<>();
        for (SefPayloads.TagAssignmentProjection assignment : ClientProtocolState.tagAssignments()) {
            if (assignment.subjectId().equals(subject.getUUID())
                    && assignment.slot().equals(slot)
                    && FancyTagClientCache.texture(assignment.tagId()).isPresent()) {
                result.add(assignment);
            }
        }
        result.sort(java.util.Comparator.comparingInt(
                SefPayloads.TagAssignmentProjection::priority).reversed());
        return List.copyOf(result);
    }

    private static float tagWidth(FancyTagClientCache.TextureFacts facts) {
        return Math.clamp(8.0F * facts.width() / Math.max(1.0F, facts.height()), 2.0F, 16.0F);
    }

    private static void renderQuad(
            PoseStack poseStack,
            MultiBufferSource buffers,
            SefPayloads.TagAssignmentProjection assignment,
            float x,
            float y,
            float width,
            float height,
            int packedLight
    ) {
        var texture = FancyTagClientCache.texture(assignment.tagId()).orElse(null);
        if (texture == null) {
            return;
        }
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        Matrix4f matrix = poseStack.last().pose();
        vertices.addVertex(matrix, x, y, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 0.0F)
                .setLight(packedLight);
        vertices.addVertex(matrix, x, y + height, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 1.0F)
                .setLight(packedLight);
        vertices.addVertex(matrix, x + width, y + height, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 1.0F)
                .setLight(packedLight);
        vertices.addVertex(matrix, x + width, y, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 0.0F)
                .setLight(packedLight);
    }
}
