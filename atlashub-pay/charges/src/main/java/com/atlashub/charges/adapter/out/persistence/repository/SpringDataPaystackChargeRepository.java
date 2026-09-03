package com.atlashub.charges.adapter.out.persistence.repository;

import com.atlashub.charges.adapter.out.persistence.entity.PaystackChargeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataPaystackChargeRepository extends JpaRepository<PaystackChargeJpaEntity, Long> {
    Optional<PaystackChargeJpaEntity> findByReference(String reference);
}
