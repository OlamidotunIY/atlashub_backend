package com.atlashub.charges.domain.repository;

import com.atlashub.charges.domain.model.PaystackCharge;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;

public interface PaystackChargeRepository extends Repository<PaystackCharge> {
    Optional<PaystackCharge> findByReference(String reference);
}
