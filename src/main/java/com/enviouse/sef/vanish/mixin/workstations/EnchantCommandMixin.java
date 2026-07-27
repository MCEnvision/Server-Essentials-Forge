package com.enviouse.sef.vanish.mixin.workstations;

import com.enviouse.sef.workstations.AdministrativeEnchantCommands;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.EnchantCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantCommand.class)
public abstract class EnchantCommandMixin {
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void sef$register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext buildContext,
            CallbackInfo callback
    ) {
        AdministrativeEnchantCommands.registerVanillaRoot(dispatcher, buildContext);
        callback.cancel();
    }
}
