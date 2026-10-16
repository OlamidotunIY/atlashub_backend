package com.atlashub.hr.payroll.infrastructure.persistence.repositories;

import com.atlashub.hr.payroll.infrastructure.persistence.entities.PayslipJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataPayslipRepository extends JpaRepository<PayslipJpaEntity, Long> {

    List<PayslipJpaEntity> findByPayrollRunId(Long payrollRunId);

    List<PayslipJpaEntity> findByEmployeeId(Long employeeId);
}
