package com.atlashub.accounts.application.command.ApplyAuthenticationEvent;

import com.atlashub.accounts.domain.repositories.UserRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApplyAuthenticationEventHandler extends Command<ApplyAuthenticationEventCommand, Void> {
    private final UserRepository repository;

    public ApplyAuthenticationEventHandler(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(ApplyAuthenticationEventCommand command) {
        repository.findById(command.userId()).ifPresent(user -> {
            if (command.emailVerified()) user.markEmailVerified();
            if (command.activeOrganizationId() != null) user.switchActiveOrganization(command.activeOrganizationId());
            if (command.activeEnvironment() != null) user.switchActiveEnvironment(command.activeEnvironment());
            repository.save(user);
        });
        return null;
    }
}
