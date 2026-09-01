package com.atlashub.identity.adapter.in.internal;

import com.atlashub.identity.domain.model.User;
import com.atlashub.identity.domain.repository.OrganizationMemberRepository;
import com.atlashub.identity.domain.repository.UserRepository;
import com.atlashub.shared.api.OrganizationMemberQueryApi;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IdentityOrganizationMemberApiImpl implements OrganizationMemberQueryApi {

    private final UserRepository userRepository;
    private final OrganizationMemberRepository memberRepository;

    public IdentityOrganizationMemberApiImpl(UserRepository userRepository, OrganizationMemberRepository memberRepository) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public String getOnboardingStatus(Long userId, String email) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            boolean hasOrg = !memberRepository.findByUserId(user.get().getId()).isEmpty();
            return hasOrg ? "HAS_ORG" : "NO_ORG";
        }
        return "NO_USER";
    }
}
