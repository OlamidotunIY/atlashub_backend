package com.atlashub.hr.payroll.application.command.MarkPayrollDisbursed;

import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.exceptions.PayrollRunNotFoundException;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarkPayrollDisbursedHandlerTest {

    private PayrollRunRepository payrollRunRepository;
    private MarkPayrollDisbursedHandler handler;

    @BeforeEach
    void setUp() {
        payrollRunRepository = Mockito.mock(PayrollRunRepository.class);
        handler = new MarkPayrollDisbursedHandler(payrollRunRepository);
    }

    @Test
    @DisplayName("Should mark payroll disbursed")
    void shouldMarkDisbursed() {
        PayrollRun run = PayrollRun.create(10L, 1L, YearMonth.of(2026, 9), 500L, List.of());
        run.submitForApproval(500L);
        run.approve(600L);
        run.markProcessing();

        when(payrollRunRepository.findById(10L)).thenReturn(Optional.of(run));

        handler.execute(new MarkPayrollDisbursedCommand(10L));

        assertThat(run.getStatus()).isEqualTo(PayrollStatus.DISBURSED);
        verify(payrollRunRepository).save(run);
    }

    @Test
    @DisplayName("Should throw PayrollRunNotFoundException when run does not exist")
    void shouldThrowIfNotFound() {
        when(payrollRunRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(PayrollRunNotFoundException.class, () ->
                handler.execute(new MarkPayrollDisbursedCommand(10L))
        );
    }
}
