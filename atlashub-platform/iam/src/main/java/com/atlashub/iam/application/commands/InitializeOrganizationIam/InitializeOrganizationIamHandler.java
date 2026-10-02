package com.atlashub.iam.application.commands.InitializeOrganizationIam;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.shared.application.usecase.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InitializeOrganizationIamHandler extends Command<InitializeOrganizationIamCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(InitializeOrganizationIamHandler.class);

    private final OrganizationMemberRepository organizationMemberRepository;
    private final CustomRoleRepository roleRepository;

    public InitializeOrganizationIamHandler(OrganizationMemberRepository organizationMemberRepository,
                                            CustomRoleRepository roleRepository) {
        this.organizationMemberRepository = organizationMemberRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public Void execute(InitializeOrganizationIamCommand command) {
        log.info("Executing InitializeOrganizationIamCommand");

        Long memberId = organizationMemberRepository.nextIdentity();
        Long roleId = roleRepository.nextIdentity();

        CustomRole role = CustomRole.create(roleId, command.orgId(), "Owner",
                "Default built-in role resolved dynamically to all active platform permissions.",
                Set.of(), true, command.foundingUserId());

        OrganizationMember member = OrganizationMember.create(memberId, command.orgId(), command.foundingUserId(), role.getId(), null);

        roleRepository.save(role);
        organizationMemberRepository.save(member);
        return null;
    }
}
