package com.atlashub.pay.controls.domain.repositories;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface ControlledFundsPolicyRepository extends Repository<ControlledFundsPolicy> {

    Optional<ControlledFundsPolicy> findActiveByFacilityId(Long facilityId);

    Optional<ControlledFundsPolicy> findByFacilityIdAndPolicyVersion(Long facilityId, String policyVersion);
}
