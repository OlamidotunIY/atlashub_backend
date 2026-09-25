package com.atlashub.accounts.infrastructure.persistence.adapters;

import com.atlashub.accounts.domain.model.User;
import com.atlashub.accounts.domain.repository.UserRepository;
import com.atlashub.accounts.infrastructure.services.UserQueryPortAdapter;
import com.atlashub.accounts.infrastructure.persistence.entities.UserJPA;
import com.atlashub.accounts.infrastructure.persistence.mappers.UserMapper;
import com.atlashub.accounts.infrastructure.persistence.repositories.SpringDataUserRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class UserRepositoryAdapter extends JpaBaseRepository<User, UserJPA> implements UserRepository {

    private final SpringDataUserRepository springDataRepo;
    private final StringRedisTemplate redisTemplate;

    public UserRepositoryAdapter(SpringDataUserRepository springDataRepo,
                                 UserMapper mapper,
                                 DomainSequenceGenerator sequenceGenerator,
                                 DomainEventPublisher eventPublisher,
                                 StringRedisTemplate redisTemplate) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataRepo.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public User save(User entity) {
        Optional<User> existing = entity.getId() == null ? Optional.empty() : findById(entity.getId());
        User saved = super.save(entity);

        existing.map(User::getEmail)
                .ifPresent(email -> evictByEmail(email.value()));
        evictUser(saved);

        return saved;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(this::evictUser);
        super.deleteById(id);
    }

    @Override
    protected String getSequenceName() {
        return "user_seq";
    }

    private void evictUser(User user) {
        try {
            redisTemplate.delete(UserQueryPortAdapter.USER_BY_ID_KEY_PREFIX + user.getId());
            redisTemplate.delete(UserQueryPortAdapter.USER_ACTIVE_ORG_KEY_PREFIX + user.getId());
            evictByEmail(user.getEmail().value());
        } catch (RuntimeException ignored) {
        }
    }

    private void evictByEmail(String email) {
        if (email == null) {
            return;
        }
        try {
            redisTemplate.delete(UserQueryPortAdapter.USER_BY_EMAIL_KEY_PREFIX + email.trim().toLowerCase());
        } catch (RuntimeException ignored) {
        }
    }
}
