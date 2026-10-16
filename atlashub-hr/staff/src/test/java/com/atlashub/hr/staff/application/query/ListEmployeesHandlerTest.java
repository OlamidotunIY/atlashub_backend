package com.atlashub.hr.staff.application.query.ListEmployees;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListEmployeesHandlerTest {

    private EmployeeRepository employeeRepository;
    private ListEmployeesHandler handler;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        handler = new ListEmployeesHandler(employeeRepository);
    }

    private Employee createEmployee(Long id, String empNo, String dept, EmployeeStatus status) {
        Employee emp = Employee.create(
                id,
                10L,
                id + 100,
                empNo,
                "First",
                "Last",
                new EmailAddress("emp" + id + "@atlashub.com"),
                new PhoneNumber("+2348012345678"),
                LocalDate.of(1990, 1, 1),
                "Lagos",
                "Nigerian",
                LocalDate.of(2023, 1, 1),
                "Engineer",
                dept,
                null,
                1L,
                Money.of(new BigDecimal("500000.00"), CurrencyCode.NGN)
        );
        if (status == EmployeeStatus.ACTIVE) {
            emp.activate();
        }
        return emp;
    }

    @Test
    @DisplayName("Should list and paginate employees")
    void shouldListAndPaginateEmployees() {
        List<Employee> employees = List.of(
                createEmployee(1L, "EMP-0001", "Engineering", EmployeeStatus.ACTIVE),
                createEmployee(2L, "EMP-0002", "Engineering", EmployeeStatus.ACTIVE),
                createEmployee(3L, "EMP-0003", "Finance", EmployeeStatus.PROBATION)
        );

        when(employeeRepository.findByOrganizationId(10L)).thenReturn(employees);

        ListEmployeesQuery query = new ListEmployeesQuery(10L, null, null, 0, 2);
        PageResult<EmployeeResult> result = handler.execute(query);

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals(3, result.totalElements());
        assertEquals(2, result.totalPages());
        assertEquals("EMP-0001", result.content().get(0).employeeNumber());
        assertEquals("EMP-0002", result.content().get(1).employeeNumber());
    }

    @Test
    @DisplayName("Should filter employees by status and department")
    void shouldFilterEmployees() {
        List<Employee> activeEng = List.of(
                createEmployee(1L, "EMP-0001", "Engineering", EmployeeStatus.ACTIVE)
        );

        when(employeeRepository.findByOrganizationIdAndStatusAndDepartment(10L, EmployeeStatus.ACTIVE, "Engineering"))
                .thenReturn(activeEng);

        ListEmployeesQuery query = new ListEmployeesQuery(10L, EmployeeStatus.ACTIVE, "Engineering", 0, 10);
        PageResult<EmployeeResult> result = handler.execute(query);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("EMP-0001", result.content().getFirst().employeeNumber());
        assertEquals(EmployeeStatus.ACTIVE, result.content().getFirst().status());
    }
}
