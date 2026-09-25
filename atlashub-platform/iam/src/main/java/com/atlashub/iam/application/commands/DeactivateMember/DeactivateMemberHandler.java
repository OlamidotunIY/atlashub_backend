package com.atlashub.iam.application.commands.DeactivateMember;

import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DeactivateMemberHandler extends Command<DeactivateMemberCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeactivateMemberHandler.class);
    private final OrganizationMemberRepository organizationMemberRepository;

    public DeactivateMemberHandler(OrganizationMemberRepository organizationMemberRepository) {
        this.organizationMemberRepository = organizationMemberRepository;
    }

    @Override
    public Void execute(DeactivateMemberCommand command) {
        log.info("Executing DeactivateMemberCommand");
        
        OrganizationMember member = organizationMemberRepository.findById(command.memberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
                
        member.deactivate(false);
        organizationMemberRepository.save(member);

        return null;
    }
}
