package com.atlashub.admin.application.usecase;

import com.atlashub.admin.application.command.VerifyOrganizationCommand;
import com.atlashub.identity.application.port.OrganizationQueryService;
import com.atlashub.identity.application.result.OrganizationProfileDto;
import com.atlashub.admin.domain.model.AdminAction;
import com.atlashub.admin.domain.repository.AdminActionRepository;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.domain.event.DomainEventPublisher;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.exception.SharedErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerifyOrganizationUseCase extends BaseUseCase<VerifyOrganizationCommand, Void> {

    private final AdminActionRepository actionRepository;
    private final OrganizationQueryService organizationQueryService;
    private final DomainEventPublisher publisher;

    public VerifyOrganizationUseCase(AdminActionRepository actionRepository,
                                     OrganizationQueryService organizationQueryService,
                                     DomainEventPublisher publisher) {
        this.actionRepository = actionRepository;
        this.organizationQueryService = organizationQueryService;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Void execute(VerifyOrganizationCommand command) {
        OrganizationProfileDto organization = organizationQueryService.getProfile(Long.valueOf(command.organizationId()))
                .orElseThrow(() -> new BusinessRuleException(SharedErrorCode.NOT_FOUND, "Organization not found"));

        Long id = actionRepository.nextIdentity();
        AdminAction action = AdminAction.record(
                id,
                command.adminUserId(),
                "ORGANIZATION",
                command.organizationId().toString(),
                "VERIFY_ORGANIZATION",
                command.notes()
        );

        actionRepository.save(action);
        publishEvents(action, publisher);
        return null;
    }
}
