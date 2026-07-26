package com.enviouse.sef.commandlog;

import com.enviouse.sef.ServerEssentialsForge;
import com.enviouse.sef.config.ConfigHandler;
import com.enviouse.sef.kernel.KernelServices;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = ServerEssentialsForge.MODID)
public final class CommandObservationEvents {
    private CommandObservationEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCommand(CommandEvent event) {
        if (ConfigHandler.config.enableCommandSpy.get() || KernelServices.fileLogs().health().accepting()) {
            KernelServices.commandJournal().onCommand(event);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        KernelServices.commandJournal().clearDeliveryState(event.getEntity().getUUID());
    }
}
