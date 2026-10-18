package com.atlashub.hr.payroll.application.query.ListPayslipsByEmployee;

import com.atlashub.hr.payroll.application.query.GetPayslip.PayslipResult;
import com.atlashub.hr.payroll.domain.entities.Payslip;
import com.atlashub.hr.payroll.domain.repositories.PayslipRepository;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ListPayslipsByEmployeeHandlerTest {

    private PayslipRepository payslipRepository;
    private ListPayslipsByEmployeeHandler handler;

    @BeforeEach
    void setUp() {
        payslipRepository = Mockito.mock(PayslipRepository.class);
        handler = new ListPayslipsByEmployeeHandler(payslipRepository);
    }

    @Test
    @DisplayName("Should list payslips for employee by year")
    void shouldListPayslipsForEmployee() {
        Payslip payslip = Payslip.create(
                10L, 1L, 100L, "John Doe",
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                List.of()
        );
        when(payslipRepository.findByEmployeeIdAndYear(100L, 2026))
                .thenReturn(List.of(payslip));

        List<PayslipResult> results = handler.execute(new ListPayslipsByEmployeeQuery(100L, 2026));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).employeeId()).isEqualTo(100L);
    }
}
