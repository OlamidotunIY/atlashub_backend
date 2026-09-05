package com.atlashub.identity.adapter.out.persistence.query;

import com.atlashub.identity.application.result.UserDto;
import com.atlashub.identity.application.port.UserQueryService;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataUserRepository;
import com.atlashub.shared.application.util.PageResult;
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
        return getUserById(UserId);
    }

    @Override
    public PageResult<UserDto> findAllByOrganizationId(Long OrganizationId, int page, int size, String emailFilter) {
        return new PageResult<>(Collections.emptyList(), page, size, 0L, 0);
    }

    @Override
    public Optional<UserDto> getUserById(Long userId) {
        return repository.findById(userId)
                .map(user -> new UserDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getImageUrl(),
                        user.getPhone(),
                        user.getCountry(),
                        user.getCreatedAt()
                ));
    }

    @Override
    public Optional<UserDto> getUserByEmail(String email) {
        return repository.findByEmail(email)
                .map(user -> new UserDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getImageUrl(),
                        user.getPhone(),
                        user.getCountry(),
                        user.getCreatedAt()
                ));
    }
}
