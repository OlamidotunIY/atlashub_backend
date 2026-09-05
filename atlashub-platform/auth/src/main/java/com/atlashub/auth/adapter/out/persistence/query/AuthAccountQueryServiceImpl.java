package com.atlashub.auth.adapter.out.persistence.query;

import com.atlashub.auth.adapter.out.persistence.repository.SpringDataAuthAccountRepository;
import com.atlashub.auth.adapter.out.persistence.repository.SpringDataSessionRepository;
import com.atlashub.auth.adapter.out.persistence.mapper.AuthAccountMapper;
import com.atlashub.auth.application.port.AuthAccountQueryService;
import com.atlashub.auth.application.result.AuthAccountDto;
import com.atlashub.auth.application.result.AuthenticatedUserDto;
import com.atlashub.auth.application.result.SessionDto;
import com.atlashub.auth.domain.exception.AuthErrorCode;
import com.atlashub.auth.domain.valueobject.PrincipalType;
import com.atlashub.auth.domain.model.AuthAccount;
import com.atlashub.identity.application.port.UserQueryService;
import com.atlashub.identity.application.port.OrganizationMemberQueryService;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthAccountQueryServiceImpl implements AuthAccountQueryService {

    private final SpringDataAuthAccountRepository authAccountRepository;
    private final SpringDataSessionRepository sessionRepository;
    private final AuthAccountMapper authAccountMapper;
    
    private final UserQueryService userQueryService;
    private final OrganizationMemberQueryService organizationMemberQueryService;

    @Override
    public ApiResponse<AuthAccountDto> getAuthAccount(Long authAccountId) {
        return authAccountRepository.findById(authAccountId)
                .map(account -> new AuthAccountDto(
                        account.getId(),
                        account.getPrincipalId(),
                        account.getPrincipalType(),
                        account.getProvider(),
                        account.getScope(),
                        account.isTotpEnabled(),
                        account.getStatus()
                ))
                .map(dto -> new ApiResponse<>(true, "Auth account retrieved successfully", dto, null))
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));
    }

    @Override
    public AuthenticatedUserDto getAuthenticatedUser(Long userId) {
        var authAccountEntity = authAccountRepository.findByPrincipalIdAndPrincipalType(userId, PrincipalType.USER)
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "AuthAccount not found for principalId: " + userId));

        var userProfile = userQueryService.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "User profile not found for ID: " + userId));

        String onboardingStatus = organizationMemberQueryService.getOnboardingStatus(authAccountEntity.getPrincipalId(), authAccountEntity.getIdentifier());

        AuthAccount domainAccount = authAccountMapper.toDomain(authAccountEntity);

        return AuthenticatedUserDto.from(domainAccount, userProfile, onboardingStatus);
    }

    @Override
    public ApiResponse<List<SessionDto>> getSessions(Long authAccountId) {
        List<SessionDto> dtos = sessionRepository.findByAuthAccountId(authAccountId).stream()
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
                .collect(Collectors.toList());

        return new ApiResponse<>(true, "Sessions retrieved successfully", dtos, null);
    }
}
