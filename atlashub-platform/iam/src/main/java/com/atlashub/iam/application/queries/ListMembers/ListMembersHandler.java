package com.atlashub.iam.application.queries.ListMembers;

import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;

import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.valueobject.MemberStatus;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListMembersHandler extends Query<ListMembersQuery, List<MemberResult>> {

    private static final Logger log = LoggerFactory.getLogger(ListMembersHandler.class);
    private final OrganizationMemberRepository organizationMemberRepository;

    public ListMembersHandler(OrganizationMemberRepository organizationMemberRepository) {
        this.organizationMemberRepository = organizationMemberRepository;
    }

    @Override
    public List<MemberResult> execute(ListMembersQuery query) {
        log.info("Executing ListMembersQuery for orgId: {}", query.orgId());
        
        List<OrganizationMember> members;
        
        if (query.status() != null && !query.status().trim().isEmpty()) {
            MemberStatus status = MemberStatus.valueOf(query.status().toUpperCase());
            members = organizationMemberRepository.findAllByOrganizationIdAndStatus(query.orgId(), status);
        } else {
            members = organizationMemberRepository.findAllByOrganizationId(query.orgId());
        }
        
        List<MemberResult> mappedList = members.stream()
                .map(member -> new MemberResult(
                        member.getId(),
                        member.getOrganizationId(),
                        member.getUserId(),
                        member.getCustomRoleId(),
                        member.getStatus().name(),
                        member.getJoinedAt(),
                        member.getInvitedBy(),
                        member.getUpdatedAt()
                ))
                .collect(Collectors.toList());
        
        return mappedList;
    }
}
