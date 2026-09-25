package com.atlashub.iam.application.commands.AcceptInvitation;

import com.atlashub.iam.domain.entities.Invitation;
import com.atlashub.iam.domain.repositories.InvitationRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AcceptInvitationHandler extends Command<AcceptInvitationCommand, Invitation> {

    private static final Logger log = LoggerFactory.getLogger(AcceptInvitationHandler.class);
    
    private final InvitationRepository invitationRepository;

    public AcceptInvitationHandler(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Override
    public Invitation execute(AcceptInvitationCommand command) {
        log.info("Executing AcceptInvitationCommand for token: {}", command.token());
        
        Invitation invitation = invitationRepository.findByToken(command.token())
            .orElseThrow(() -> new IllegalArgumentException("Invitation not found for token: " + command.token()));
            
        invitation.accept(command.acceptingUserId());
        invitationRepository.save(invitation);

        return invitation;
    }
}
