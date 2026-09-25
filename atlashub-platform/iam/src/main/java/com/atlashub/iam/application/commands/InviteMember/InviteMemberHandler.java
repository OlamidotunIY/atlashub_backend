package com.atlashub.iam.application.commands.InviteMember;

import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.iam.domain.repositories.InvitationRepository;
import com.atlashub.shared.application.port.OrganizationQueryPort;
import com.atlashub.shared.application.port.UserQueryPort;
import com.atlashub.iam.domain.entities.Invitation;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import java.util.UUID;

@Component
public class InviteMemberHandler extends Command<InviteMemberCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(InviteMemberHandler.class);

    private final InvitationRepository invitationRepository;
    private final OrganizationQueryPort organizationQueryPort;
    private final UserQueryPort userQueryPort;

    public InviteMemberHandler(
            InvitationRepository invitationRepository,
            OrganizationQueryPort organizationQueryPort,
            UserQueryPort userQueryPort) {
        this.invitationRepository = invitationRepository;
        this.organizationQueryPort = organizationQueryPort;
        this.userQueryPort = userQueryPort;
    }

    @Override
    public Void execute(InviteMemberCommand command) {
        log.info("Executing InviteMemberCommand");
        
        if (!organizationQueryPort.existsById(command.orgId())) {
            throw new IllegalArgumentException("Organization does not exist");
        }
        
        if (!userQueryPort.existsById(command.invitedByUserId())) {
            throw new IllegalArgumentException("Invited by user does not exist");
        }

        EmailAddress emailAddress = new EmailAddress(command.email());
        String token = UUID.randomUUID().toString();

        Invitation invitation = Invitation.create(
                invitationRepository.nextIdentity(),
                command.orgId(),
                emailAddress,
                command.invitedByUserId(),
                command.customRoleId(),
                token
        );

        invitationRepository.save(invitation);

        return null;
    }
}
