package com.atlashub.auth.adapter.out.persistence;

import com.atlashub.auth.domain.model.Session;
import com.atlashub.auth.domain.valueobject.SessionStatus;
import com.atlashub.auth.domain.repository.SessionRepository;
import com.atlashub.auth.adapter.out.persistence.entity.SessionJpaEntity;
import com.atlashub.auth.adapter.out.persistence.mapper.SessionMapper;
import com.atlashub.auth.adapter.out.persistence.repository.SpringDataSessionRepository;
import com.atlashub.shared.adapter.out.external.DomainSequenceGenerator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SessionRepositoryAdapter implements SessionRepository {

    private final SpringDataSessionRepository jpaRepository;
    private final DomainSequenceGenerator sequenceGenerator;
    private final SessionMapper mapper;

    public SessionRepositoryAdapter(SpringDataSessionRepository jpaRepository, DomainSequenceGenerator sequenceGenerator, SessionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.mapper = mapper;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("session_seq");
    }

    @Override
    public Session save(Session session) {
        SessionJpaEntity entity = mapper.toEntity(session);
        jpaRepository.save(entity);
        return session;
    }

    @Override
    public Optional<Session> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Session> findByToken(String token) {
        return jpaRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public List<Session> findByAuthAccountIdAndStatus(Long authAccountId, SessionStatus status) {
        return jpaRepository.findByAuthAccountIdAndStatus(authAccountId, status)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Session> findByAuthAccountId(Long authAccountId) {
        return jpaRepository.findByAuthAccountId(authAccountId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Session> saveAll(List<Session> sessions) {
        List<SessionJpaEntity> entities = sessions.stream().map(mapper::toEntity).collect(Collectors.toList());
        jpaRepository.saveAll(entities);
        return sessions;
    }

    @Override
    public boolean existsByAuthAccountIdAndIpAddressAndUserAgent(Long authAccountId, String ipAddress, String userAgent) {
        return jpaRepository.existsByAuthAccountIdAndIpAddressAndUserAgent(authAccountId, ipAddress, userAgent);
    }
}
