package com.atlashub.iam.application.commands.InitializeOrganizationIam;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.entities.Permission;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class InitializeOrganizationIamHandler extends Command<InitializeOrganizationIamCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(InitializeOrganizationIamHandler.class);

    private final OrganizationMemberRepository organizationMemberRepository;
    private final PermissionRepository permissionRepository;
    private final CustomRoleRepository roleRepository;

    public InitializeOrganizationIamHandler(OrganizationMemberRepository organizationMemberRepository, PermissionRepository permissionRepository, CustomRoleRepository roleRepository) {
        this.organizationMemberRepository = organizationMemberRepository;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public Void execute(InitializeOrganizationIamCommand command) {
        log.info("Executing InitializeOrganizationIamCommand");

        Long memberId = organizationMemberRepository.nextIdentity();
        Long roleId = roleRepository.nextIdentity();

        Set<Long> permissions = permissionRepository.findAll().stream().map(Permission::getId).collect(Collectors.toSet());
        CustomRole role = CustomRole.create(roleId, command.orgId(), "Owner", "Default built-in role granting full administrative access to all organization resources.", permissions, true, null);

        OrganizationMember member = OrganizationMember.create(memberId, command.orgId(), command.foundingUserId(), role.getId(), null);

        organizationMemberRepository.save(member);
        roleRepository.save(role);
        return null;
    }
}
