package com.atlashub.hr.loan.application.command.RecordLoanRepayment;

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
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecordLoanRepaymentHandlerTest {

    private EmployeeLoanRepository repository;
    private RecordLoanRepaymentHandler handler;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(EmployeeLoanRepository.class);
        handler = new RecordLoanRepaymentHandler(repository);
    }

    @Test
    @DisplayName("Should record loan repayment successfully")
    void shouldRecordRepaymentSuccessfully() {
        Money principal = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        Money deduction = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        EmployeeLoan loan = EmployeeLoan.create(1L, 100L, 10L, principal, deduction);
        loan.approve(200L, LocalDate.of(2026, 8, 1));
        loan.disburse();

        when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(loan));

        handler.execute(new RecordLoanRepaymentCommand(1L, Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN)));

        assertThat(loan.getOutstandingBalance().amount()).isEqualByComparingTo(new BigDecimal("80000.0000"));
        verify(repository).save(loan);
    }

    @Test
    @DisplayName("Should throw LoanNotFoundException when loan does not exist")
    void shouldThrowIfLoanNotFound() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () ->
                handler.execute(new RecordLoanRepaymentCommand(1L, Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN)))
        );
    }
}
