package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.CreateUserCommand;

import com.atlashub.shared.usecase.BaseUseCase;

import com.atlashub.identity.application.dto.CreateUserResult;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.User;
import com.atlashub.identity.domain.repository.UserRepository;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.ConflictException;

@Service
public class CreateUserUseCase extends BaseUseCase<CreateUserCommand, CreateUserResult> {
    private static final Logger log = LoggerFactory.getLogger(CreateUserUseCase.class);


    private final UserRepository UserRepository;
    private final DomainEventPublisher eventPublisher;

    public CreateUserUseCase(UserRepository UserRepository, DomainEventPublisher eventPublisher) {
        this.UserRepository = UserRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public CreateUserResult execute(CreateUserCommand command) {
        log.info("Executing CreateUserUseCase");

        if (UserRepository.findByOrganizationIdAndEmail(command.OrganizationId(), command.email()).isPresent()) {
            throw new ConflictException(IdentityErrorCode.User_EMAIL_ALREADY_EXISTS, "User with this email already exists for this Organization");
        }

        User User = new User(UserRepository.nextIdentity(),
                "CUS_" + java.util.UUID.randomUUID().toString(),
                command.OrganizationId(),
            command.firstName(),
            command.lastName(),
            new EmailAddress(command.email()),
            new PhoneNumber(command.phone()),
            command.metadata()
        );

        UserRepository.save(User);

        publishEvents(User, eventPublisher);

        return new CreateUserResult(User.getId());
    }
}



