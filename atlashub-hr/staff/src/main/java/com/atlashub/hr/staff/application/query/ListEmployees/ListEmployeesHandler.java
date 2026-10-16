package com.atlashub.hr.staff.application.query.ListEmployees;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListEmployeesHandler extends Query<ListEmployeesQuery, PageResult<EmployeeResult>> {

    private final EmployeeRepository employeeRepository;

    public ListEmployeesHandler(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public PageResult<EmployeeResult> execute(ListEmployeesQuery query) {
        List<Employee> allEmployees;

        if (query.status() != null && query.department() != null && !query.department().isBlank()) {
            allEmployees = employeeRepository.findByOrganizationIdAndStatusAndDepartment(
                    query.organizationId(),
                    query.status(),
                    query.department()
            );
        } else if (query.status() != null) {
            allEmployees = employeeRepository.findByOrganizationIdAndStatus(query.organizationId(), query.status());
        } else if (query.department() != null && !query.department().isBlank()) {
            allEmployees = employeeRepository.findByOrganizationIdAndDepartment(query.organizationId(), query.department());
        } else {
            allEmployees = employeeRepository.findByOrganizationId(query.organizationId());
        }

        int page = Math.max(0, query.page());
        int size = query.size() > 0 ? query.size() : 20;
        int totalElements = allEmployees.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<EmployeeResult> content = allEmployees.subList(fromIndex, toIndex).stream()
                .map(this::toResult)
                .toList();

        return new PageResult<>(content, page, size, totalElements, totalPages);
    }

    private EmployeeResult toResult(Employee employee) {
        return new EmployeeResult(
                employee.getId(),
                employee.getEmployeeNumber(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail().value(),
                employee.getPhone() != null ? employee.getPhone().value() : null,
                employee.getDesignation(),
                employee.getDepartment(),
                employee.getStatus(),
                employee.getHireDate()
        );
    }
}
