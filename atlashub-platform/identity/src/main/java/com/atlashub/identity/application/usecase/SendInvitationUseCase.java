package com.atlashub.identity.application.usecase;

import com.atlashub.identity.application.command.SendInvitationCommand;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Invitation;
import com.atlashub.identity.domain.repository.InvitationRepository;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class SendInvitationUseCase extends BaseUseCase<SendInvitationCommand, Void> {

    private final InvitationRepository invitationRepository;
    private final DomainEventPublisher eventPublisher;

    public SendInvitationUseCase(InvitationRepository invitationRepository, DomainEventPublisher eventPublisher) {
        this.invitationRepository = invitationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Void execute(SendInvitationCommand command) {
        // Simple token generation (UUID)
        String token = UUID.randomUUID().toString().replace("-", "");
        
        Invitation invitation = new Invitation(
            command.organizationId(),
            command.email(),
            command.inviterId(),
            command.role(),
            token,
            ZonedDateTime.now().plusDays(7)
        );

        invitationRepository.save(invitation);
        publishEvents(invitation, eventPublisher);

        return null;
    }
}
