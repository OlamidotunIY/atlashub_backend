package com.atlashub.hr.loan.application.command.ApplyForLoan;

import com.atlashub.hr.loan.domain.entities.EmployeeLoan;
import com.atlashub.hr.loan.domain.exceptions.LoanAlreadyActiveException;
import com.atlashub.hr.loan.domain.repositories.EmployeeLoanRepository;
import com.atlashub.hr.loan.domain.valueobject.LoanStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplyForLoanHandlerTest {

    private EmployeeLoanRepository repository;
    private ApplyForLoanHandler handler;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(EmployeeLoanRepository.class);
        handler = new ApplyForLoanHandler(repository);
    }

    @Test
    @DisplayName("Should apply for loan when no active/pending loan exists")
    void shouldApplyForLoanSuccessfully() {
        when(repository.findByEmployeeIdAndStatusIn(eq(100L), any()))
                .thenReturn(List.of());

        Money principal = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        Money deduction = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);

        EmployeeLoan saved = EmployeeLoan.create(1L, 100L, 10L, principal, deduction);
        when(repository.save(any(EmployeeLoan.class))).thenReturn(saved);

        ApplyForLoanResponse response = handler.execute(new ApplyForLoanCommand(
                100L,
                10L,
                principal,
                deduction
        ));

        assertThat(response).isNotNull();
        assertThat(response.loanId()).isEqualTo(1L);
        verify(repository).save(any(EmployeeLoan.class));
    }

    @Test
    @DisplayName("Should throw LoanAlreadyActiveException when active or pending loan exists")
    void shouldThrowIfLoanAlreadyActive() {
        Money principal = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        Money deduction = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);

        EmployeeLoan existing = EmployeeLoan.create(1L, 100L, 10L, principal, deduction);
        when(repository.findByEmployeeIdAndStatusIn(eq(100L), any()))
                .thenReturn(List.of(existing));

        assertThrows(LoanAlreadyActiveException.class, () ->
                handler.execute(new ApplyForLoanCommand(100L, 10L, principal, deduction))
        );
    }
}
