package com.atlashub.authentication.application.query.GetActiveSessions;

import com.atlashub.authentication.application.port.SessionPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.atlashub.authentication.domain.valueobject.Session;

@Component
public class GetActiveSessionsHandler extends Query<GetActiveSessionsQuery, List<SessionResult>> {

    private final SessionPort sessionPort;
    private final AuthAccountRepository accountRepository;

    public GetActiveSessionsHandler(SessionPort sessionPort, AuthAccountRepository accountRepository) {
        this.sessionPort = sessionPort;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<SessionResult> execute(GetActiveSessionsQuery query) {
        AuthAccount account = accountRepository.findByUserId(query.userId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        Set<Session> sessions = sessionPort.findAllByAuthAccountId(account.getId());

        return sessions.stream()
                .map(s -> new SessionResult(
                        s.refreshTokenHash(),
                        s.accessTokenExpiresAt(),
                        s.refreshTokenExpiresAt(),
                        s.deviceId(),
                        s.orgId()
                ))
                .collect(Collectors.toList());
    }
}
