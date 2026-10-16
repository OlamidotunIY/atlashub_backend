package com.atlashub.hr.loan.domain.entities;

import com.atlashub.hr.loan.domain.exceptions.InvalidLoanStateException;
import com.atlashub.hr.loan.domain.valueobject.LoanStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmployeeLoanTest {

    @Test
    @DisplayName("Should create EmployeeLoan with valid parameters")
    void shouldCreateEmployeeLoan() {
        Money principal = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        Money deduction = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);

        EmployeeLoan loan = EmployeeLoan.create(
                1L,
                100L,
                10L,
                principal,
                deduction
        );

        assertThat(loan.getId()).isEqualTo(1L);
        assertThat(loan.getEmployeeId()).isEqualTo(100L);
        assertThat(loan.getOrganizationId()).isEqualTo(10L);
        assertThat(loan.getPrincipalAmount()).isEqualTo(principal);
        assertThat(loan.getOutstandingBalance()).isEqualTo(principal);
        assertThat(loan.getMonthlyDeduction()).isEqualTo(deduction);
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.PENDING_APPROVAL);
        assertThat(loan.getApprovedBy()).isNull();
        assertThat(loan.getStartDate()).isNull();
        assertThat(loan.getDisbursedAt()).isNull();
    }

    @Test
    @DisplayName("Should throw when creating with invalid invariants")
    void shouldThrowOnInvalidCreationInvariants() {
        Money validPrincipal = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        Money validDeduction = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);

        assertThrows(InvalidLoanStateException.class, () ->
                EmployeeLoan.create(1L, null, 10L, validPrincipal, validDeduction));

        assertThrows(InvalidLoanStateException.class, () ->
                EmployeeLoan.create(1L, 100L, null, validPrincipal, validDeduction));

        assertThrows(InvalidLoanStateException.class, () ->
                EmployeeLoan.create(1L, 100L, 10L, null, validDeduction));

        assertThrows(InvalidLoanStateException.class, () ->
                EmployeeLoan.create(1L, 100L, 10L, Money.of(BigDecimal.ZERO, CurrencyCode.NGN), validDeduction));

        assertThrows(InvalidLoanStateException.class, () ->
                EmployeeLoan.create(1L, 100L, 10L, validPrincipal, null));

        assertThrows(InvalidLoanStateException.class, () ->
                EmployeeLoan.create(1L, 100L, 10L, validPrincipal, Money.of(BigDecimal.ZERO, CurrencyCode.NGN)));
    }

    @Test
    @DisplayName("Should approve loan and register LoanApprovedEvent")
    void shouldApproveLoan() {
        EmployeeLoan loan = EmployeeLoan.create(
                1L,
                100L,
                10L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN)
        );

        LocalDate startDate = LocalDate.of(2026, 8, 1);
        loan.approve(200L, startDate);

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.APPROVED);
        assertThat(loan.getApprovedBy()).isEqualTo(200L);
        assertThat(loan.getStartDate()).isEqualTo(startDate);
        assertThat(loan.peekDomainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("Should fail approving loan when not PENDING_APPROVAL")
    void shouldFailApproveIfNotPending() {
        EmployeeLoan loan = EmployeeLoan.create(
                1L,
                100L,
                10L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN)
        );

        LocalDate startDate = LocalDate.of(2026, 8, 1);
        loan.approve(200L, startDate);

        assertThrows(InvalidLoanStateException.class, () -> loan.approve(200L, startDate));
    }

    @Test
    @DisplayName("Should disburse approved loan")
    void shouldDisburseLoan() {
        EmployeeLoan loan = EmployeeLoan.create(
                1L,
                100L,
                10L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN)
        );
        loan.approve(200L, LocalDate.of(2026, 8, 1));
        loan.disburse();

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loan.getDisbursedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should fail disburse if not APPROVED")
    void shouldFailDisburseIfNotApproved() {
        EmployeeLoan loan = EmployeeLoan.create(
                1L,
                100L,
                10L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN)
        );

        assertThrows(InvalidLoanStateException.class, loan::disburse);
    }

    @Test
    @DisplayName("Should record partial repayment correctly")
    void shouldRecordPartialRepayment() {
        EmployeeLoan loan = EmployeeLoan.create(
                1L,
                100L,
                10L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN)
        );
        loan.approve(200L, LocalDate.of(2026, 8, 1));
        loan.disburse();

        loan.recordRepayment(Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN));

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loan.getOutstandingBalance().amount()).isEqualByComparingTo(new BigDecimal("80000.0000"));
    }

    @Test
    @DisplayName("Should record full repayment and mark FULLY_REPAID")
    void shouldRecordFullRepayment() {
        EmployeeLoan loan = EmployeeLoan.create(
                1L,
                100L,
                10L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN)
        );
        loan.approve(200L, LocalDate.of(2026, 8, 1));
        loan.disburse();

        loan.recordRepayment(Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN));

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.FULLY_REPAID);
        assertThat(loan.getOutstandingBalance().amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should cancel pending loan")
    void shouldCancelPendingLoan() {
        EmployeeLoan loan = EmployeeLoan.create(
                1L,
                100L,
                10L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN)
        );

        loan.cancel();

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should mark termination pending for active loan")
    void shouldMarkTerminationPending() {
        EmployeeLoan loan = EmployeeLoan.create(
                1L,
                100L,
                10L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN)
        );
        loan.approve(200L, LocalDate.of(2026, 8, 1));
        loan.disburse();

        loan.markTerminationPending();

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.TERMINATION_PENDING);
    }
}
