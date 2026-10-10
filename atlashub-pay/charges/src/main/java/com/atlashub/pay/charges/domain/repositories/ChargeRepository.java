package com.atlashub.pay.charges.domain.repositories;

import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.repository.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface ChargeRepository extends Repository<Charge> {
    Optional<Charge> findByOrganizationIdAndEnvironmentAndReference(Long organizationId, ApiEnvironment environment,
                                                                    String reference);

    Optional<Charge> findByProviderReferenceAndEnvironment(String providerReference, ApiEnvironment environment);

    List<Charge> findPendingCharges(ZonedDateTime createdBefore);
}
