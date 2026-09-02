package com.atlashub.identity.adapter.in.internal;

import com.atlashub.identity.adapter.out.persistence.repository.SpringDataUserRepository;
import com.atlashub.shared.application.api.UserQueryApi;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class IdentityUserApiImpl implements UserQueryApi {

    private final SpringDataUserRepository userRepository;

    public IdentityUserApiImpl(SpringDataUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSharedDto> getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(entity -> new UserSharedDto(
                        entity.getId(),
                        entity.getFirstName(),
                        entity.getLastName(),
                        entity.getEmail(),
                        entity.getImageUrl(),
                        entity.getPhone(),
                        entity.getCountry()
                ));
    }
}
