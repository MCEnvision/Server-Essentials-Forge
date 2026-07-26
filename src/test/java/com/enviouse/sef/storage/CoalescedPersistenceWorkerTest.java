package com.enviouse.sef.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

class CoalescedPersistenceWorkerTest {
    @Test
    void coalescesQueuedWritesToTheLatestSnapshot() throws Exception {
        AtomicInteger storedValue = new AtomicInteger(-1);
        AtomicInteger executions = new AtomicInteger();
        AtomicReference<Exception> failure = new AtomicReference<>();
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CoalescedPersistenceWorker worker =
                new CoalescedPersistenceWorker("sef-storage-test", failure::set);

        assertTrue(worker.submit(() -> {
            executions.incrementAndGet();
            firstStarted.countDown();
            assertTrue(releaseFirst.await(2, TimeUnit.SECONDS));
            storedValue.set(0);
        }));
        assertTrue(firstStarted.await(2, TimeUnit.SECONDS));

        for (int value = 1; value <= 100; value++) {
            int snapshot = value;
            assertTrue(worker.submit(() -> {
                executions.incrementAndGet();
                storedValue.set(snapshot);
            }));
        }
        releaseFirst.countDown();

        assertTrue(worker.shutdown(Duration.ofSeconds(2)));
        assertEquals(100, storedValue.get());
        assertEquals(2, executions.get());
        assertNull(failure.get());
        assertFalse(worker.submit(() -> storedValue.set(101)));
    }

    @Test
    void reportsFailureAndRecoversOnANewerSnapshot() {
        AtomicReference<Exception> failure = new AtomicReference<>();
        CoalescedPersistenceWorker worker =
                new CoalescedPersistenceWorker("sef-storage-test", failure::set);

        assertTrue(worker.submit(() -> {
            throw new IllegalStateException("write failed");
        }));
        assertFalse(worker.flush(Duration.ofSeconds(2)));
        assertEquals("write failed", failure.get().getMessage());

        assertTrue(worker.submit(() -> {}));
        assertTrue(worker.flush(Duration.ofSeconds(2)));
        assertTrue(worker.shutdown(Duration.ofSeconds(2)));
    }
}
