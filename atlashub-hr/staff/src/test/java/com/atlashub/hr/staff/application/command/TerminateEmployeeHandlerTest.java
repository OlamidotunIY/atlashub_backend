package com.atlashub.hr.staff.application.command.TerminateEmployee;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;
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

class TerminateEmployeeHandlerTest {

    private EmployeeRepository employeeRepository;
    private TerminateEmployeeHandler handler;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        handler = new TerminateEmployeeHandler(employeeRepository);
    }

    @Test
    @DisplayName("Should terminate employee successfully")
    void shouldTerminateEmployeeSuccessfully() {
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

        LocalDate termDate = LocalDate.now();
        TerminateEmployeeCommand command = new TerminateEmployeeCommand(1L, termDate);

        handler.execute(command);

        assertEquals(EmployeeStatus.TERMINATED, employee.getStatus());
        assertEquals(termDate, employee.getTerminationDate());
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("Should throw EmployeeNotFoundException when employee does not exist")
    void shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        TerminateEmployeeCommand command = new TerminateEmployeeCommand(999L, LocalDate.now());

        assertThrows(EmployeeNotFoundException.class, () -> handler.execute(command));
    }
}
