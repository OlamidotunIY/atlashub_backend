package com.atlashub.identity.application.usecase;

import com.atlashub.identity.application.command.DeclineInvitationCommand;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Invitation;
import com.atlashub.identity.domain.repository.InvitationRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeclineInvitationUseCase extends BaseUseCase<DeclineInvitationCommand, Void> {

    private final InvitationRepository invitationRepository;
    private final DomainEventPublisher eventPublisher;

    public DeclineInvitationUseCase(InvitationRepository invitationRepository, DomainEventPublisher eventPublisher) {
        this.invitationRepository = invitationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Void execute(DeclineInvitationCommand command) {
        Invitation invitation = invitationRepository.findByToken(command.token())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.INVITATION_NOT_FOUND, "Invitation not found"));

        invitation.decline();
        invitationRepository.save(invitation);

        publishEvents(invitation, eventPublisher);

        return null;
    }
}
