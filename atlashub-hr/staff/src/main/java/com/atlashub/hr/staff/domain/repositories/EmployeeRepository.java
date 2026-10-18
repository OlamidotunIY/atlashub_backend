package com.atlashub.hr.staff.domain.repositories;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends Repository<Employee> {

    List<Employee> findByOrganizationId(Long organizationId);

    List<Employee> findByOrganizationIdAndStatus(Long organizationId, EmployeeStatus status);

    List<Employee> findByOrganizationIdAndDepartment(Long organizationId, String department);

    List<Employee> findByOrganizationIdAndStatusAndDepartment(Long organizationId, EmployeeStatus status, String department);

    Optional<Employee> findByOrganizationIdAndUserId(Long organizationId, Long userId);

    Optional<Employee> findByOrganizationIdAndEmployeeNumber(Long organizationId, String employeeNumber);

    boolean existsByOrganizationIdAndUserId(Long organizationId, Long userId);
}
