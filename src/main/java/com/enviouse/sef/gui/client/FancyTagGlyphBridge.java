package com.enviouse.sef.gui.client;

import com.enviouse.sef.gui.protocol.ClientProtocolState;
import com.enviouse.sef.gui.protocol.SefPayloads;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class FancyTagGlyphBridge {
    public static final ResourceLocation FONT =
            ResourceLocation.fromNamespaceAndPath("sef", "fancy_tags");
    private static final int FIRST_CODE_POINT = 0xe000;
    private static final int LAST_CODE_POINT = 0xf8ff;
    private static final Map<UUID, Integer> TAG_CODE_POINTS = new LinkedHashMap<>();
    private static final Map<Integer, UUID> CODE_POINT_TAGS = new LinkedHashMap<>();

    private FancyTagGlyphBridge() {
    }

    public static synchronized void refresh() {
        List<UUID> tags = ClientProtocolState.tagManifests().stream()
                .map(SefPayloads.TagManifestEntry::tagId)
                .distinct()
                .sorted(Comparator.comparing(UUID::toString))
                .toList();
        TAG_CODE_POINTS.keySet().retainAll(tags);
        CODE_POINT_TAGS.entrySet().removeIf(entry -> !tags.contains(entry.getValue()));
        for (UUID tag : tags) {
            if (TAG_CODE_POINTS.containsKey(tag)) {
                continue;
            }
            int codePoint = FIRST_CODE_POINT;
            while (codePoint <= LAST_CODE_POINT && CODE_POINT_TAGS.containsKey(codePoint)) {
                codePoint++;
            }
            if (codePoint > LAST_CODE_POINT) {
                break;
            }
            TAG_CODE_POINTS.put(tag, codePoint);
            CODE_POINT_TAGS.put(codePoint, tag);
        }
    }

    public static synchronized Optional<GlyphInfo> glyphInfo(int codePoint) {
        UUID tagId = CODE_POINT_TAGS.get(codePoint);
        if (tagId == null) {
            return Optional.empty();
        }
        FancyTagClientCache.TextureFacts facts = FancyTagClientCache.facts(tagId).orElse(null);
        if (facts == null) {
            return Optional.empty();
        }
        float width = Math.clamp(8.0F * facts.width() / Math.max(1.0F, facts.height()), 2.0F, 16.0F);
        return Optional.of(new TagGlyphInfo(width));
    }

    public static synchronized Optional<BakedGlyph> glyph(int codePoint) {
        UUID tagId = CODE_POINT_TAGS.get(codePoint);
        if (tagId == null) {
            return Optional.empty();
        }
        var texture = FancyTagClientCache.texture(tagId);
        var facts = FancyTagClientCache.facts(tagId);
        if (texture.isEmpty() || facts.isEmpty()) {
            return Optional.empty();
        }
        float width = Math.clamp(
                8.0F * facts.orElseThrow().width() / Math.max(1.0F, facts.orElseThrow().height()),
                2.0F,
                16.0F);
        return Optional.of(new BakedGlyph(
                GlyphRenderTypes.createForColorTexture(texture.orElseThrow()),
                0.0F,
                1.0F,
                0.0F,
                1.0F,
                0.0F,
                width,
                0.0F,
                8.0F));
    }

    public static synchronized Component chatPrefix(UUID subjectId) {
        return component(subjectId, "chat_prefix");
    }

    public static synchronized Component chatSuffix(UUID subjectId) {
        return component(subjectId, "chat_suffix");
    }

    public static synchronized void clear() {
        TAG_CODE_POINTS.clear();
        CODE_POINT_TAGS.clear();
    }

    private static Component component(UUID subjectId, String slot) {
        List<Integer> codePoints = new ArrayList<>();
        for (SefPayloads.TagAssignmentProjection assignment : ClientProtocolState.tagAssignments()) {
            if (assignment.subjectId().equals(subjectId) && assignment.slot().equals(slot)) {
                Integer codePoint = TAG_CODE_POINTS.get(assignment.tagId());
                if (codePoint != null && FancyTagClientCache.texture(assignment.tagId()).isPresent()) {
                    codePoints.add(codePoint);
                }
            }
        }
        if (codePoints.isEmpty()) {
            return Component.empty();
        }
        StringBuilder text = new StringBuilder();
        codePoints.forEach(text::appendCodePoint);
        return Component.literal(text.toString()).withStyle(style -> style.withFont(FONT));
    }

    private record TagGlyphInfo(float width) implements GlyphInfo {
        @Override
        public float getAdvance() {
            return width + 1.0F;
        }

        @Override
        public BakedGlyph bake(java.util.function.Function<com.mojang.blaze3d.font.SheetGlyphInfo, BakedGlyph> baker) {
            return baker.apply(new SheetGlyphInfo() {
                @Override
                public int getPixelWidth() {
                    return 1;
                }

                @Override
                public int getPixelHeight() {
                    return 1;
                }

                @Override
                public void upload(int x, int y) {
                }

                @Override
                public boolean isColored() {
                    return true;
                }

                @Override
                public float getOversample() {
                    return 1.0F;
                }
            });
        }
    }
}
