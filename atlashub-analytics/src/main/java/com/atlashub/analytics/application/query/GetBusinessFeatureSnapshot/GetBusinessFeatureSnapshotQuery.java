package com.atlashub.analytics.application.query.GetBusinessFeatureSnapshot;

public record GetBusinessFeatureSnapshotQuery(
        Long snapshotId,
        Long organizationId,
        String environment,
        String schemaVersion
) {}
