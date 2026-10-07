package com.atlashub.accounts.application.query.GetUserProfile;

import com.atlashub.accounts.domain.exceptions.UserNotFoundException;
import com.atlashub.accounts.domain.entities.Organization;
import com.atlashub.accounts.domain.entities.User;
import com.atlashub.accounts.domain.repositories.OrganizationRepository;
import com.atlashub.accounts.domain.repositories.UserRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.application.port.ComplianceQueryPort;
import com.atlashub.shared.application.port.MembershipQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetUserProfileHandler extends Query<GetUserProfileQuery, UserProfileResult> {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final MembershipQueryPort membershipQueryPort;
    private final ComplianceQueryPort complianceQueryPort;

    public GetUserProfileHandler(UserRepository userRepository, OrganizationRepository organizationRepository,
                                 MembershipQueryPort membershipQueryPort, ComplianceQueryPort complianceQueryPort) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.membershipQueryPort = membershipQueryPort;
        this.complianceQueryPort = complianceQueryPort;
    }

    @Override
    public UserProfileResult execute(GetUserProfileQuery query) {
        User user = userRepository.findById(query.userId()).orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Long> orgIds = membershipQueryPort.listOrganizationIds(query.userId());

        List<OrganizationSummary> organizations = organizationRepository.findAllByIds(orgIds).stream().map(this::toSummary).toList();
        Organization activeOrganization = user.getActiveOrganizationId() == null
                ? null
                : organizationRepository.findById(user.getActiveOrganizationId()).orElse(null);
        String activeOrganizationRole = activeOrganization == null
                ? null
                : membershipQueryPort.getActiveRoleName(user.getId(), activeOrganization.getId()).orElse(null);
        boolean complianceApproved = activeOrganization != null
                && complianceQueryPort.isApproved(activeOrganization.getId());

        return new UserProfileResult(
                user.getId(), user.getFirstName(), user.getLastName(), user.getEmail().value(),
                user.getPhone() != null ? user.getPhone().value() : null, user.getImageUrl(), user.getCountry().code(),
                user.getActiveEnvironment(), complianceApproved, activeOrganizationRole,
                toActiveOrganization(activeOrganization), user.getCreatedAt(), organizations);
    }

    private OrganizationSummary toSummary(Organization org) {
        return new OrganizationSummary(org.getId(), org.getBusinessName(), org.getCountry().code(), org.getBaseCurrency().name(), org.getLogoUrl());
    }

    private ActiveOrganization toActiveOrganization(Organization organization) {
        if (organization == null) return null;
        return new ActiveOrganization(
                organization.getId(), organization.getBusinessName(), organization.getRegistrationType(),
                organization.getIndustry(), organization.getRegistrationDate(), organization.getDescription(),
                organization.getLogoUrl(), organization.getWebsiteUrl(), organization.getCountry().code(),
                organization.getBaseCurrency().name(), organization.getCreatedAt());
    }
}
