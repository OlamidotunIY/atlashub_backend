package com.atlashub.identity.adapter.out.persistence.query;

import com.atlashub.identity.adapter.out.persistence.repository.SpringDataOrganizationMemberRepository;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataUserRepository;
import com.atlashub.identity.application.port.OrganizationMemberQueryService;
import com.atlashub.identity.application.result.OrganizationMemberDto;
import com.atlashub.shared.application.util.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationMemberQueryServiceImpl implements OrganizationMemberQueryService {

    private final SpringDataOrganizationMemberRepository memberRepository;
    private final SpringDataUserRepository userRepository;

    @Override
    public Optional<OrganizationMemberDto> findById(Long organizationId, Long memberId) {
        return memberRepository.findById(memberId)
            .filter(m -> m.getOrganizationId().equals(organizationId))
            .map(m -> new OrganizationMemberDto(
                m.getId(), m.getUserId(), m.getOrganizationId(), m.getRole(), m.getStatus(), m.getJoinedAt()
            ));
    }

    @Override
    public PageResult<OrganizationMemberDto> findAllByOrganizationId(Long organizationId, int page, int size, String searchFilter) {
        return new PageResult<>(Collections.emptyList(), page, size, 0L, 0);
    }

    @Override
    public boolean isUserMemberOfOrganization(Long userId, Long organizationId) {
        return memberRepository.findByOrganizationIdAndUserId(organizationId, userId).isPresent();
    }

    @Override
    public String getOnboardingStatus(Long userId, String email) {
        return userRepository.findById(userId)
            .map(u -> {
                boolean hasOrg = !memberRepository.findByUserId(u.getId()).isEmpty();
                return hasOrg ? "HAS_ORG" : "NO_ORG";
            })
            .orElse("NO_USER");
    }
}
