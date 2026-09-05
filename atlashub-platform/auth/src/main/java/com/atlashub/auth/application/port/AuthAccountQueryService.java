package com.atlashub.auth.application.port;

import com.atlashub.auth.application.result.AuthAccountDto;
import com.atlashub.auth.application.result.AuthenticatedUserDto;
import com.atlashub.auth.application.result.SessionDto;
import com.atlashub.shared.application.dto.ApiResponse;

import java.util.List;

public interface AuthAccountQueryService {
    ApiResponse<AuthAccountDto> getAuthAccount(Long authAccountId);
    AuthenticatedUserDto getAuthenticatedUser(Long userId);
    ApiResponse<List<SessionDto>> getSessions(Long authAccountId);
}
