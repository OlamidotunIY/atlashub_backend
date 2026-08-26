package com.atlashub.auth.adapter.out.persistence.repository;

import com.atlashub.auth.domain.model.SessionStatus;
import com.atlashub.auth.adapter.out.persistence.entity.SessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataSessionRepository extends JpaRepository<SessionJpaEntity, Long> {
    Optional<SessionJpaEntity> findByToken(String token);
    List<SessionJpaEntity> findByAuthAccountIdAndStatus(Long authAccountId, SessionStatus status);
    List<SessionJpaEntity> findByAuthAccountId(Long authAccountId);
}
