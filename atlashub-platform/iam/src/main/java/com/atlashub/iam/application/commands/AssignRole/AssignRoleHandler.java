package com.atlashub.iam.application.commands.AssignRole;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlashub.iam.domain.exception.CustomRoleNotFoundException;
import com.atlashub.iam.domain.exception.OrganizationMemberNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AssignRoleHandler extends Command<AssignRoleCommand, OrganizationMember> {

    private static final Logger log = LoggerFactory.getLogger(AssignRoleHandler.class);

    private final OrganizationMemberRepository organizationMemberRepository;
    private final CustomRoleRepository customRoleRepository;

    public AssignRoleHandler(OrganizationMemberRepository organizationMemberRepository, CustomRoleRepository customRoleRepository) {
        this.organizationMemberRepository = organizationMemberRepository;
        this.customRoleRepository = customRoleRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('iam:members:manage')")
    public OrganizationMember execute(AssignRoleCommand command) {
        log.info("Executing AssignRoleCommand");
        
        OrganizationMember member = organizationMemberRepository.findByIdAndOrganizationIdForUpdate(
                        command.memberId(), command.organizationId())
                .orElseThrow(OrganizationMemberNotFoundException::new);

        CustomRole role = customRoleRepository.findById(command.newRoleId())
                .filter(found -> found.getOrganizationId().equals(command.organizationId()))
                .orElseThrow(CustomRoleNotFoundException::new);

        member.assignRole(role.getId());
        organizationMemberRepository.save(member);

        return member;
    }
}
