package com.atlashub.analytics.application.command.CreateBusinessFeatureSnapshot;

import com.atlashub.analytics.domain.entities.BusinessFeatureSnapshot;
import com.atlashub.analytics.domain.repositories.BusinessFeatureSnapshotRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateBusinessFeatureSnapshotHandler extends Command<CreateBusinessFeatureSnapshotCommand, CreateBusinessFeatureSnapshotResult> {

    private final BusinessFeatureSnapshotRepository repository;

    public CreateBusinessFeatureSnapshotHandler(BusinessFeatureSnapshotRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public CreateBusinessFeatureSnapshotResult execute(CreateBusinessFeatureSnapshotCommand command) {
        String env = command.environment() != null ? command.environment() : "live";
        BusinessFeatureSnapshot snapshot = BusinessFeatureSnapshot.create(
                null,
                command.organizationId(),
                env,
                command.schemaVersion(),
                command.windowStart(),
                command.windowEnd(),
                command.asOf(),
                command.completeness(),
                command.metricValues(),
                command.sourceProjectionHashes(),
                command.expiresAt(),
                command.correlationId()
        );

        BusinessFeatureSnapshot saved = repository.save(snapshot);
        return new CreateBusinessFeatureSnapshotResult(
                saved.getId(),
                saved.getOrganizationId(),
                saved.getEnvironment(),
                saved.getSchemaVersion(),
                saved.getAsOf(),
                saved.getCompleteness()
        );
    }
}
