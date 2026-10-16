package com.atlashub.hr.staff.domain.repositories;

import com.atlashub.hr.staff.domain.entities.EmployeeDeduction;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface EmployeeDeductionRepository extends Repository<EmployeeDeduction> {

    List<EmployeeDeduction> findByEmployeeId(Long employeeId);
}
