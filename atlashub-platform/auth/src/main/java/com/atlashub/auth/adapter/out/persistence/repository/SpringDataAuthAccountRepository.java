package com.atlashub.auth.adapter.out.persistence.repository;

import com.atlashub.auth.domain.model.PrincipalType;
import com.atlashub.auth.adapter.out.persistence.entity.AuthAccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataAuthAccountRepository extends JpaRepository<AuthAccountJpaEntity, Long> {
    Optional<AuthAccountJpaEntity> findByPrincipalIdAndPrincipalType(Long principalId, PrincipalType principalType);
    Optional<AuthAccountJpaEntity> findByIdentifierOrSecondaryIdentifier(String identifier, String secondaryIdentifier);
    boolean existsByPrincipalIdAndPrincipalType(Long principalId, PrincipalType principalType);
}
