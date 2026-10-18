package com.atlashub.pay.mandates.infrastructure.persistence.repositories;

import com.atlashub.pay.mandates.domain.valueobject.MandateStatus;
import com.atlashub.pay.mandates.infrastructure.persistence.entities.PaymentMandateJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataPaymentMandateRepository extends JpaRepository<PaymentMandateJpa, Long> {

    Optional<PaymentMandateJpa> findByIdAndOrganizationId(Long id, Long organizationId);

    List<PaymentMandateJpa> findByOrganizationId(Long organizationId);

    List<PaymentMandateJpa> findByOrganizationIdAndCustomerId(Long organizationId, String customerId);

    List<PaymentMandateJpa> findByOrganizationIdAndStatus(Long organizationId, MandateStatus status);

    List<PaymentMandateJpa> findByOrganizationIdAndCustomerIdAndStatus(Long organizationId, String customerId, MandateStatus status);

    @Query("SELECT m FROM PaymentMandateJpa m WHERE m.status = 'ACTIVE' AND m.nextChargeDate <= :today")
    List<PaymentMandateJpa> findActiveMandatesDueForCharge(@Param("today") LocalDate today);
}
