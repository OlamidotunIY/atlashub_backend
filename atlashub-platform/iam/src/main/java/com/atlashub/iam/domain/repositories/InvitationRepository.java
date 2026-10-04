package com.atlashub.iam.domain.repositories;

import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.iam.domain.entities.Invitation;
import java.util.Optional;
import java.util.List;
import com.atlashub.iam.domain.valueobject.InvitationStatus;
import java.time.ZonedDateTime;

public interface InvitationRepository extends Repository<Invitation> {
    Optional<Invitation> findByToken(String token);
    List<Invitation> findByOrganizationId(Long organizationId);
    List<Invitation> findByOrganizationIdAndStatus(Long organizationId, InvitationStatus status);
    List<Invitation> findPendingExpiredBefore(ZonedDateTime cutoff);
    Optional<Invitation> findPendingByOrganizationIdAndEmail(Long organizationId, String email);
}
