package com.atlashub.identity.adapter.out.persistence.adapter;

import com.atlashub.identity.domain.model.User;
import com.atlashub.identity.domain.repository.UserRepository;
import com.atlashub.identity.adapter.out.persistence.entity.UserJpaEntity;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataUserRepository;
import com.atlashub.identity.adapter.out.persistence.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository jpaRepository;
    private final com.atlashub.shared.adapter.out.external.DomainSequenceGenerator sequenceGenerator;
    private final UserMapper mapper;

    public UserRepositoryAdapter(SpringDataUserRepository jpaRepository, com.atlashub.shared.adapter.out.external.DomainSequenceGenerator sequenceGenerator, UserMapper mapper) {
        this.sequenceGenerator = sequenceGenerator;
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User User) {
        UserJpaEntity entity = mapper.toEntity(User);
        jpaRepository.save(entity);
        return User;
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByOrganizationIdAndEmail(Long OrganizationId, String email) {
        return jpaRepository.findByIntegrationAndEmail(OrganizationId, email).map(mapper::toDomain);
    }


    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("User_seq");
    }

}
