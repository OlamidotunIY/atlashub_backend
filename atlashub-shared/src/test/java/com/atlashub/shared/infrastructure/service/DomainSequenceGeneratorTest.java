package com.atlashub.shared.infrastructure.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainSequenceGeneratorTest {

    private static final long JS_MAX_SAFE_INTEGER = 9_007_199_254_740_991L;

    @Test
    @DisplayName("Generates monotonically increasing positive IDs within JS safe range")
    void generatesMonotonicallyIncreasingIdsWithinJsSafeRange() {
        DomainSequenceGenerator generator = new DomainSequenceGenerator(1);

        Long first = generator.nextIdentity("user_seq");
        Long second = generator.nextIdentity("user_seq");

        assertNotNull(first);
        assertNotNull(second);
        assertTrue(first > 0);
        assertTrue(second > first);
        assertTrue(first < JS_MAX_SAFE_INTEGER);
        assertTrue(second < JS_MAX_SAFE_INTEGER);
    }

    @Test
    @DisplayName("Applies expected entity prefix to generated IDs")
    void appliesExpectedEntityPrefix() {
        DomainSequenceGenerator generator = new DomainSequenceGenerator(2);

        Long orgId = generator.nextIdentity("organization_seq");
        Long userId = generator.nextIdentity("user_seq");
        Long outletId = generator.nextIdentity("outlet_seq");
        Long cashId = generator.nextIdentity("cash_evacuation_seq");
        Long authId = generator.nextIdentity("auth_account_seq");
        Long ledgerId = generator.nextIdentity("ledger_account_seq");
        Long defaultId = generator.nextIdentity("unknown_seq");

        assertEquals('1', String.valueOf(orgId).charAt(0));
        assertEquals('2', String.valueOf(userId).charAt(0));
        assertEquals('3', String.valueOf(outletId).charAt(0));
        assertEquals('4', String.valueOf(cashId).charAt(0));
        assertEquals('5', String.valueOf(authId).charAt(0));
        assertEquals('6', String.valueOf(ledgerId).charAt(0));
        assertEquals('9', String.valueOf(defaultId).charAt(0));
    }

    @Test
    @DisplayName("Handles concurrent ID requests without any duplicates")
    void handlesConcurrentRequestsWithoutDuplicates() throws InterruptedException {
        DomainSequenceGenerator generator = new DomainSequenceGenerator(1);
        int threadCount = 10;
        int idsPerThread = 100;
        int totalExpectedIds = threadCount * idsPerThread;

        Set<Long> generatedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < idsPerThread; j++) {
                        generatedIds.add(generator.nextIdentity("user_seq"));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All threads should complete in time");
        assertEquals(totalExpectedIds, generatedIds.size(), "Should produce zero duplicate IDs under concurrency");
    }

    @Test
    @DisplayName("Safely handles null sequence name with fallback prefix")
    void handlesNullSequenceName() {
        DomainSequenceGenerator generator = new DomainSequenceGenerator(0);
        Long id = generator.nextIdentity(null);
        assertNotNull(id);
        assertEquals('9', String.valueOf(id).charAt(0));
    }
}
