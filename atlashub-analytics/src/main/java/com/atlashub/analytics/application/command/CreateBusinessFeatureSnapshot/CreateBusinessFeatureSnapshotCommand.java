package com.atlashub.analytics.application.command.CreateBusinessFeatureSnapshot;

import com.atlashub.analytics.domain.valueobject.DataQualityStatus;
import java.time.ZonedDateTime;

public record CreateBusinessFeatureSnapshotCommand(
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
) {}
