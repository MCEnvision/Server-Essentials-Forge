package com.enviouse.sef.gui.protocol;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SefSessionNegotiationTest {
    @Test
    void requiresTheExactNonceAndCompatibleProtocol() {
        SessionRequestGuardTest.MutableClock clock = new SessionRequestGuardTest.MutableClock();
        SefSessionManager manager = new SefSessionManager(clock);
        Connection forgedConnection = new Connection(PacketFlow.SERVERBOUND);
        SefPayloads.ServerHello forgedHello = manager.begin(forgedConnection);
        assertFalse(manager.acknowledge(
                forgedConnection,
                new SefPayloads.ClientHello(
                        forgedHello.negotiationId(),
                        forgedHello.nonce() + 1L,
                        SefProtocol.MAJOR,
                        SefProtocol.MINOR,
                        SefProtocol.SERVER_FEATURES,
                        true)));

        Connection validConnection = new Connection(PacketFlow.SERVERBOUND);
        SefPayloads.ServerHello validHello = manager.begin(validConnection);
        assertTrue(manager.acknowledge(
                validConnection,
                new SefPayloads.ClientHello(
                        validHello.negotiationId(),
                        validHello.nonce(),
                        SefProtocol.MAJOR,
                        SefProtocol.MINOR,
                        SefProtocol.SERVER_FEATURES,
                        true)));

        Connection incompatibleConnection = new Connection(PacketFlow.SERVERBOUND);
        SefPayloads.ServerHello incompatibleHello = manager.begin(incompatibleConnection);
        assertFalse(manager.acknowledge(
                incompatibleConnection,
                new SefPayloads.ClientHello(
                        incompatibleHello.negotiationId(),
                        incompatibleHello.nonce(),
                        SefProtocol.MAJOR + 1,
                        0,
                        SefProtocol.SERVER_FEATURES,
                        false)));
    }

    @Test
    void expiresUnfinishedNegotiations() {
        SessionRequestGuardTest.MutableClock clock = new SessionRequestGuardTest.MutableClock();
        SefSessionManager manager = new SefSessionManager(clock);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        SefPayloads.ServerHello hello = manager.begin(connection);
        clock.advance(Duration.ofMinutes(3));
        assertFalse(manager.acknowledge(
                connection,
                new SefPayloads.ClientHello(
                        hello.negotiationId(),
                        hello.nonce(),
                        SefProtocol.MAJOR,
                        SefProtocol.MINOR,
                        SefProtocol.SERVER_FEATURES,
                        true)));
    }
}
