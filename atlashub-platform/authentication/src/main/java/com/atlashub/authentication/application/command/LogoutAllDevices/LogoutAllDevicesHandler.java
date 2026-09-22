package com.atlashub.authentication.application.command.LogoutAllDevices;

import com.atlashub.authentication.application.port.SessionPort;
import com.atlashub.authentication.application.port.TokenRevocationPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.valueobject.Session;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Set;

@Component
public class LogoutAllDevicesHandler extends Command<LogoutAllDevicesCommand, Void> {

    private final SessionPort sessionPort;
    private final TokenRevocationPort revocationPort;
    private final AuthAccountRepository accountRepository;

    public LogoutAllDevicesHandler(SessionPort sessionPort,
                                   TokenRevocationPort revocationPort,
                                   AuthAccountRepository accountRepository) {
        this.sessionPort = sessionPort;
        this.revocationPort = revocationPort;
        this.accountRepository = accountRepository;
    }

    @Override
    public Void execute(LogoutAllDevicesCommand command) {
        AuthAccount account = accountRepository.findByUserId(command.userId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        Set<Session> activeSessions = sessionPort.findAllByAuthAccountId(account.getId());

        for (Session session : activeSessions) {
            // Revoke the access token JTI if still within its expiry window
            Duration remaining = Duration.between(ZonedDateTime.now(), session.accessTokenExpiresAt());
            if (!remaining.isNegative()) {
                revocationPort.revokeAccessToken(session.accessTokenJti(), remaining);
            }
        }

        sessionPort.deleteAllForUser(account.getId());

        return null;
    }
}
