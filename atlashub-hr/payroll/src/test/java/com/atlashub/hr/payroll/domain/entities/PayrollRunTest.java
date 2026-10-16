package com.atlashub.hr.payroll.domain.entities;

import com.atlashub.hr.payroll.domain.exceptions.InvalidPayrollStateException;
import com.atlashub.hr.payroll.domain.exceptions.SelfApprovalNotAllowedException;
import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayrollRunTest {

    @Test
    @DisplayName("Should create PayrollRun and compute totals from payslips")
    void shouldCreatePayrollRunAndComputeTotals() {
        Payslip payslip1 = Payslip.create(
                1L, 10L, 100L, "John Doe",
                Money.of(new BigDecimal("500000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("70000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("430000.00"), CurrencyCode.NGN),
                List.of()
        );
        Payslip payslip2 = Payslip.create(
                2L, 10L, 101L, "Jane Doe",
                Money.of(new BigDecimal("300000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("30000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("40000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("260000.00"), CurrencyCode.NGN),
                List.of()
        );

        PayrollRun run = PayrollRun.create(
                10L,
                1L,
                YearMonth.of(2026, 9),
                500L,
                List.of(payslip1, payslip2)
        );

        assertThat(run.getId()).isEqualTo(10L);
        assertThat(run.getOrganizationId()).isEqualTo(1L);
        assertThat(run.getPeriod()).isEqualTo(YearMonth.of(2026, 9));
        assertThat(run.getInitiatedBy()).isEqualTo(500L);
        assertThat(run.getStatus()).isEqualTo(PayrollStatus.DRAFT);
        assertThat(run.getTotalGross().amount()).isEqualByComparingTo(new BigDecimal("800000.0000"));
        assertThat(run.getTotalDeductions().amount()).isEqualByComparingTo(new BigDecimal("110000.0000"));
        assertThat(run.getTotalNet().amount()).isEqualByComparingTo(new BigDecimal("690000.0000"));
    }

    @Test
    @DisplayName("Should throw when creating with null required fields")
    void shouldThrowOnNullFields() {
        assertThrows(InvalidPayrollStateException.class, () ->
                PayrollRun.create(1L, null, YearMonth.of(2026, 9), 500L, List.of()));

        assertThrows(InvalidPayrollStateException.class, () ->
                PayrollRun.create(1L, 1L, null, 500L, List.of()));

        assertThrows(InvalidPayrollStateException.class, () ->
                PayrollRun.create(1L, 1L, YearMonth.of(2026, 9), null, List.of()));
    }

    @Test
    @DisplayName("Should submit draft payroll for approval")
    void shouldSubmitForApproval() {
        PayrollRun run = PayrollRun.create(1L, 1L, YearMonth.of(2026, 9), 500L, List.of());
        run.submitForApproval(500L);

        assertThat(run.getStatus()).isEqualTo(PayrollStatus.PENDING_APPROVAL);
        assertThat(run.peekDomainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw if submitting non-draft payroll")
    void shouldThrowIfSubmittingNonDraft() {
        PayrollRun run = PayrollRun.create(1L, 1L, YearMonth.of(2026, 9), 500L, List.of());
        run.submitForApproval(500L);

        assertThrows(InvalidPayrollStateException.class, () -> run.submitForApproval(500L));
    }

    @Test
    @DisplayName("Maker-Checker: Should approve when approver is different from initiator")
    void shouldApproveWhenCheckerIsDifferent() {
        PayrollRun run = PayrollRun.create(1L, 1L, YearMonth.of(2026, 9), 500L, List.of());
        run.submitForApproval(500L);

        run.approve(600L);

        assertThat(run.getStatus()).isEqualTo(PayrollStatus.APPROVED);
        assertThat(run.getApprovedBy()).isEqualTo(600L);
        assertThat(run.getApprovedAt()).isNotNull();
    }

    @Test
    @DisplayName("Maker-Checker: Should reject self-approval with SelfApprovalNotAllowedException")
    void shouldRejectSelfApproval() {
        PayrollRun run = PayrollRun.create(1L, 1L, YearMonth.of(2026, 9), 500L, List.of());
        run.submitForApproval(500L);

        assertThrows(SelfApprovalNotAllowedException.class, () -> run.approve(500L));
    }

    @Test
    @DisplayName("Should throw if approving non-pending payroll")
    void shouldThrowIfApprovingNonPending() {
        PayrollRun run = PayrollRun.create(1L, 1L, YearMonth.of(2026, 9), 500L, List.of());

        assertThrows(InvalidPayrollStateException.class, () -> run.approve(600L));
    }

    @Test
    @DisplayName("Should mark processing and disburse payroll")
    void shouldMarkProcessingAndDisburse() {
        Payslip payslip = Payslip.create(
                1L, 1L, 100L, "John",
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                List.of()
        );
        PayrollRun run = PayrollRun.create(1L, 1L, YearMonth.of(2026, 9), 500L, List.of(payslip));
        run.submitForApproval(500L);
        run.approve(600L);

        run.markProcessing();
        assertThat(run.getStatus()).isEqualTo(PayrollStatus.PROCESSING);

        run.markDisbursed();
        assertThat(run.getStatus()).isEqualTo(PayrollStatus.DISBURSED);
        assertThat(payslip.getStatus()).isEqualTo(com.atlashub.hr.payroll.domain.valueobject.PayslipStatus.DISBURSED);
    }

    @Test
    @DisplayName("Should expire approval and revert to draft")
    void shouldExpireApproval() {
        PayrollRun run = PayrollRun.create(1L, 1L, YearMonth.of(2026, 9), 500L, List.of());
        run.submitForApproval(500L);

        run.expire();

        assertThat(run.getStatus()).isEqualTo(PayrollStatus.DRAFT);
    }
}
