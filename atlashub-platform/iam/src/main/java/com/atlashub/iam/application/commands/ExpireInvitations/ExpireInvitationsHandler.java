package com.atlashub.iam.application.commands.ExpireInvitations;

import com.atlashub.iam.domain.repositories.InvitationRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExpireInvitationsHandler extends Command<ExpireInvitationsCommand, Integer> {
    private final InvitationRepository invitationRepository;

    public ExpireInvitationsHandler(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Override
    @Transactional
    public Integer execute(ExpireInvitationsCommand command) {
        var invitations = invitationRepository.findPendingExpiredBefore(command.cutoff());
        invitations.forEach(invitation -> {
            invitation.expire();
            invitationRepository.save(invitation);
        });
        return invitations.size();
    }
}
