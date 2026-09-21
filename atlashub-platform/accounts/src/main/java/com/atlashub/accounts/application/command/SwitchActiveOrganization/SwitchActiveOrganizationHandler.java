package com.atlashub.accounts.application.command.SwitchActiveOrganization;

import com.atlashub.accounts.application.command.RegisterOrg.RegisterOrganizationHandler;
import com.atlashub.accounts.domain.exception.OrganizationNotFoundException;
import com.atlashub.accounts.domain.exception.UserNotFoundException;
import com.atlashub.accounts.domain.model.Organization;
import com.atlashub.accounts.domain.model.User;
import com.atlashub.accounts.domain.repository.OrganizationRepository;
import com.atlashub.accounts.domain.repository.UserRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class SwitchActiveOrganizationHandler extends BaseUseCase<SwitchActiveOrganizationCommand, SwitchActiveOrganizationResult> {

    private static final Logger log = LoggerFactory.getLogger(RegisterOrganizationHandler.class);

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    public SwitchActiveOrganizationHandler(OrganizationRepository organizationRepository, UserRepository userRepository, DomainEventPublisher eventPublisher) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public SwitchActiveOrganizationResult execute(SwitchActiveOrganizationCommand input) {
        log.info("Starting organization switch for user id: {}", input.userId());

        User user = userRepository.findById(input.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Organization organization = organizationRepository.findById(input.orgId()).orElseThrow(() -> new OrganizationNotFoundException("Organization not found"));

        user.switchActiveOrganization(organization.getId());

        userRepository.save(user);

        log.debug("user active organization switched successfully to {}", user.getActiveOrganizationId());

        publishEvents(user, eventPublisher);

        return new SwitchActiveOrganizationResult(user);
    }
}
