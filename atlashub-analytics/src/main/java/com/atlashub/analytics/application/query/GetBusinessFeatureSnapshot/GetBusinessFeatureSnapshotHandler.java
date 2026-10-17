package com.atlashub.analytics.application.query.GetBusinessFeatureSnapshot;

import com.atlashub.analytics.domain.entities.BusinessFeatureSnapshot;
import com.atlashub.analytics.domain.exceptions.FeatureSnapshotNotFoundException;
import com.atlashub.analytics.domain.repositories.BusinessFeatureSnapshotRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetBusinessFeatureSnapshotHandler extends Query<GetBusinessFeatureSnapshotQuery, BusinessFeatureSnapshotResult> {

    private final BusinessFeatureSnapshotRepository repository;

    public GetBusinessFeatureSnapshotHandler(BusinessFeatureSnapshotRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessFeatureSnapshotResult execute(GetBusinessFeatureSnapshotQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        BusinessFeatureSnapshot snapshot;
        if (query.snapshotId() != null) {
            snapshot = repository.findById(query.snapshotId())
                    .orElseThrow(() -> new FeatureSnapshotNotFoundException(query.snapshotId()));
        } else {
            String schema = query.schemaVersion() != null ? query.schemaVersion() : "v1.0";
            snapshot = repository.findLatestByOrgEnv(query.organizationId(), env, schema)
                    .orElseThrow(() -> new FeatureSnapshotNotFoundException(0L));
        }

        return new BusinessFeatureSnapshotResult(
                snapshot.getId(),
                snapshot.getOrganizationId(),
                snapshot.getEnvironment(),
                snapshot.getSchemaVersion(),
                snapshot.getWindowStart(),
                snapshot.getWindowEnd(),
                snapshot.getAsOf(),
                snapshot.getCompleteness(),
                snapshot.getMetricValues(),
                snapshot.getSourceProjectionHashes(),
                snapshot.getGeneratedAt(),
                snapshot.getExpiresAt()
        );
    }
}
