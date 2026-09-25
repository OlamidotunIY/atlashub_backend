package com.atlashub.authentication.application.command.LogoutAllDevices;

import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class LogoutAllDevicesHandler extends Command<LogoutAllDevicesCommand, Void> {

    private final SessionRepository sessionRepository;
    private final AuthAccountRepository accountRepository;

    public LogoutAllDevicesHandler(SessionRepository sessionRepository,
                                   AuthAccountRepository accountRepository) {
        this.sessionRepository = sessionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Void execute(LogoutAllDevicesCommand command) {
        AuthAccount account = accountRepository.findByUserId(command.userId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        sessionRepository.deleteAllByUserId(account.getUserId().toString());

        return null;
    }
}
