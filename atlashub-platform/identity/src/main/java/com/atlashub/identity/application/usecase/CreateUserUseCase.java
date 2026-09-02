package com.atlashub.identity.application.usecase;

import com.atlashub.identity.application.command.AcceptInvitationCommand;
import com.atlashub.identity.application.command.CreateUserCommand;
import com.atlashub.identity.application.result.CreateUserResult;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Invitation;
import com.atlashub.identity.domain.model.User;
import com.atlashub.identity.domain.repository.InvitationRepository;
import com.atlashub.identity.domain.repository.UserRepository;
import com.atlashub.identity.domain.valueobject.InvitationStatus;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.exception.ConflictException;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUserUseCase extends BaseUseCase<CreateUserCommand, CreateUserResult> {

    private static final Logger log = LoggerFactory.getLogger(CreateUserUseCase.class);

    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;
    private final InvitationRepository invitationRepository;
    private final AcceptInvitationUseCase acceptInvitationUseCase;

    public CreateUserUseCase(UserRepository userRepository, 
                             DomainEventPublisher eventPublisher,
                             InvitationRepository invitationRepository,
                             AcceptInvitationUseCase acceptInvitationUseCase) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.invitationRepository = invitationRepository;
        this.acceptInvitationUseCase = acceptInvitationUseCase;
    }

    @Override
    @Transactional
    public CreateUserResult execute(CreateUserCommand command) {
        log.info("Registering new user with email: {}", command.email());

        if (userRepository.findByEmail(command.email()).isPresent()) {
            log.warn("Registration failed: email {} already exists", command.email());
            throw new ConflictException(IdentityErrorCode.EMAIL_ALREADY_EXISTS, "A user with this email already exists");
        }

        boolean isInvited = false;
        if (command.inviteToken() != null && !command.inviteToken().isBlank()) {
            Invitation invitation = invitationRepository.findByToken(command.inviteToken())
                    .orElseThrow(() -> new NotFoundException(IdentityErrorCode.INVITATION_NOT_FOUND, "Invitation not found"));

            if (invitation.getStatus() != InvitationStatus.PENDING) {
                throw new BusinessRuleException(IdentityErrorCode.INVITATION_NOT_FOUND, "Invitation is no longer pending");
            }
            if (!invitation.getInvitedEmail().equalsIgnoreCase(command.email())) {
                throw new BusinessRuleException(IdentityErrorCode.INVITATION_NOT_FOUND, "Invitation email does not match registration email");
            }
            isInvited = true;
        }

        User user = new User(
            userRepository.nextIdentity(),
            command.firstName(),
            command.lastName(),
            new EmailAddress(command.email()),
            command.phone() != null ? new PhoneNumber(command.phone()) : null,
            Country.fromString(command.country()),
            isInvited
        );

        userRepository.save(user);
        log.debug("User saved with id: {}", user.getId());

        publishEvents(user, eventPublisher);
        log.info("Successfully registered user id: {}", user.getId());

        if (isInvited) {
            acceptInvitationUseCase.execute(new AcceptInvitationCommand(command.inviteToken(), user.getId()));
            log.info("Successfully accepted invitation for user id: {}", user.getId());
        }

        return new CreateUserResult(user.getId());
    }
}
