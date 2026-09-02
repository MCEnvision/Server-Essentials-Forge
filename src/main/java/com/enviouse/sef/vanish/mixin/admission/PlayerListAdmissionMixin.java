package com.enviouse.sef.vanish.mixin.admission;

import com.enviouse.sef.control.MinecraftServerControlRuntime;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerList.class)
public abstract class PlayerListAdmissionMixin {
    @Inject(method = "canPlayerLogin", at = @At("RETURN"), cancellable = true)
    private void sef$allowAdmissionNegotiationAtCapacity(
            SocketAddress address,
            GameProfile profile,
            CallbackInfoReturnable<Component> callback
    ) {
        Component denial = callback.getReturnValue();
        if (denial != null
                && denial.equals(Component.translatable("multiplayer.disconnect.server_full"))
                && MinecraftServerControlRuntime.allowsFullServerNegotiation(profile.getId())) {
            callback.setReturnValue(null);
        }
    }
}
