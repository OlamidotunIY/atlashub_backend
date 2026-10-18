package com.atlashub.analytics.application.command.UpdateHeadcountProjection;

import com.atlashub.analytics.domain.entities.HeadcountProjection;
import com.atlashub.analytics.domain.repositories.HeadcountProjectionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateHeadcountProjectionHandler extends Command<UpdateHeadcountProjectionCommand, Void> {

    private final HeadcountProjectionRepository repository;

    public UpdateHeadcountProjectionHandler(HeadcountProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(UpdateHeadcountProjectionCommand command) {
        String env = command.environment() != null ? command.environment() : "live";
        HeadcountProjection projection = repository.findByOrgEnv(
                command.organizationId(),
                env
        ).orElseGet(() -> HeadcountProjection.create(
                null,
                command.organizationId(),
                env
        ));

        projection.applyEmployeeChange(command.changeType());
        repository.save(projection);
        return null;
    }
}
