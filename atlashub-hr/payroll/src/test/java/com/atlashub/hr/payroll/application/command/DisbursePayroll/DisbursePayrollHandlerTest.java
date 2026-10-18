package com.atlashub.hr.payroll.application.command.DisbursePayroll;

import com.atlashub.shared.application.port.PayrollReserveAccountPort;
import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.entities.Payslip;
import com.atlashub.hr.payroll.domain.exceptions.InsufficientPayrollReserveException;
import com.atlashub.hr.payroll.domain.exceptions.InvalidPayrollStateException;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DisbursePayrollHandlerTest {

    private PayrollRunRepository payrollRunRepository;
    private PayrollReserveAccountPort payrollReserveAccountPort;
    private DisbursePayrollHandler handler;

    @BeforeEach
    void setUp() {
        payrollRunRepository = Mockito.mock(PayrollRunRepository.class);
        payrollReserveAccountPort = Mockito.mock(PayrollReserveAccountPort.class);
        handler = new DisbursePayrollHandler(payrollRunRepository, payrollReserveAccountPort);
    }

    @Test
    @DisplayName("Should mark processing when reserve has sufficient balance")
    void shouldDisburseSuccessfully() {
        Payslip payslip = Payslip.create(
                1L, 10L, 100L, "John",
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                List.of()
        );
        PayrollRun run = PayrollRun.create(10L, 1L, YearMonth.of(2026, 9), 500L, List.of(payslip));
        run.submitForApproval(500L);
        run.approve(600L);

        when(payrollRunRepository.findById(10L)).thenReturn(Optional.of(run));
        when(payrollReserveAccountPort.getBalance(1L))
                .thenReturn(Money.of(new BigDecimal("500000.00"), CurrencyCode.NGN));

        handler.execute(new DisbursePayrollCommand(10L));

        assertThat(run.getStatus()).isEqualTo(PayrollStatus.PROCESSING);
        verify(payrollRunRepository).save(run);
    }

    @Test
    @DisplayName("Should throw InsufficientPayrollReserveException when reserve balance is low")
    void shouldThrowIfInsufficientBalance() {
        Payslip payslip = Payslip.create(
                1L, 10L, 100L, "John",
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                List.of()
        );
        PayrollRun run = PayrollRun.create(10L, 1L, YearMonth.of(2026, 9), 500L, List.of(payslip));
        run.submitForApproval(500L);
        run.approve(600L);

        when(payrollRunRepository.findById(10L)).thenReturn(Optional.of(run));
        when(payrollReserveAccountPort.getBalance(1L))
                .thenReturn(Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN));

        assertThrows(InsufficientPayrollReserveException.class, () ->
                handler.execute(new DisbursePayrollCommand(10L))
        );
    }

    @Test
    @DisplayName("Should throw InvalidPayrollStateException when run is not APPROVED")
    void shouldThrowIfNotApproved() {
        PayrollRun run = PayrollRun.create(10L, 1L, YearMonth.of(2026, 9), 500L, List.of());

        when(payrollRunRepository.findById(10L)).thenReturn(Optional.of(run));

        assertThrows(InvalidPayrollStateException.class, () ->
                handler.execute(new DisbursePayrollCommand(10L))
        );
    }
}
