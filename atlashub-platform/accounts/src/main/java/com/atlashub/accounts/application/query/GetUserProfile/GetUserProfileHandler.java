package com.atlashub.accounts.application.query.GetUserProfile;

import com.atlashub.accounts.domain.exception.UserNotFoundException;
import com.atlashub.accounts.domain.model.Organization;
import com.atlashub.accounts.domain.model.User;
import com.atlashub.accounts.domain.repository.OrganizationRepository;
import com.atlashub.accounts.domain.repository.UserRepository;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetUserProfileHandler extends Query<GetUserProfileQuery, UserProfileResult> {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final MembershipQueryPort membershipQueryPort;

    public GetUserProfileHandler(UserRepository userRepository,
                                 OrganizationRepository organizationRepository,
                                 MembershipQueryPort membershipQueryPort) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.membershipQueryPort = membershipQueryPort;
    }

    @Override
    public UserProfileResult execute(GetUserProfileQuery query) {
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Long> orgIds = membershipQueryPort.listOrganizationIds(query.userId());

        List<OrganizationSummary> organizations = organizationRepository.findAllByIds(orgIds)
                .stream()
                .map(this::toSummary)
                .toList();

        return new UserProfileResult(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail().value(),
                user.getPhone() != null ? user.getPhone().value() : null,
                user.getImageUrl(),
                user.getCountry().code(),
                user.getActiveOrganizationId(),
                user.getCreatedAt(),
                organizations
        );
    }

    private OrganizationSummary toSummary(Organization org) {
        return new OrganizationSummary(
                org.getId(),
                org.getBusinessName(),
                org.getCountry().code(),
                org.getBaseCurrency().name(),
                org.getLogoUrl()
        );
    }
}
