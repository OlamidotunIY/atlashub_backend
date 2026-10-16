package com.atlashub.hr.staff.domain.repositories;

import com.atlashub.hr.staff.domain.entities.EmployeeAllowance;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface EmployeeAllowanceRepository extends Repository<EmployeeAllowance> {

    List<EmployeeAllowance> findByEmployeeId(Long employeeId);
}
