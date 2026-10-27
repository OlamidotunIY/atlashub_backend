package com.atlashub.eventbus.application.command;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReplayEventsCommandTest {
    private final ZonedDateTime from = ZonedDateTime.parse("2026-10-01T00:00:00Z");
    private final ZonedDateTime to = ZonedDateTime.parse("2026-10-02T00:00:00Z");

    @Test
    void accepts_a_bounded_replay_window() {
        assertDoesNotThrow(() -> new ReplayEventsCommand("commerce.sales", from, to, 500, true, 42L));
    }

    @Test
    void rejects_an_unbounded_or_reversed_replay() {
        assertThrows(IllegalArgumentException.class,
                () -> new ReplayEventsCommand("commerce.sales", from, to, 1_001, false, 42L));
        assertThrows(IllegalArgumentException.class,
                () -> new ReplayEventsCommand("commerce.sales", to, from, 100, false, 42L));
        assertThrows(IllegalArgumentException.class,
                () -> new ReplayEventsCommand(" ", from, to, 100, false, 42L));
    }
}
