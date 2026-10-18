package com.atlashub.analytics.application.query.GetBusinessFeatureSnapshot;

import com.atlashub.analytics.domain.valueobject.DataQualityStatus;
import java.time.ZonedDateTime;

public record BusinessFeatureSnapshotResult(
        Long snapshotId,
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
) {}
