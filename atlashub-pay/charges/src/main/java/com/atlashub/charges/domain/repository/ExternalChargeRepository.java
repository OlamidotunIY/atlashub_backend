package com.atlashub.charges.domain.repository;

import com.atlashub.charges.domain.model.ExternalCharge;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface ExternalChargeRepository extends Repository<ExternalCharge> {
    Optional<ExternalCharge> findByReference(String reference);
}
