package com.atlashub.identity.application.usecase;

import com.atlashub.identity.application.command.CreateUserCommand;
import com.atlashub.identity.application.result.CreateUserResult;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.User;
import com.atlashub.identity.domain.repository.UserRepository;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.ConflictException;
import com.atlashub.shared.usecase.BaseUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUserUseCase extends BaseUseCase<CreateUserCommand, CreateUserResult> {

    private static final Logger log = LoggerFactory.getLogger(CreateUserUseCase.class);

    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    public CreateUserUseCase(UserRepository userRepository, DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public CreateUserResult execute(CreateUserCommand command) {
        log.info("Registering new user with email: {}", command.email());

        if (userRepository.findByEmail(command.email()).isPresent()) {
            log.warn("Registration failed: email {} already exists", command.email());
            throw new ConflictException(IdentityErrorCode.EMAIL_ALREADY_EXISTS, "A user with this email already exists");
        }

        User user = new User(
            userRepository.nextIdentity(),
            command.firstName(),
            command.lastName(),
            new EmailAddress(command.email()),
            command.phone() != null ? new PhoneNumber(command.phone()) : null,
            Country.fromString(command.country())
        );

        userRepository.save(user);
        log.debug("User saved with id: {}", user.getId());

        publishEvents(user, eventPublisher);
        log.info("Successfully registered user id: {}", user.getId());

        return new CreateUserResult(user.getId());
    }
}
