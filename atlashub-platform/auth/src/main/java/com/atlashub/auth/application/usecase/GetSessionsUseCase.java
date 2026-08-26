package com.atlashub.auth.application.usecase;

import com.atlashub.auth.application.dto.SessionDto;
import com.atlashub.auth.application.query.GetSessionsQuery;
import com.atlashub.auth.domain.model.Session;
import com.atlashub.auth.domain.repository.SessionRepository;
import com.atlashub.shared.dto.ApiResponse;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetSessionsUseCase extends BaseUseCase<GetSessionsQuery, ApiResponse<List<SessionDto>>> {

    private final SessionRepository sessionRepository;

    public GetSessionsUseCase(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<SessionDto>> execute(GetSessionsQuery input) {
        List<Session> sessions = sessionRepository.findByAuthAccountId(input.authAccountId());
        
        List<SessionDto> dtos = sessions.stream()
                .map(s -> new SessionDto(
                        s.getId(),
                        s.getToken(),
                        s.getIpAddress(),
                        s.getUserAgent(),
                        s.getStatus(),
                        s.getCreatedAt(),
                        s.getExpiresAt(),
                        s.getRevokedAt()
                ))
                .toList();

        return new ApiResponse<>(true, "Sessions retrieved successfully", dtos, null);
    }
}
