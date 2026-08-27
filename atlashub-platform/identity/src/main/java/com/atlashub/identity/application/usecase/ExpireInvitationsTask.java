package com.atlashub.identity.application.usecase;

import com.atlashub.identity.domain.model.Invitation;
import com.atlashub.identity.domain.repository.InvitationRepository;
import com.atlashub.shared.event.DomainEvent;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.event.EnvelopedDomainEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ExpireInvitationsTask {

    private final InvitationRepository invitationRepository;
    private final DomainEventPublisher eventPublisher;

    public ExpireInvitationsTask(InvitationRepository invitationRepository, DomainEventPublisher eventPublisher) {
        this.invitationRepository = invitationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void execute() {
        List<Invitation> expiredInvitations = invitationRepository.findExpiredPendingInvitations();
        for (Invitation invitation : expiredInvitations) {
            invitation.expire();
            invitationRepository.save(invitation);
            invitation.pullDomainEvents().forEach(event -> 
                eventPublisher.publish(EnvelopedDomainEvent.wrap((DomainEvent) event))
            );
        }
    }
}

