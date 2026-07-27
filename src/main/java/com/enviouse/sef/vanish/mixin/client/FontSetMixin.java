package com.enviouse.sef.vanish.mixin.client;

import com.enviouse.sef.gui.client.FancyTagGlyphBridge;
import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontSet.class)
public final class FontSetMixin {
    @Inject(method = "getGlyphInfo", at = @At("HEAD"), cancellable = true)
    private void sef$dynamicTagGlyphInfo(
            int character,
            boolean filterFishyGlyphs,
            CallbackInfoReturnable<GlyphInfo> callback
    ) {
        if (!((FontSet) (Object) this).name().equals(FancyTagGlyphBridge.FONT)) {
            return;
        }
        FancyTagGlyphBridge.glyphInfo(character).ifPresent(callback::setReturnValue);
    }

    @Inject(method = "getGlyph", at = @At("HEAD"), cancellable = true)
    private void sef$dynamicTagGlyph(
            int character,
            CallbackInfoReturnable<BakedGlyph> callback
    ) {
        if (!((FontSet) (Object) this).name().equals(FancyTagGlyphBridge.FONT)) {
            return;
        }
        FancyTagGlyphBridge.glyph(character).ifPresent(callback::setReturnValue);
    }
}
