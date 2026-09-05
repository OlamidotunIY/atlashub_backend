package com.atlashub.charges.adapter.out.persistence.repository;

import com.atlashub.charges.adapter.out.persistence.entity.ExternalChargeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataExternalChargeRepository extends JpaRepository<ExternalChargeJpaEntity, Long> {
    Optional<ExternalChargeJpaEntity> findByReference(String reference);
}
