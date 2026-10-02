package com.atlashub.iam.application.commands.AcceptInvitation;

import com.atlashub.iam.domain.entities.Invitation;
import com.atlashub.iam.domain.repositories.InvitationRepository;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.application.port.UserQueryPort;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AcceptInvitationHandler extends Command<AcceptInvitationCommand, Invitation> {

    private static final Logger log = LoggerFactory.getLogger(AcceptInvitationHandler.class);
    
    private final InvitationRepository invitationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final CustomRoleRepository roleRepository;
    private final UserQueryPort userQueryPort;

    public AcceptInvitationHandler(InvitationRepository invitationRepository,
                                   OrganizationMemberRepository memberRepository,
                                   CustomRoleRepository roleRepository,
                                   UserQueryPort userQueryPort) {
        this.invitationRepository = invitationRepository;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
        this.userQueryPort = userQueryPort;
    }

    @Override
    @Transactional
    public Invitation execute(AcceptInvitationCommand command) {
        log.info("Executing AcceptInvitationCommand for token: {}", command.token());
        
        Invitation invitation = invitationRepository.findByToken(command.token())
            .orElseThrow(() -> new IllegalArgumentException("Invitation not found for token: " + command.token()));
            
        var user = userQueryPort.findById(command.acceptingUserId())
                .orElseThrow(() -> new IllegalArgumentException("Accepting user does not exist"));
        if (!user.email().equalsIgnoreCase(invitation.getInvitedEmail().value())) {
            throw new IllegalArgumentException("Invitation does not belong to the accepting user");
        }
        if (memberRepository.findByOrganizationIdAndUserId(
                invitation.getOrganizationId(), command.acceptingUserId()).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this organization");
        }
        roleRepository.findById(invitation.getCustomRoleId())
                .filter(role -> role.getOrganizationId().equals(invitation.getOrganizationId()))
                .orElseThrow(() -> new IllegalArgumentException("Invitation role is invalid"));

        invitation.accept(command.acceptingUserId());
        OrganizationMember member = OrganizationMember.create(
                memberRepository.nextIdentity(), invitation.getOrganizationId(), command.acceptingUserId(),
                invitation.getCustomRoleId(), invitation.getInvitedByUserId());
        memberRepository.save(member);
        invitationRepository.save(invitation);

        return invitation;
    }
}
