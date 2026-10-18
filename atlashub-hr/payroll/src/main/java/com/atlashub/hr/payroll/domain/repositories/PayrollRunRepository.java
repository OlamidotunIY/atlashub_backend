package com.atlashub.hr.payroll.domain.repositories;

import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface PayrollRunRepository extends Repository<PayrollRun> {

    Optional<PayrollRun> findByOrganizationIdAndPeriod(Long organizationId, YearMonth period);

    List<PayrollRun> findByOrganizationId(Long organizationId);

    List<PayrollRun> findByOrganizationIdAndStatus(Long organizationId, PayrollStatus status);

    List<PayrollRun> findByStatusAndCreatedAtBefore(PayrollStatus status, ZonedDateTime threshold);
}
