package com.atlashub.analytics.application.command.CreateBusinessFeatureSnapshot;

import com.atlashub.analytics.domain.valueobject.DataQualityStatus;
import java.time.ZonedDateTime;

public record CreateBusinessFeatureSnapshotResult(
        Long snapshotId,
        Long organizationId,
        String environment,
        String schemaVersion,
        ZonedDateTime asOf,
        DataQualityStatus completeness
) {}
