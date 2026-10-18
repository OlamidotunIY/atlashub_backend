package com.atlashub.hr.staff.application.query.GetEmployeeDetails;

import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.entities.EmployeeAllowance;
import com.atlashub.hr.staff.domain.entities.EmployeeBank;
import com.atlashub.hr.staff.domain.entities.EmployeeDeduction;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeAllowanceRepository;
import com.atlashub.hr.staff.domain.repositories.EmployeeBankRepository;
import com.atlashub.hr.staff.domain.repositories.EmployeeDeductionRepository;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import com.atlashub.hr.staff.domain.valueobject.AllowanceType;
import com.atlashub.hr.staff.domain.valueobject.DeductionType;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetEmployeeDetailsHandlerTest {

    private EmployeeRepository employeeRepository;
    private EmployeeBankRepository employeeBankRepository;
    private EmployeeAllowanceRepository employeeAllowanceRepository;
    private EmployeeDeductionRepository employeeDeductionRepository;
    private GetEmployeeDetailsHandler handler;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        employeeBankRepository = mock(EmployeeBankRepository.class);
        employeeAllowanceRepository = mock(EmployeeAllowanceRepository.class);
        employeeDeductionRepository = mock(EmployeeDeductionRepository.class);
        handler = new GetEmployeeDetailsHandler(
                employeeRepository,
                employeeBankRepository,
                employeeAllowanceRepository,
                employeeDeductionRepository
        );
    }

    @Test
    @DisplayName("Should return full employee details with banks, allowances, deductions")
    void shouldReturnEmployeeDetails() {
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
                "Lead Dev",
                "Engineering",
                null,
                1L,
                Money.of(new BigDecimal("1000000.00"), CurrencyCode.NGN)
        );

        EmployeeBank bank = EmployeeBank.create(
                10L,
                1L,
                "058",
                "GTB",
                "0123456789",
                "JOHN DOE",
                true,
                true
        );

        EmployeeAllowance allowance = EmployeeAllowance.create(
                20L,
                1L,
                "Housing",
                AllowanceType.FIXED,
                new BigDecimal("200000.00"),
                LocalDate.of(2023, 1, 1),
                null
        );

        EmployeeDeduction deduction = EmployeeDeduction.create(
                30L,
                1L,
                "Pension",
                DeductionType.PERCENTAGE_OF_GROSS,
                new BigDecimal("8.00"),
                LocalDate.of(2023, 1, 1),
                null,
                null
        );

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeBankRepository.findByEmployeeId(1L)).thenReturn(List.of(bank));
        when(employeeAllowanceRepository.findByEmployeeId(1L)).thenReturn(List.of(allowance));
        when(employeeDeductionRepository.findByEmployeeId(1L)).thenReturn(List.of(deduction));

        EmployeeDetailsResult result = handler.execute(new GetEmployeeDetailsQuery(1L, 10L));

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("EMP-001", result.employeeNumber());
        assertEquals(1, result.banks().size());
        assertEquals("0123456789", result.banks().getFirst().accountNumber());
        assertEquals(1, result.allowances().size());
        assertEquals("Housing", result.allowances().getFirst().name());
        assertEquals(1, result.deductions().size());
        assertEquals("Pension", result.deductions().getFirst().name());
    }

    @Test
    @DisplayName("Should throw EmployeeNotFoundException when employee does not exist")
    void shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () ->
                handler.execute(new GetEmployeeDetailsQuery(999L, 10L)));
    }
}
