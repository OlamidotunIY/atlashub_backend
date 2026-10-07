package com.atlashub.iam.application.commands.DeactivateMember;

import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.exception.OrganizationMemberNotFoundException;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.iam.domain.valueobject.MemberStatus;
import com.atlashub.shared.application.usecase.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeactivateMemberHandler extends Command<DeactivateMemberCommand, OrganizationMember> {

    private static final Logger log = LoggerFactory.getLogger(DeactivateMemberHandler.class);

    private final OrganizationMemberRepository organizationMemberRepository;

    private final CustomRoleRepository roleRepository;

    public DeactivateMemberHandler(OrganizationMemberRepository organizationMemberRepository,
                                   CustomRoleRepository roleRepository) {
        this.organizationMemberRepository = organizationMemberRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('iam:members:manage')")
    public OrganizationMember execute(DeactivateMemberCommand command) {
        log.info("Executing DeactivateMemberCommand");

        OrganizationMember member = organizationMemberRepository.findByIdAndOrganizationIdForUpdate(command.memberId(),
                command.organizationId()).orElseThrow(OrganizationMemberNotFoundException::new);

        boolean ownerRole = roleRepository.findById(member.getCustomRoleId())
                .map(role -> role.isBuiltIn() && "Owner".equalsIgnoreCase(role.getName())).orElse(false);
        boolean lastOwner = ownerRole &&
                organizationMemberRepository.countByOrganizationIdAndCustomRoleIdAndStatus(member.getOrganizationId(),
                        member.getCustomRoleId(), MemberStatus.ACTIVE) <= 1;
        member.deactivate(lastOwner);
        organizationMemberRepository.save(member);

        return member;
    }
}
