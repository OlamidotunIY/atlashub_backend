package com.atlashub.accounts.infrastructure.services;

import com.atlashub.accounts.infrastructure.persistence.entities.UserJPA;
import com.atlashub.accounts.infrastructure.persistence.repositories.SpringDataUserRepository;
import com.atlashub.shared.application.port.UserQueryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserQueryPortAdapter implements UserQueryPort {

    private final SpringDataUserRepository springDataRepo;

    public UserQueryPortAdapter(SpringDataUserRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<UserDto> findById(Long userId) {
        return springDataRepo.findById(userId).map(this::toDto);
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        return springDataRepo.findByEmail(email).map(this::toDto);
    }

    @Override
    public boolean existsById(Long userId) {
        return springDataRepo.existsById(userId);
    }

    private UserDto toDto(UserJPA jpa) {
        return new UserDto(
                jpa.getId(),
                jpa.getFirstName(),
                jpa.getLastName(),
                jpa.getEmail(),
                jpa.getCountry(),
                jpa.getActiveOrganizationId()
        );
    }
}
