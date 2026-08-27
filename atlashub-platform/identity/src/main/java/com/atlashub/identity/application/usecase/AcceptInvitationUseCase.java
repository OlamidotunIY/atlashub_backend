package com.atlashub.identity.application.usecase;

import com.atlashub.identity.application.command.AcceptInvitationCommand;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Invitation;
import com.atlashub.identity.domain.model.OrganizationMember;
import com.atlashub.identity.domain.repository.InvitationRepository;
import com.atlashub.identity.domain.repository.OrganizationMemberRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcceptInvitationUseCase extends BaseUseCase<AcceptInvitationCommand, Void> {

    private final InvitationRepository invitationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final DomainEventPublisher eventPublisher;

    public AcceptInvitationUseCase(
            InvitationRepository invitationRepository,
            OrganizationMemberRepository organizationMemberRepository,
            DomainEventPublisher eventPublisher) {
        this.invitationRepository = invitationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Void execute(AcceptInvitationCommand command) {
        Invitation invitation = invitationRepository.findByToken(command.token())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.INVITATION_NOT_FOUND, "Invitation not found"));

        invitation.accept(command.acceptingUserId());
        invitationRepository.save(invitation);

        OrganizationMember member = new OrganizationMember
                (
                        invitation.getOrganizationId(),
                        command.acceptingUserId(),
                        invitation.getRole()
                );
        organizationMemberRepository.save(member);

        publishEvents(invitation, eventPublisher);
        publishEvents(member, eventPublisher);

        return null;
    }
}
