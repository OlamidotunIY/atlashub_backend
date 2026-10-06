package com.atlashub.iam.application.commands.DeactivateMemberByUser;

import com.atlashub.iam.domain.exception.OrganizationMemberNotFoundException;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.iam.domain.valueobject.MemberStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeactivateMemberByUserHandler extends Command<DeactivateMemberByUserCommand, Void> {
    private final OrganizationMemberRepository members;
    private final CustomRoleRepository roles;

    public DeactivateMemberByUserHandler(OrganizationMemberRepository members, CustomRoleRepository roles) {
        this.members = members;
        this.roles = roles;
    }

    @Override
    @Transactional
    public Void execute(DeactivateMemberByUserCommand command) {
        var member = members.findByOrganizationIdAndUserId(command.organizationId(), command.userId())
                .orElseThrow(OrganizationMemberNotFoundException::new);
        boolean owner = roles.findById(member.getCustomRoleId())
                .map(role -> role.isBuiltIn() && "Owner".equalsIgnoreCase(role.getName())).orElse(false);
        boolean lastOwner = owner && members.countByOrganizationIdAndCustomRoleIdAndStatus(
                command.organizationId(), member.getCustomRoleId(), MemberStatus.ACTIVE) <= 1;
        member.deactivate(lastOwner);
        members.save(member);
        return null;
    }
}
