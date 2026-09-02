package com.enviouse.sef.moderation;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ConnectionAddressServiceTest {
    @Test
    void proxyFailSafeRejectsAnySharedDirectAddress() {
        assertTrue(ConnectionAddressService.sharedActionSafe(1, 1, 10, true));
        assertFalse(ConnectionAddressService.sharedActionSafe(2, 2, 10, true));
        assertFalse(ConnectionAddressService.sharedActionSafe(1, 2, 10, true));
    }

    @Test
    void configuredSharedActionsStillHonorHardCap() {
        assertTrue(ConnectionAddressService.sharedActionSafe(4, 4, 10, false));
        assertFalse(ConnectionAddressService.sharedActionSafe(11, 11, 10, false));
        assertFalse(ConnectionAddressService.sharedActionSafe(-1, 1, 10, false));
    }

    @Test
    void externalProviderUsesTheHighestPriorityRegisteredAdapter() {
        String lowId = "test.external.low";
        String highId = "test.external.high";
        try {
            assertTrue(ConnectionAddressService.registerAdapter(adapter(
                    lowId,
                    ConnectionAddressService.ProviderMode.EXTERNAL,
                    1,
                    new byte[]{10, 0, 0, 1})));
            assertTrue(ConnectionAddressService.registerAdapter(adapter(
                    highId,
                    ConnectionAddressService.ProviderMode.EXTERNAL,
                    10,
                    new byte[]{10, 0, 0, 2})));
            ConnectionAddressService service =
                    new ConnectionAddressService(ConnectionAddressService.ProviderMode.EXTERNAL);

            ConnectionAddressService.Address address =
                    service.forPlayer(mock(net.minecraft.server.level.ServerPlayer.class)).orElseThrow();

            assertEquals("10.0.0.2", address.normalized());
            assertTrue(service.health(null).available());
            assertTrue(service.health(null).detail().contains(highId));
        } finally {
            ConnectionAddressService.unregisterAdapter(lowId);
            ConnectionAddressService.unregisterAdapter(highId);
        }
    }

    @Test
    void adapterFailuresAndMissingAdaptersFailClosed() {
        String id = "test.proxy.failure";
        ConnectionAddressService missing =
                new ConnectionAddressService(ConnectionAddressService.ProviderMode.TRUSTED_PROXY);
        assertFalse(missing.health(null).available());
        assertTrue(missing.forPlayer(mock(net.minecraft.server.level.ServerPlayer.class)).isEmpty());

        try {
            assertTrue(ConnectionAddressService.registerAdapter(new ConnectionAddressService.Adapter() {
                @Override
                public String id() {
                    return id;
                }

                @Override
                public ConnectionAddressService.ProviderMode mode() {
                    return ConnectionAddressService.ProviderMode.TRUSTED_PROXY;
                }

                @Override
                public int priority() {
                    return 1;
                }

                @Override
                public Optional<ConnectionAddressService.ProvidedAddress> resolve(
                        net.minecraft.server.level.ServerPlayer player
                ) {
                    throw new IllegalStateException("provider unavailable");
                }
            }));
            ConnectionAddressService failing =
                    new ConnectionAddressService(ConnectionAddressService.ProviderMode.TRUSTED_PROXY);

            assertTrue(failing.forPlayer(mock(net.minecraft.server.level.ServerPlayer.class)).isEmpty());
            assertFalse(failing.health(null).available());
            assertTrue(failing.health(null).detail().contains(id));
        } finally {
            ConnectionAddressService.unregisterAdapter(id);
        }
    }

    @Test
    void adapterRegistryRejectsUnsafeOrDuplicateDefinitions() {
        String id = "test.external.duplicate";
        try {
            ConnectionAddressService.Adapter adapter = adapter(
                    id,
                    ConnectionAddressService.ProviderMode.EXTERNAL,
                    1,
                    new byte[]{127, 0, 0, 1});
            assertTrue(ConnectionAddressService.registerAdapter(adapter));
            assertFalse(ConnectionAddressService.registerAdapter(adapter));
            assertFalse(ConnectionAddressService.registerAdapter(adapter(
                    "bad adapter",
                    ConnectionAddressService.ProviderMode.EXTERNAL,
                    1,
                    new byte[]{127, 0, 0, 1})));
            assertFalse(ConnectionAddressService.registerAdapter(adapter(
                    "test.direct",
                    ConnectionAddressService.ProviderMode.DIRECT,
                    1,
                    new byte[]{127, 0, 0, 1})));
        } finally {
            ConnectionAddressService.unregisterAdapter(id);
        }
    }

    @Test
    void replacingAFailedAdapterClearsItsStaleHealthState() {
        String failedId = "test.external.failed";
        String healthyId = "test.external.healthy";
        ConnectionAddressService service =
                new ConnectionAddressService(ConnectionAddressService.ProviderMode.EXTERNAL);
        try {
            assertTrue(ConnectionAddressService.registerAdapter(new ConnectionAddressService.Adapter() {
                @Override
                public String id() {
                    return failedId;
                }

                @Override
                public ConnectionAddressService.ProviderMode mode() {
                    return ConnectionAddressService.ProviderMode.EXTERNAL;
                }

                @Override
                public int priority() {
                    return 10;
                }

                @Override
                public Optional<ConnectionAddressService.ProvidedAddress> resolve(
                        net.minecraft.server.level.ServerPlayer player
                ) {
                    return Optional.empty();
                }
            }));
            assertTrue(service.forPlayer(mock(net.minecraft.server.level.ServerPlayer.class)).isEmpty());
            assertFalse(service.health(null).available());
            assertTrue(ConnectionAddressService.unregisterAdapter(failedId));
            assertTrue(ConnectionAddressService.registerAdapter(adapter(
                    healthyId,
                    ConnectionAddressService.ProviderMode.EXTERNAL,
                    1,
                    new byte[]{127, 0, 0, 1})));

            assertTrue(service.health(null).available());
        } finally {
            ConnectionAddressService.unregisterAdapter(failedId);
            ConnectionAddressService.unregisterAdapter(healthyId);
        }
    }

    private static ConnectionAddressService.Adapter adapter(
            String id,
            ConnectionAddressService.ProviderMode mode,
            int priority,
            byte[] address
    ) {
        return new ConnectionAddressService.Adapter() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public ConnectionAddressService.ProviderMode mode() {
                return mode;
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public Optional<ConnectionAddressService.ProvidedAddress> resolve(
                    net.minecraft.server.level.ServerPlayer player
            ) {
                return Optional.of(new ConnectionAddressService.ProvidedAddress(address));
            }
        };
    }
}
