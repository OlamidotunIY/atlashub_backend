package com.atlashub.authentication.application.query.GetUserInfo;

import com.atlashub.shared.application.port.UserQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class GetUserInfoHandler extends Query<GetUserInfoQuery, GetUserInfoResult> {

    private final UserQueryPort userQueryPort;

    public GetUserInfoHandler(UserQueryPort userQueryPort) {
        this.userQueryPort = userQueryPort;
    }

    @Override
    public GetUserInfoResult execute(GetUserInfoQuery query) {
        if (query.userId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        UserQueryPort.UserDto user = userQueryPort.findById(query.userId())
                .orElseThrow(() -> new NotFoundException("User not found: " + query.userId()));

        String fullName = ((user.firstName() == null ? "" : user.firstName()) + " "
                + (user.lastName() == null ? "" : user.lastName())).trim();

        String environment = user.activeEnvironment() != null
                ? user.activeEnvironment().name()
                : ApiEnvironment.TEST.name();

        return new GetUserInfoResult(
                user.id().toString(),
                user.email(),
                user.emailVerified(),
                fullName,
                user.firstName(),
                user.lastName(),
                user.activeOrganizationId(),
                environment
        );
    }
}
