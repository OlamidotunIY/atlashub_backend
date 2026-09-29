package com.atlashub.iam.application.queries.ListInvitations;

import com.atlashub.shared.application.usecase.Query;
import com.atlashub.iam.domain.repositories.InvitationRepository;
import com.atlashub.iam.domain.valueobject.InvitationStatus;
import com.atlashub.iam.domain.entities.Invitation;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListInvitationsHandler extends Query<ListInvitationsQuery, List<InvitationResult>> {

    private static final Logger log = LoggerFactory.getLogger(ListInvitationsHandler.class);
    
    private final InvitationRepository invitationRepository;

    public ListInvitationsHandler(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Override
    public List<InvitationResult> execute(ListInvitationsQuery query) {
        log.info("Executing ListInvitationsQuery for orgId: {}", query.orgId());
        
        InvitationStatus status = query.status() != null ? InvitationStatus.valueOf(query.status().toUpperCase()) : null;
        
        List<Invitation> invitations = invitationRepository.findByOrganizationIdAndStatus(query.orgId(), status);
        
        List<InvitationResult> results = invitations.stream()
            .map(invitation -> new InvitationResult(
                invitation.getId(),
                invitation.getOrganizationId(),
                invitation.getInvitedEmail().value(),
                invitation.getStatus().name(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt()
            ))
            .collect(Collectors.toList());
            
        return results;
    }
}
