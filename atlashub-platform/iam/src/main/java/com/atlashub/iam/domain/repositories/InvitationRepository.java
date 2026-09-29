package com.atlashub.iam.domain.repositories;

import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.iam.domain.entities.Invitation;
import java.util.Optional;
import java.util.List;
import com.atlashub.iam.domain.valueobject.InvitationStatus;

public interface InvitationRepository extends Repository<Invitation> {
    Optional<Invitation> findByToken(String token);
    List<Invitation> findByOrganizationIdAndStatus(Long organizationId, InvitationStatus status);
}
