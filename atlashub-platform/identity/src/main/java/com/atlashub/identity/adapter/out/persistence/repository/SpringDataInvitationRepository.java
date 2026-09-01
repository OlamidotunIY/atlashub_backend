package com.atlashub.identity.adapter.out.persistence.repository;

import com.atlashub.identity.adapter.out.persistence.entity.InvitationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataInvitationRepository extends JpaRepository<InvitationJpaEntity, UUID> {
    Optional<InvitationJpaEntity> findByToken(String token);
    List<InvitationJpaEntity> findByOrganizationId(Long organizationId);
    List<InvitationJpaEntity> findByInvitedEmail(String email);
    
    @Query("SELECT i FROM InvitationJpaEntity i WHERE i.status = 'PENDING' AND i.expiresAt < CURRENT_TIMESTAMP")
    List<InvitationJpaEntity> findExpiredPendingInvitations();
}
