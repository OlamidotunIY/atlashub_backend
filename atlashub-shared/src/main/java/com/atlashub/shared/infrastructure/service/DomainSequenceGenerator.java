package com.atlashub.shared.infrastructure.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DomainSequenceGenerator {

    // Epoch: 2026-01-01T00:00:00Z (UTC)
    private static final long CUSTOM_EPOCH = 1767225600000L;
    private static final long MAX_SEQUENCE = 99L;

    private final int nodeId;
    private long lastTick = -1L;
    private long sequence = 0L;

    public DomainSequenceGenerator(@Value("${atlashub.node-id:1}") int nodeId) {
        this.nodeId = Math.abs(nodeId) % 10;
    }

    public synchronized Long nextIdentity(String sequenceName) {
        long currentTick = getCurrentTick();

        if (currentTick < lastTick) {
            long drift = lastTick - currentTick;
            if (drift <= 50) { // Up to 500ms allowable drift
                try {
                    Thread.sleep(drift * 10L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Thread interrupted while waiting for clock synchronization", e);
                }
                currentTick = getCurrentTick();
                if (currentTick < lastTick) {
                    throw new IllegalStateException("System clock moved backwards by " + drift + " ticks");
                }
            } else {
                throw new IllegalStateException("System clock moved backwards by " + drift + " ticks");
            }
        }

        if (currentTick == lastTick) {
            sequence++;
            if (sequence > MAX_SEQUENCE) {
                currentTick = waitNextTick(currentTick);
                sequence = 0;
                lastTick = currentTick;
            }
        } else {
            sequence = 0;
            lastTick = currentTick;
        }

        long prefix = resolvePrefix(sequenceName);

        // Formula: [Prefix: 1 digit] + [Tick (10ms): 10 digits] + [NodeId: 1 digit] + [Sequence: 2 digits]
        // Example: 2_2451176912_1_00 -> 22451176912100 (14 digits)
        return (prefix * 10_000_000_000_000L)
                + (currentTick * 1_000L)
                + (nodeId * 100L)
                + sequence;
    }

    private long getCurrentTick() {
        return (System.currentTimeMillis() - CUSTOM_EPOCH) / 10L;
    }

    private long waitNextTick(long currentTick) {
        long next = getCurrentTick();
        while (next <= currentTick) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for next time tick", e);
            }
            next = getCurrentTick();
        }
        return next;
    }

    private int resolvePrefix(String sequenceName) {
        if (sequenceName == null) {
            return 9;
        }
        String normalized = sequenceName.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("organization") || normalized.startsWith("org_")) {
            return 1;
        }
        if (normalized.startsWith("user")) {
            return 2;
        }
        if (normalized.startsWith("outlet")) {
            return 3;
        }
        if (normalized.contains("cash") || normalized.contains("bank") || normalized.contains("reconciliation")
                || normalized.contains("deposit") || normalized.contains("sub_account")) {
            return 4;
        }
        if (normalized.contains("auth") || normalized.contains("trusted_device") || normalized.contains("verification")
                || normalized.contains("otp")) {
            return 5;
        }
        if (normalized.contains("ledger") || normalized.contains("settlement") || normalized.contains("charge")
                || normalized.contains("split")) {
            return 6;
        }
        if (normalized.contains("catalog") || normalized.contains("product") || normalized.contains("inventory")
                || normalized.contains("till") || normalized.contains("storefront") || normalized.contains("table")
                || normalized.contains("discount") || normalized.contains("vendor") || normalized.contains("supplier")) {
            return 7;
        }
        if (normalized.contains("compliance") || normalized.contains("provider_onboarding")
                || normalized.contains("api_key") || normalized.contains("role") || normalized.contains("invitation")
                || normalized.contains("member") || normalized.contains("permission")) {
            return 8;
        }
        return 9;
    }
}
