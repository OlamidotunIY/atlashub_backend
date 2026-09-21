package com.atlashub.authentication.application.command.Logout;

import com.atlashub.authentication.application.port.SessionPort;
import com.atlashub.authentication.application.port.TokenRevocationPort;
import com.atlashub.shared.application.service.HashingUtils;
import com.atlashub.shared.application.usecase.Command;

import java.time.Duration;
import java.time.ZonedDateTime;

public class LogoutHandler extends Command<LogoutCommand, Void> {

    final SessionPort sessionPort;
    final TokenRevocationPort revocationPort;

    public LogoutHandler(SessionPort sessionPort, TokenRevocationPort revocationPort) {
        this.sessionPort = sessionPort;
        this.revocationPort = revocationPort;
    }

    @Override
    public Void execute(LogoutCommand input) {
        String hash = HashingUtils.sha256Hex(input.refreshToken());

        sessionPort.delete(hash);

        Duration remainingTime = Duration.between(ZonedDateTime.now(), input.accessTokenExpiresAt());
        revocationPort.revokeAccessToken(input.accessTokenJti(), remainingTime);

        return null;
    }
}
