package com.atlashub.hr.payroll.application.query.GetPayslip;

import com.atlashub.hr.payroll.domain.entities.Payslip;
import com.atlashub.hr.payroll.domain.repositories.PayslipRepository;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class GetPayslipHandlerTest {

    private PayslipRepository payslipRepository;
    private GetPayslipHandler handler;

    @BeforeEach
    void setUp() {
        payslipRepository = Mockito.mock(PayslipRepository.class);
        handler = new GetPayslipHandler(payslipRepository);
    }

    @Test
    @DisplayName("Should get payslip by ID successfully")
    void shouldGetPayslipById() {
        Payslip payslip = Payslip.create(
                10L, 1L, 100L, "John Doe",
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                List.of()
        );
        when(payslipRepository.findById(10L)).thenReturn(Optional.of(payslip));

        PayslipResult result = handler.execute(new GetPayslipQuery(10L));

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.employeeName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should throw NotFoundException when payslip does not exist")
    void shouldThrowIfNotFound() {
        when(payslipRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                handler.execute(new GetPayslipQuery(10L))
        );
    }
}
