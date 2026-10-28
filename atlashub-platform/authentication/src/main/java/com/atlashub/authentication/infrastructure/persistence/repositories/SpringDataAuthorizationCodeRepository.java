package com.atlashub.authentication.infrastructure.persistence.repositories;

import com.atlashub.authentication.infrastructure.persistence.entities.AuthorizationCodeJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface SpringDataAuthorizationCodeRepository extends JpaRepository<AuthorizationCodeJpa, Long> {
    Optional<AuthorizationCodeJpa> findByCodeHash(String codeHash);

    @Modifying
    @Query("DELETE FROM AuthorizationCodeJpa a WHERE a.expiresAt < :threshold")
    void deleteExpiredBefore(@Param("threshold") ZonedDateTime threshold);
}
