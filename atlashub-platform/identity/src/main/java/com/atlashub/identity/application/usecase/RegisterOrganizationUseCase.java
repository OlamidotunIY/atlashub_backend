package com.atlashub.identity.application.usecase;

import com.atlashub.identity.application.command.RegisterOrganizationCommand;
import com.atlashub.identity.application.result.RegisterOrganizationResult;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.model.OrganizationMember;
import com.atlashub.identity.domain.model.User;
import com.atlashub.identity.domain.repository.OrganizationMemberRepository;
import com.atlashub.identity.domain.repository.OrganizationRepository;
import com.atlashub.identity.domain.repository.UserRepository;
import com.atlashub.identity.domain.valueobject.OrganizationRole;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.valueobject.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterOrganizationUseCase extends BaseUseCase<RegisterOrganizationCommand, RegisterOrganizationResult> {

    private static final Logger log = LoggerFactory.getLogger(RegisterOrganizationUseCase.class);

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    public RegisterOrganizationUseCase(
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository memberRepository,
            UserRepository userRepository,
            DomainEventPublisher eventPublisher) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public RegisterOrganizationResult execute(RegisterOrganizationCommand command) {
        log.info("Starting organization registration for user id: {}", command.userId());

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.USER_NOT_FOUND, "User not found"));

        Organization organization = new Organization(
                organizationRepository.nextIdentity(),
                command.businessName(),
                command.businessType(),
                command.businessSize(),
                Country.valueOf(user.getCountry().name()).getDefaultCurrency(),
                command.logoUrl()
        );

        organizationRepository.save(organization);
        log.debug("Organization saved with id: {}", organization.getId());

        OrganizationMember ownerMembership = new OrganizationMember(
                organization.getId(),
                command.userId(),
                OrganizationRole.OWNER
        );
        memberRepository.save(ownerMembership);
        publishEvents(ownerMembership, eventPublisher);
        log.debug("Owner membership created for user {} in organization {}", command.userId(), organization.getId());

        if (user.getActiveOrganizationId() == null) {
            user.switchActiveOrganization(organization.getId());
            userRepository.save(user);
            publishEvents(user, eventPublisher);
            log.debug("Set activeOrganizationId={} for user {}", organization.getId(), command.userId());
        }

        publishEvents(organization, eventPublisher);
        log.info("Successfully registered organization id: {}", organization.getId());

        return new RegisterOrganizationResult(organization.getId());
    }
}
