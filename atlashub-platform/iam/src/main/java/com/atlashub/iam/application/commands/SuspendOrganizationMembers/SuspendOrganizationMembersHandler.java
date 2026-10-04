package com.atlashub.iam.application.commands.SuspendOrganizationMembers;

import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.iam.domain.valueobject.MemberStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SuspendOrganizationMembersHandler extends Command<SuspendOrganizationMembersCommand, Integer> {
    private final OrganizationMemberRepository members;
    private final CustomRoleRepository roles;

    public SuspendOrganizationMembersHandler(OrganizationMemberRepository members, CustomRoleRepository roles) {
        this.members = members;
        this.roles = roles;
    }

    @Override
    @Transactional
    public Integer execute(SuspendOrganizationMembersCommand command) {
        var candidates = members.findAllByOrganizationIdAndStatus(command.organizationId(), MemberStatus.ACTIVE);
        int suspended = 0;
        for (var member : candidates) {
            boolean owner = roles.findById(member.getCustomRoleId())
                    .map(role -> role.isBuiltIn() && "Owner".equalsIgnoreCase(role.getName()))
                    .orElse(false);
            if (!owner) {
                member.suspend(command.reason());
                members.save(member);
                suspended++;
            }
        }
        return suspended;
    }
}
