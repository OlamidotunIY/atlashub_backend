package com.atlashub.pay.controls.infrastructure.persistence.repositories;

import com.atlashub.pay.controls.domain.valueobject.PolicyStatus;
import com.atlashub.pay.controls.infrastructure.persistence.entities.ControlledFundsPolicyJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataControlledFundsPolicyRepository extends JpaRepository<ControlledFundsPolicyJpa, Long> {

    Optional<ControlledFundsPolicyJpa> findByFacilityIdAndStatus(Long facilityId, PolicyStatus status);

    Optional<ControlledFundsPolicyJpa> findByFacilityIdAndPolicyVersion(Long facilityId, String policyVersion);
}
