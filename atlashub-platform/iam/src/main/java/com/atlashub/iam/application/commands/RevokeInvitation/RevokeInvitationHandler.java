package com.atlashub.iam.application.commands.RevokeInvitation;

import com.atlashub.iam.domain.entities.Invitation;
import com.atlashub.iam.domain.repositories.InvitationRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RevokeInvitationHandler extends Command<RevokeInvitationCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(RevokeInvitationHandler.class);
    private final InvitationRepository invitationRepository;

    public RevokeInvitationHandler(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Override
    public Void execute(RevokeInvitationCommand command) {
        log.info("Executing RevokeInvitationCommand");
        
        Invitation invitation = invitationRepository.findById(command.invitationId())
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        invitation.revoke(command.revokedByUserId());
        invitationRepository.save(invitation);

        return null;
    }
}
