package com.atlashub.iam.infrastructure.persistence.repositories;

import com.atlashub.iam.infrastructure.persistence.entities.InvitationJpa;
import com.atlashub.iam.domain.valueobject.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface SpringDataInvitationRepository extends JpaRepository<InvitationJpa, Long> {
    Optional<InvitationJpa> findByToken(String token);
    List<InvitationJpa> findByOrganizationIdAndStatus(Long organizationId, InvitationStatus status);
}
