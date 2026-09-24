package com.atlashub.authentication.application.query.GetActiveSessions;

import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GetActiveSessionsHandler extends Query<GetActiveSessionsQuery, List<SessionResult>> {

    private final SessionRepository sessionRepository;
    private final AuthAccountRepository accountRepository;

    public GetActiveSessionsHandler(SessionRepository sessionRepository,
                                    AuthAccountRepository accountRepository) {
        this.sessionRepository = sessionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<SessionResult> execute(GetActiveSessionsQuery query) {
        AuthAccount account = accountRepository.findByUserId(query.userId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        Set<Session> sessions = sessionRepository.findAllByUserId(account.getUserId().toString());

        return sessions.stream()
                .map(s -> new SessionResult(
                        s.getToken(),
                        s.getExpiresAt(),
                        s.getIpAddress(),
                        s.getUserAgent()
                ))
                .collect(Collectors.toList());
    }
}
