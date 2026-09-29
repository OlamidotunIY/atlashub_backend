package com.atlashub.iam.application.commands.DeclineInvitation;

import com.atlashub.iam.domain.entities.Invitation;
import com.atlashub.iam.domain.repositories.InvitationRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DeclineInvitationHandler extends Command<DeclineInvitationCommand, Invitation> {

    private static final Logger log = LoggerFactory.getLogger(DeclineInvitationHandler.class);
    private final InvitationRepository invitationRepository;

    public DeclineInvitationHandler(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Override
    public Invitation execute(DeclineInvitationCommand command) {
        log.info("Executing DeclineInvitationCommand");
        
        Invitation invitation = invitationRepository.findByToken(command.token())
                .orElseThrow(() -> new NotFoundException("Invitation not found with given token"));

        invitation.decline();

        invitationRepository.save(invitation);

        return invitation;
    }
}
