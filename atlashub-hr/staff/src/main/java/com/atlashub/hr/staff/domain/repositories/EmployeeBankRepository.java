package com.atlashub.hr.staff.domain.repositories;

import com.atlashub.hr.staff.domain.entities.EmployeeBank;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface EmployeeBankRepository extends Repository<EmployeeBank> {

    List<EmployeeBank> findByEmployeeId(Long employeeId);

    Optional<EmployeeBank> findByEmployeeIdAndIsPrimaryTrue(Long employeeId);
}
