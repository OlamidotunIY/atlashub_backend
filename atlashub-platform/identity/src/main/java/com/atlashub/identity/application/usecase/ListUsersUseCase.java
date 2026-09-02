package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.result.UserDto;
import com.atlashub.identity.application.port.UserQueryService;
import com.atlashub.identity.application.query.ListUsersQuery;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.application.util.PageResult;

@Service
public class ListUsersUseCase extends BaseUseCase<ListUsersQuery, PageResult<UserDto>> {
    private static final Logger log = LoggerFactory.getLogger(ListUsersUseCase.class);


    private final UserQueryService queryService;

    public ListUsersUseCase(UserQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public PageResult<UserDto> execute(ListUsersQuery query) {
        return queryService.findAllByOrganizationId(
                query.OrganizationId(),
                query.page(),
                query.size(),
                query.emailFilter()
        );
    }
}


