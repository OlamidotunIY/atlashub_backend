package com.atlashub.identity.domain.repository;

import com.atlashub.identity.domain.model.Invitation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository {
    Invitation save(Invitation invitation);
    Optional<Invitation> findById(UUID id);
    Optional<Invitation> findByToken(String token);
    List<Invitation> findByOrganizationId(Long organizationId);
    List<Invitation> findByInvitedEmail(String email);
    List<Invitation> findExpiredPendingInvitations();
}
