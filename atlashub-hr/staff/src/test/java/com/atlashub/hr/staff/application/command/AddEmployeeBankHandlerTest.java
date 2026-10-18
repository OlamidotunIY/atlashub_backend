package com.atlashub.hr.staff.application.command.AddEmployeeBank;

import com.atlashub.shared.application.port.BankVerificationPort;
import com.atlashub.hr.staff.domain.entities.Employee;
import com.atlashub.hr.staff.domain.entities.EmployeeBank;
import com.atlashub.hr.staff.domain.exceptions.EmployeeBankUnverifiedException;
import com.atlashub.hr.staff.domain.exceptions.EmployeeNotFoundException;
import com.atlashub.hr.staff.domain.repositories.EmployeeBankRepository;
import com.atlashub.hr.staff.domain.repositories.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AddEmployeeBankHandlerTest {

    private EmployeeRepository employeeRepository;
    private EmployeeBankRepository employeeBankRepository;
    private BankVerificationPort bankVerificationPort;
    private AddEmployeeBankHandler handler;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        employeeBankRepository = mock(EmployeeBankRepository.class);
        bankVerificationPort = mock(BankVerificationPort.class);
        handler = new AddEmployeeBankHandler(employeeRepository, employeeBankRepository, bankVerificationPort);
    }

    @Test
    @DisplayName("Should verify and add bank account as primary")
    void shouldAddBankAccountSuccessfully() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mock(Employee.class)));
        when(bankVerificationPort.verifyAccount("058", "0123456789")).thenReturn("JOHN DOE");

        EmployeeBank existingPrimary = mock(EmployeeBank.class);
        when(employeeBankRepository.findByEmployeeIdAndIsPrimaryTrue(1L)).thenReturn(Optional.of(existingPrimary));
        when(employeeBankRepository.nextIdentity()).thenReturn(77L);

        AddEmployeeBankCommand command = new AddEmployeeBankCommand(
                1L,
                "058",
                "Guaranty Trust Bank",
                "0123456789",
                true
        );

        AddEmployeeBankResponse response = handler.execute(command);

        assertNotNull(response);
        assertEquals(77L, response.bankId());
        assertEquals("JOHN DOE", response.accountName());

        verify(existingPrimary).setPrimary(false);

        ArgumentCaptor<EmployeeBank> captor = ArgumentCaptor.forClass(EmployeeBank.class);
        verify(employeeBankRepository, times(2)).save(captor.capture());
        EmployeeBank saved = captor.getAllValues().get(1);
        assertEquals(77L, saved.getId());
        assertEquals("058", saved.getBankCode());
        assertEquals("JOHN DOE", saved.getAccountName());
        assertTrue(saved.isPrimary());
        assertTrue(saved.isVerified());
    }

    @Test
    @DisplayName("Should throw EmployeeNotFoundException when employee does not exist")
    void shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        AddEmployeeBankCommand command = new AddEmployeeBankCommand(
                999L,
                "058",
                "GTB",
                "0123456789",
                false
        );

        assertThrows(EmployeeNotFoundException.class, () -> handler.execute(command));
    }

    @Test
    @DisplayName("Should throw EmployeeBankUnverifiedException when bank verification fails")
    void shouldThrowWhenVerificationFails() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mock(Employee.class)));
        when(bankVerificationPort.verifyAccount("058", "0000000000")).thenReturn(null);

        AddEmployeeBankCommand command = new AddEmployeeBankCommand(
                1L,
                "058",
                "GTB",
                "0000000000",
                false
        );

        assertThrows(EmployeeBankUnverifiedException.class, () -> handler.execute(command));
    }
}
