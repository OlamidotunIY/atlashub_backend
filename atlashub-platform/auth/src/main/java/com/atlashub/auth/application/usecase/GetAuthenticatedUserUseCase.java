package com.atlashub.auth.application.usecase;

import com.atlashub.auth.application.query.GetAuthenticatedUserQuery;
import com.atlashub.auth.application.result.AuthenticatedUserDto;
import com.atlashub.auth.domain.model.AuthAccount;
import com.atlashub.auth.domain.repository.AuthAccountRepository;
import com.atlashub.auth.domain.valueobject.PrincipalType;
import com.atlashub.shared.api.UserQueryApi;
import com.atlashub.shared.api.UserQueryApi.UserSharedDto;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;

import com.atlashub.auth.domain.exception.AuthErrorCode;

@Service
public class GetAuthenticatedUserUseCase extends BaseUseCase<GetAuthenticatedUserQuery, AuthenticatedUserDto> {

    private final AuthAccountRepository authAccountRepository;
    private final UserQueryApi userQueryApi;

    public GetAuthenticatedUserUseCase(AuthAccountRepository authAccountRepository, UserQueryApi userQueryApi) {
        this.authAccountRepository = authAccountRepository;
        this.userQueryApi = userQueryApi;
    }

    @Override
    public AuthenticatedUserDto execute(GetAuthenticatedUserQuery query) {
        AuthAccount authAccount = authAccountRepository.findByPrincipalIdAndType(query.userId(), PrincipalType.USER)
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "AuthAccount not found for principalId: " + query.userId()));

        UserSharedDto userProfile = userQueryApi.getUserById(query.userId())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "User profile not found for ID: " + query.userId()));

        return AuthenticatedUserDto.from(authAccount, userProfile);
    }
}
