package com.atlashub.accounts.infrastructure.persistence.adapters;

import com.atlashub.accounts.domain.model.User;
import com.atlashub.accounts.domain.repository.UserRepository;
import com.atlashub.accounts.infrastructure.persistence.entities.UserJPA;
import com.atlashub.accounts.infrastructure.persistence.mappers.UserMapper;
import com.atlashub.accounts.infrastructure.persistence.repositories.SpringDataUserRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter extends JpaBaseRepository<User, UserJPA> implements UserRepository {

    private final SpringDataUserRepository springDataRepo;

    public UserRepositoryAdapter(SpringDataUserRepository springDataRepo,
                                 UserMapper mapper,
                                 DomainSequenceGenerator sequenceGenerator, DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataRepo.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    protected String getSequenceName() {
        return "user_seq";
    }
}
