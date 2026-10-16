package com.atlashub.hr.payroll.domain.repositories;

import com.atlashub.hr.payroll.domain.entities.Payslip;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface PayslipRepository extends Repository<Payslip> {

    List<Payslip> findByPayrollRunId(Long payrollRunId);

    List<Payslip> findByEmployeeIdAndYear(Long employeeId, int year);
}
