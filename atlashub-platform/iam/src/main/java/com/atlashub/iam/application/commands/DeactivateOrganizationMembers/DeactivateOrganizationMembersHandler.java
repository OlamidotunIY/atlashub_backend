package com.atlashub.iam.application.commands.DeactivateOrganizationMembers;

import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeactivateOrganizationMembersHandler extends Command<DeactivateOrganizationMembersCommand, Integer> {
    private final OrganizationMemberRepository members;

    public DeactivateOrganizationMembersHandler(OrganizationMemberRepository members) {
        this.members = members;
    }

    @Override
    @Transactional
    public Integer execute(DeactivateOrganizationMembersCommand command) {
        var organizationMembers = members.findAllByOrganizationId(command.organizationId());
        organizationMembers.forEach(member -> {
            member.deactivateForOrganizationBan();
            members.save(member);
        });
        return organizationMembers.size();
    }
}
