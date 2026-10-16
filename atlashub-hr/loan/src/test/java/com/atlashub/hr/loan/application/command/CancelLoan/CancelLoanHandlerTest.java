package com.atlashub.hr.loan.application.command.CancelLoan;

import com.atlashub.hr.loan.domain.entities.EmployeeLoan;
import com.atlashub.hr.loan.domain.exceptions.LoanNotFoundException;
import com.atlashub.hr.loan.domain.repositories.EmployeeLoanRepository;
import com.atlashub.hr.loan.domain.valueobject.LoanStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CancelLoanHandlerTest {

    private EmployeeLoanRepository repository;
    private CancelLoanHandler handler;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(EmployeeLoanRepository.class);
        handler = new CancelLoanHandler(repository);
    }

    @Test
    @DisplayName("Should cancel loan successfully")
    void shouldCancelLoanSuccessfully() {
        Money principal = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        Money deduction = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        EmployeeLoan loan = EmployeeLoan.create(1L, 100L, 10L, principal, deduction);

        when(repository.findById(1L)).thenReturn(Optional.of(loan));

        handler.execute(new CancelLoanCommand(1L));

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.CANCELLED);
        verify(repository).save(loan);
    }

    @Test
    @DisplayName("Should throw LoanNotFoundException when cancelling non-existent loan")
    void shouldThrowIfLoanNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () ->
                handler.execute(new CancelLoanCommand(1L))
        );
    }
}
