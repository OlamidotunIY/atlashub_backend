package com.atlashub.hr.staff.application.command.OnboardEmployee;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.entities.SalaryGrade;
import com.atlashub.hr.staff.domain.exceptions.SalaryGradeNotFoundException;
import com.atlashub.hr.staff.domain.exceptions.UserAlreadyEmployedException;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.hr.staff.domain.repositories.SalaryGradeRepository;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OnboardEmployeeHandlerTest {

    private EmployeeRepository employeeRepository;
    private SalaryGradeRepository salaryGradeRepository;
    private OnboardEmployeeHandler handler;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        salaryGradeRepository = mock(SalaryGradeRepository.class);
        handler = new OnboardEmployeeHandler(employeeRepository, salaryGradeRepository);
    }

    @Test
    @DisplayName("Should onboard employee successfully")
    void shouldOnboardEmployeeSuccessfully() {
        OnboardEmployeeCommand command = new OnboardEmployeeCommand(
                10L,
                100L,
                "Jane",
                "Smith",
                "jane.smith@atlashub.com",
                "+2348011112222",
                LocalDate.of(1992, 5, 20),
                "Victoria Island, Lagos",
                "Nigerian",
                LocalDate.of(2023, 2, 1),
                "Product Manager",
                "Product",
                null,
                1L,
                Money.of(new BigDecimal("900000.00"), CurrencyCode.NGN)
        );

        when(employeeRepository.existsByOrganizationIdAndUserId(10L, 100L)).thenReturn(false);
        when(salaryGradeRepository.findById(1L)).thenReturn(Optional.of(mock(SalaryGrade.class)));
        when(employeeRepository.nextIdentity()).thenReturn(42L);

        OnboardEmployeeResponse response = handler.execute(command);

        assertNotNull(response);
        assertEquals(42L, response.employeeId());
        assertEquals("EMP-0042", response.employeeNumber());

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        Employee saved = captor.getValue();
        assertEquals(42L, saved.getId());
        assertEquals("Jane", saved.getFirstName());
        assertEquals("EMP-0042", saved.getEmployeeNumber());
    }

    @Test
    @DisplayName("Should throw UserAlreadyEmployedException when user already exists in organization")
    void shouldThrowWhenUserAlreadyEmployed() {
        OnboardEmployeeCommand command = new OnboardEmployeeCommand(
                10L,
                100L,
                "Jane",
                "Smith",
                "jane.smith@atlashub.com",
                "+2348011112222",
                LocalDate.of(1992, 5, 20),
                "Victoria Island, Lagos",
                "Nigerian",
                LocalDate.of(2023, 2, 1),
                "Product Manager",
                "Product",
                null,
                1L,
                Money.of(new BigDecimal("900000.00"), CurrencyCode.NGN)
        );

        when(employeeRepository.existsByOrganizationIdAndUserId(10L, 100L)).thenReturn(true);

        assertThrows(UserAlreadyEmployedException.class, () -> handler.execute(command));
    }

    @Test
    @DisplayName("Should throw SalaryGradeNotFoundException when grade does not exist")
    void shouldThrowWhenSalaryGradeNotFound() {
        OnboardEmployeeCommand command = new OnboardEmployeeCommand(
                10L,
                100L,
                "Jane",
                "Smith",
                "jane.smith@atlashub.com",
                "+2348011112222",
                LocalDate.of(1992, 5, 20),
                "Victoria Island, Lagos",
                "Nigerian",
                LocalDate.of(2023, 2, 1),
                "Product Manager",
                "Product",
                null,
                999L,
                Money.of(new BigDecimal("900000.00"), CurrencyCode.NGN)
        );

        when(employeeRepository.existsByOrganizationIdAndUserId(10L, 100L)).thenReturn(false);
        when(salaryGradeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(SalaryGradeNotFoundException.class, () -> handler.execute(command));
    }
}
