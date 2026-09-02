package com.enviouse.sef.gui.protocol;

import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

import java.util.Objects;
import java.util.function.Consumer;

final class SefConfigurationTask implements ICustomConfigurationTask {
    static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type("sef:capabilities");

    private final SefPayloads.ServerHello hello;

    SefConfigurationTask(SefPayloads.ServerHello hello) {
        this.hello = Objects.requireNonNull(hello, "hello");
    }

    @Override
    public void run(Consumer<net.minecraft.network.protocol.common.custom.CustomPacketPayload> sender) {
        sender.accept(hello);
    }

    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }
}
