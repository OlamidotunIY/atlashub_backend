package com.atlashub.auth.application.usecase;

import com.atlashub.auth.application.command.RegisterViaInvitationCommand;
import com.atlashub.auth.application.port.in.PasswordEncoderPort;
import com.atlashub.auth.application.port.out.InvitationQueryPort;
import com.atlashub.auth.domain.exception.AuthErrorCode;
import com.atlashub.auth.domain.model.AuthAccount;
import com.atlashub.auth.domain.repository.AuthAccountRepository;
import com.atlashub.auth.domain.valueobject.AuthStatus;
import com.atlashub.auth.domain.valueobject.PrincipalType;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.exception.ConflictException;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atlashub.auth.domain.valueobject.AuthProvider;

@Service
public class RegisterViaInvitationUseCase extends BaseUseCase<RegisterViaInvitationCommand, Void> {

    private final AuthAccountRepository authAccountRepository;
    private final InvitationQueryPort invitationQueryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final DomainEventPublisher eventPublisher;

    public RegisterViaInvitationUseCase(
            AuthAccountRepository authAccountRepository,
            InvitationQueryPort invitationQueryPort,
            PasswordEncoderPort passwordEncoderPort,
            DomainEventPublisher eventPublisher) {
        this.authAccountRepository = authAccountRepository;
        this.invitationQueryPort = invitationQueryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Void execute(RegisterViaInvitationCommand command) {
        InvitationQueryPort.InvitationDetails invite = invitationQueryPort.findByToken(command.token())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.INVALID_CREDENTIAL, "Invalid or expired invitation token"));

        if (!"PENDING".equals(invite.status())) {
            throw new ConflictException(AuthErrorCode.INVALID_CREDENTIAL, "Invitation is no longer pending");
        }

        // Ideally here we also publish an event to tell Identity to create the User and member.
        // For now, we create the AuthAccount as ACTIVE because the email is verified via invite.
        String hash = passwordEncoderPort.encode(command.password());

        // A real system would generate an ID or get it from identity. We simulate user ID generation.
        Long generatedUserId = authAccountRepository.nextIdentity();

        AuthAccount account = AuthAccount.createWithStatus(
                generatedUserId,
                generatedUserId,
                PrincipalType.USER,
                invite.invitedEmail(),
                null,
                AuthProvider.EMAIL,
                hash,
                "USER",
                AuthStatus.ACTIVE
        );

        authAccountRepository.save(account);
        publishEvents(account, eventPublisher);

        return null;
    }
}
