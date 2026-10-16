package com.atlashub.hr.payroll.domain.entities;

import com.atlashub.hr.payroll.domain.valueobject.LineItemType;
import com.atlashub.hr.payroll.domain.valueobject.PayslipStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PayslipTest {

    @Test
    @DisplayName("Should create Payslip and change status")
    void shouldCreatePayslipAndChangeStatus() {
        PayslipLineItem item = PayslipLineItem.create(
                1L, 10L, "Housing Allowance",
                LineItemType.ALLOWANCE,
                Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN)
        );

        Payslip payslip = Payslip.create(
                10L,
                1L,
                100L,
                "John Doe",
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.of(new BigDecimal("120000.00"), CurrencyCode.NGN),
                List.of(item)
        );

        assertThat(payslip.getId()).isEqualTo(10L);
        assertThat(payslip.getStatus()).isEqualTo(PayslipStatus.DRAFT);
        assertThat(payslip.getBreakdown()).hasSize(1);

        payslip.markDisbursed();
        assertThat(payslip.getStatus()).isEqualTo(PayslipStatus.DISBURSED);

        payslip.markFailed();
        assertThat(payslip.getStatus()).isEqualTo(PayslipStatus.FAILED);
    }
}
