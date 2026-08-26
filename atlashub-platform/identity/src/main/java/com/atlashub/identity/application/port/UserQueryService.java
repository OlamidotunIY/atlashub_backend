package com.atlashub.identity.application.port;

import com.atlashub.identity.application.dto.UserDto;
import com.atlashub.shared.util.PageResult;

import java.util.Optional;

public interface UserQueryService {
    Optional<UserDto> findById(Long OrganizationId, Long UserId);
    PageResult<UserDto> findAllByOrganizationId(Long OrganizationId, int page, int size, String emailFilter);
}
