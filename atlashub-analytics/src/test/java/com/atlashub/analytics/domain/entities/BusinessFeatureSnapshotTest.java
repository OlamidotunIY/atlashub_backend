package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.events.AnalyticsFeatureSnapshotReadyEvent;
import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.analytics.domain.valueobject.DataQualityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessFeatureSnapshotTest {

    @Test
    @DisplayName("Should create immutable BusinessFeatureSnapshot and register ready event")
    void shouldCreateSnapshotAndRegisterEvent() {
        ZonedDateTime now = ZonedDateTime.now();
        BusinessFeatureSnapshot snapshot = BusinessFeatureSnapshot.create(
                10L,
                100L,
                "live",
                "v1.0",
                now.minusDays(30),
                now,
                now,
                DataQualityStatus.COMPLETE,
                "{\"salesTotal\": 50000}",
                "{\"daily_sales\": \"hash123\"}",
                now.plusDays(90),
                "corr-test-1"
        );

        assertNotNull(snapshot);
        assertEquals(10L, snapshot.getId());
        assertEquals("v1.0", snapshot.getSchemaVersion());
        assertEquals(DataQualityStatus.COMPLETE, snapshot.getCompleteness());

        assertFalse(snapshot.peekDomainEvents().isEmpty());
        assertEquals(1, snapshot.peekDomainEvents().size());
        AnalyticsFeatureSnapshotReadyEvent event = (AnalyticsFeatureSnapshotReadyEvent) snapshot.peekDomainEvents().get(0);
        assertEquals(10L, event.payload().snapshotId());
        assertEquals("corr-test-1", event.correlationId());
    }

    @Test
    @DisplayName("Should enforce invariants on snapshot creation")
    void shouldEnforceInvariants() {
        ZonedDateTime now = ZonedDateTime.now();
        assertThrows(AnalyticsInvariantException.class, () ->
                BusinessFeatureSnapshot.create(
                        10L,
                        null,
                        "live",
                        "v1.0",
                        now,
                        now,
                        now,
                        DataQualityStatus.COMPLETE,
                        "{}",
                        "{}",
                        now,
                        "corr"
                )
        );

        assertThrows(AnalyticsInvariantException.class, () ->
                BusinessFeatureSnapshot.create(
                        10L,
                        100L,
                        "live",
                        "",
                        now,
                        now,
                        now,
                        DataQualityStatus.COMPLETE,
                        "{}",
                        "{}",
                        now,
                        "corr"
                )
        );
    }
}
