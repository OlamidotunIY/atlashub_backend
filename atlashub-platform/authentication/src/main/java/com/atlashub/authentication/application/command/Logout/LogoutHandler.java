package com.atlashub.authentication.application.command.Logout;

import com.atlashub.authentication.application.port.TokenRevocationPort;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;

@Component
public class LogoutHandler extends Command<LogoutCommand, Void> {

    final SessionRepository sessionRepository;
    final TokenRevocationPort revocationPort;

    public LogoutHandler(SessionRepository sessionRepository, TokenRevocationPort revocationPort) {
        this.sessionRepository = sessionRepository;
        this.revocationPort = revocationPort;
    }

    @Override
    public Void execute(LogoutCommand input) {
        sessionRepository.deleteByToken(input.refreshToken());

        Duration remainingTime = Duration.between(ZonedDateTime.now(), input.accessTokenExpiresAt());
        if (!remainingTime.isNegative()) {
            revocationPort.revokeAccessToken(input.accessTokenJti(), remainingTime);
        }

        return null;
    }
}
