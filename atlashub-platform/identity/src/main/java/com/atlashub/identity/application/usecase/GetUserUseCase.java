package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.dto.UserDto;
import com.atlashub.identity.application.port.UserQueryService;
import com.atlashub.identity.application.query.GetUserQuery;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.usecase.BaseUseCase;

@Service
public class GetUserUseCase extends BaseUseCase<GetUserQuery, UserDto> {
    private static final Logger log = LoggerFactory.getLogger(GetUserUseCase.class);


    private final UserQueryService queryService;

    public GetUserUseCase(UserQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public UserDto execute(GetUserQuery query) {
        log.info("Executing GetUserUseCase");

        return queryService.findById(query.OrganizationId(), query.UserId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.User_NOT_FOUND, "User not found"));
    }
}


