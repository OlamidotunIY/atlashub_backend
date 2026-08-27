package com.atlashub.identity.adapter.out.persistence.query;

import com.atlashub.identity.application.result.UserDto;
import com.atlashub.identity.application.port.UserQueryService;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataUserRepository;
import com.atlashub.shared.util.PageResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final SpringDataUserRepository repository;

    public UserQueryServiceImpl(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserDto> findById(Long OrganizationId, Long UserId) {
        return repository.findById(UserId)
                .map(user -> new UserDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getPhone(),
                        null,
                        user.getCreatedAt()
                ));
    }

    @Override
    public PageResult<UserDto> findAllByOrganizationId(Long OrganizationId, int page, int size, String emailFilter) {
        return new PageResult<>(Collections.emptyList(), page, size, 0L, 0);
    }
}
