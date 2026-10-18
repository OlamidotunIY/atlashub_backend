package com.atlashub.pay.controls.infrastructure.persistence.repositories;

import com.atlashub.pay.controls.infrastructure.persistence.entities.PaymentControlDecisionJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataPaymentControlDecisionRepository extends JpaRepository<PaymentControlDecisionJpa, Long> {

    Optional<PaymentControlDecisionJpa> findByPaymentInstructionReferenceAndPolicyId(String instructionRef, Long policyId);

    Optional<PaymentControlDecisionJpa> findByPaymentInstructionReference(String instructionRef);

    Page<PaymentControlDecisionJpa> findAllByOrganizationId(Long organizationId, Pageable pageable);
}
