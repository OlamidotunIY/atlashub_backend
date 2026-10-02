package com.atlashub.iam.application.commands.DeleteCustomRole;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DeleteCustomRoleHandler extends Command<DeleteCustomRoleCommand, CustomRole> {

    private static final Logger log = LoggerFactory.getLogger(DeleteCustomRoleHandler.class);
    private final CustomRoleRepository customRoleRepository;
    private final com.atlashub.iam.domain.repositories.OrganizationMemberRepository memberRepository;

    public DeleteCustomRoleHandler(CustomRoleRepository customRoleRepository,
            com.atlashub.iam.domain.repositories.OrganizationMemberRepository memberRepository) {
        this.customRoleRepository = customRoleRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public CustomRole execute(DeleteCustomRoleCommand command) {
        log.info("Executing DeleteCustomRoleCommand");
        
        CustomRole role = customRoleRepository.findById(command.roleId())
            .filter(found -> found.getOrganizationId().equals(command.organizationId()))
            .orElseThrow(() -> new IllegalArgumentException("CustomRole not found: " + command.roleId()));
            
        boolean inUse = memberRepository.countByOrganizationIdAndCustomRoleIdAndStatus(
                command.organizationId(), command.roleId(), com.atlashub.iam.domain.valueobject.MemberStatus.ACTIVE) > 0;
        role.delete(inUse);
        customRoleRepository.deleteById(command.roleId());

        return role;
    }
}
