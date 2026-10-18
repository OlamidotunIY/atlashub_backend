package com.atlashub.hr.payroll.infrastructure.persistence.repositories;

import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import com.atlashub.hr.payroll.infrastructure.persistence.entities.PayrollRunJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataPayrollRunRepository extends JpaRepository<PayrollRunJpaEntity, Long> {

    List<PayrollRunJpaEntity> findByOrganizationIdAndStatus(Long organizationId, PayrollStatus status);

    Optional<PayrollRunJpaEntity> findByOrganizationIdAndPeriod(Long organizationId, String period);

    List<PayrollRunJpaEntity> findByOrganizationId(Long organizationId);

    List<PayrollRunJpaEntity> findByStatusAndCreatedAtBefore(PayrollStatus status, ZonedDateTime threshold);
}
