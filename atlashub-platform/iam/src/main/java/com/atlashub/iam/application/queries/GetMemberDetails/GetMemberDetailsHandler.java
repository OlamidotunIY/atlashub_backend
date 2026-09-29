package com.atlashub.iam.application.queries.GetMemberDetails;

import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class GetMemberDetailsHandler extends Query<GetMemberDetailsQuery, MemberDetailsResult> {

    private static final Logger log = LoggerFactory.getLogger(GetMemberDetailsHandler.class);

    private final OrganizationMemberRepository organizationMemberRepository;

    public GetMemberDetailsHandler(OrganizationMemberRepository organizationMemberRepository) {
        this.organizationMemberRepository = organizationMemberRepository;
    }

    @Override
    public MemberDetailsResult execute(GetMemberDetailsQuery query) {
        log.info("Executing GetMemberDetailsQuery");
        
        OrganizationMember member = organizationMemberRepository.findById(query.memberId())
                .orElseThrow(() -> new NotFoundException("Member not found with id: " + query.memberId()));
        
        return new MemberDetailsResult(
                member.getId(),
                member.getOrganizationId(),
                member.getUserId(),
                member.getCustomRoleId(),
                member.getStatus().name(),
                member.getJoinedAt(),
                member.getInvitedBy(),
                member.getUpdatedAt()
        );
    }
}
