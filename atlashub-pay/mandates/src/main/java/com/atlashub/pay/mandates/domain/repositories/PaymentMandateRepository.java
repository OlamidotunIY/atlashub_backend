package com.atlashub.pay.mandates.domain.repositories;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.valueobject.MandateStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentMandateRepository extends Repository<PaymentMandate> {

    Optional<PaymentMandate> findByIdAndOrganizationId(Long id, Long organizationId);

    List<PaymentMandate> findByOrganizationId(Long organizationId);

    List<PaymentMandate> findByOrganizationIdAndCustomerId(Long organizationId, String customerId);

    List<PaymentMandate> findByOrganizationIdAndStatus(Long organizationId, MandateStatus status);

    List<PaymentMandate> findByOrganizationIdAndCustomerIdAndStatus(Long organizationId, String customerId, MandateStatus status);

    List<PaymentMandate> findActiveMandatesDueForCharge(LocalDate today);
}
