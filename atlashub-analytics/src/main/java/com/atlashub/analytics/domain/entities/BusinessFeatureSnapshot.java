package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.events.AnalyticsFeatureSnapshotReadyEvent;
import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.analytics.domain.valueobject.DataQualityStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class BusinessFeatureSnapshot extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String environment;
    private final String schemaVersion;
    private final ZonedDateTime windowStart;
    private final ZonedDateTime windowEnd;
    private final ZonedDateTime asOf;
    private final DataQualityStatus completeness;
    private final String metricValues;
    private final String sourceProjectionHashes;
    private final ZonedDateTime generatedAt;
    private final ZonedDateTime expiresAt;

    public BusinessFeatureSnapshot(
            Long id,
            Long organizationId,
            String environment,
            String schemaVersion,
            ZonedDateTime windowStart,
            ZonedDateTime windowEnd,
            ZonedDateTime asOf,
            DataQualityStatus completeness,
            String metricValues,
            String sourceProjectionHashes,
            ZonedDateTime generatedAt,
            ZonedDateTime expiresAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.schemaVersion = schemaVersion;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.asOf = asOf;
        this.completeness = completeness;
        this.metricValues = metricValues;
        this.sourceProjectionHashes = sourceProjectionHashes;
        this.generatedAt = generatedAt;
        this.expiresAt = expiresAt;
    }

    public static BusinessFeatureSnapshot create(
            Long id,
            Long organizationId,
            String environment,
            String schemaVersion,
            ZonedDateTime windowStart,
            ZonedDateTime windowEnd,
            ZonedDateTime asOf,
            DataQualityStatus completeness,
            String metricValues,
            String sourceProjectionHashes,
            ZonedDateTime expiresAt,
            String correlationId
    ) {
        if (organizationId == null) {
            throw new AnalyticsInvariantException("Organization ID must not be null");
        }
        if (schemaVersion == null || schemaVersion.isBlank()) {
            throw new AnalyticsInvariantException("Schema version must not be blank");
        }
        if (completeness == null) {
            throw new AnalyticsInvariantException("Completeness status must not be null");
        }

        ZonedDateTime now = ZonedDateTime.now();
        BusinessFeatureSnapshot snapshot = new BusinessFeatureSnapshot(
                id,
                organizationId,
                environment != null ? environment : "live",
                schemaVersion,
                windowStart,
                windowEnd,
                asOf != null ? asOf : now,
                completeness,
                metricValues != null ? metricValues : "{}",
                sourceProjectionHashes != null ? sourceProjectionHashes : "{}",
                now,
                expiresAt != null ? expiresAt : now.plusDays(90)
        );

        snapshot.registerEvent(AnalyticsFeatureSnapshotReadyEvent.of(
                id,
                correlationId,
                new AnalyticsFeatureSnapshotReadyEvent.Payload(
                        id,
                        organizationId,
                        snapshot.getEnvironment(),
                        schemaVersion,
                        snapshot.getAsOf(),
                        completeness
                )
        ));

        return snapshot;
    }

    @Override
    public Long getId() {
        return id;
    }
}
