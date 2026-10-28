package com.atlashub.accounts.application.command.RegisterUser;

import com.atlashub.accounts.domain.entities.User;
import com.atlashub.accounts.domain.exceptions.EmailAlreadyExistsException;
import com.atlashub.accounts.domain.repositories.UserRepository;
import com.atlashub.shared.application.port.OneTimeSecretStore;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Component
public class RegisterUserHandler extends Command<RegisterUserCommand, Long> {
    private final UserRepository users;
    private final OneTimeSecretStore secrets;

    public RegisterUserHandler(UserRepository users, OneTimeSecretStore secrets) {
        this.users = users;
        this.secrets = secrets;
    }

    @Override
    @Transactional
    public Long execute(RegisterUserCommand command) {
        String email = new EmailAddress(command.email()).value();
        if (users.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("User with email already exists");
        }
        String credentialReference = secrets.store(command.password(), Duration.ofHours(24));
        User user = User.create(users.nextIdentity(), command.firstName(), command.lastName(),
                new EmailAddress(email), command.country(), false, command.password(), credentialReference);
        users.save(user);
        return user.getId();
    }
}
