package com.atlashub.iam.application.commands.AssignRole;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public OrganizationMember execute(AssignRoleCommand command) {
        log.info("Executing AssignRoleCommand");
        
        OrganizationMember member = organizationMemberRepository.findById(command.memberId())
                .orElseThrow(() -> new IllegalArgumentException("OrganizationMember not found"));

        CustomRole role = customRoleRepository.findById(command.newRoleId())
                .orElseThrow(() -> new IllegalArgumentException("CustomRole not found"));

        member.assignRole(role.getId());
        organizationMemberRepository.save(member);

        return member;
    }
}
