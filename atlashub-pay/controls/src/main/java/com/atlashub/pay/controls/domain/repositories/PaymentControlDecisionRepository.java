package com.atlashub.pay.controls.domain.repositories;

import com.atlashub.pay.controls.domain.entities.PaymentControlDecision;
import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface PaymentControlDecisionRepository extends Repository<PaymentControlDecision> {

    Optional<PaymentControlDecision> findByPaymentInstructionReferenceAndPolicyId(String instructionRef, Long policyId);

    Optional<PaymentControlDecision> findByPaymentInstructionReference(String instructionRef);

    PageResult<PaymentControlDecision> findAllByOrganizationId(Long organizationId, int pageNumber, int pageSize);
}
