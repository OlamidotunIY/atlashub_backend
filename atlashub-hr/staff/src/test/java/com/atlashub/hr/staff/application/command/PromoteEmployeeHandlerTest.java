package com.atlashub.hr.staff.application.command.PromoteEmployee;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.entities.SalaryGrade;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.exceptions.SalaryGradeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.hr.staff.domain.repositories.SalaryGradeRepository;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PromoteEmployeeHandlerTest {

    private EmployeeRepository employeeRepository;
    private SalaryGradeRepository salaryGradeRepository;
    private PromoteEmployeeHandler handler;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        salaryGradeRepository = mock(SalaryGradeRepository.class);
        handler = new PromoteEmployeeHandler(employeeRepository, salaryGradeRepository);
    }

    @Test
    @DisplayName("Should promote employee successfully")
    void shouldPromoteEmployeeSuccessfully() {
        Employee employee = Employee.create(
                1L,
                10L,
                100L,
                "EMP-001",
                "John",
                "Doe",
                new EmailAddress("john@atlashub.com"),
                new PhoneNumber("+2348012345678"),
                LocalDate.of(1990, 1, 1),
                "Lagos",
                "Nigerian",
                LocalDate.of(2023, 1, 1),
                "Software Engineer",
                "Engineering",
                null,
                1L,
                Money.of(new BigDecimal("600000.00"), CurrencyCode.NGN)
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryGradeRepository.findById(2L)).thenReturn(Optional.of(mock(SalaryGrade.class)));

        Money newSalary = Money.of(new BigDecimal("850000.00"), CurrencyCode.NGN);
        PromoteEmployeeCommand command = new PromoteEmployeeCommand(
                1L,
                "Senior Software Engineer",
                2L,
                newSalary
        );

        handler.execute(command);

        assertEquals("Senior Software Engineer", employee.getDesignation());
        assertEquals(2L, employee.getSalaryGradeId());
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("Should throw EmployeeNotFoundException when employee does not exist")
    void shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        PromoteEmployeeCommand command = new PromoteEmployeeCommand(
                999L,
                "Lead",
                2L,
                Money.of(new BigDecimal("1000000.00"), CurrencyCode.NGN)
        );

        assertThrows(EmployeeNotFoundException.class, () -> handler.execute(command));
    }

    @Test
    @DisplayName("Should throw SalaryGradeNotFoundException when new grade does not exist")
    void shouldThrowWhenGradeNotFound() {
        Employee employee = Employee.create(
                1L,
                10L,
                100L,
                "EMP-001",
                "John",
                "Doe",
                new EmailAddress("john@atlashub.com"),
                new PhoneNumber("+2348012345678"),
                LocalDate.of(1990, 1, 1),
                "Lagos",
                "Nigerian",
                LocalDate.of(2023, 1, 1),
                "Dev",
                "Eng",
                null,
                1L,
                Money.of(new BigDecimal("600000.00"), CurrencyCode.NGN)
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryGradeRepository.findById(999L)).thenReturn(Optional.empty());

        PromoteEmployeeCommand command = new PromoteEmployeeCommand(
                1L,
                "Lead",
                999L,
                Money.of(new BigDecimal("1000000.00"), CurrencyCode.NGN)
        );

        assertThrows(SalaryGradeNotFoundException.class, () -> handler.execute(command));
    }
}
