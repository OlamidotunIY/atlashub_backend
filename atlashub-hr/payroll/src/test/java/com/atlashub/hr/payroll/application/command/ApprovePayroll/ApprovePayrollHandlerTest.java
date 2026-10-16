package com.atlashub.hr.payroll.application.command.ApprovePayroll;

import com.atlashub.hr.payroll.application.command.DisbursePayroll.DisbursePayrollCommand;
import com.atlashub.hr.payroll.application.command.DisbursePayroll.DisbursePayrollHandler;
import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.exceptions.PayrollRunNotFoundException;
import com.atlashub.hr.payroll.domain.exceptions.SelfApprovalNotAllowedException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApprovePayrollHandlerTest {

    private PayrollRunRepository payrollRunRepository;
    private DisbursePayrollHandler disbursePayrollHandler;
    private ApprovePayrollHandler handler;

    @BeforeEach
    void setUp() {
        payrollRunRepository = Mockito.mock(PayrollRunRepository.class);
        disbursePayrollHandler = Mockito.mock(DisbursePayrollHandler.class);
        handler = new ApprovePayrollHandler(payrollRunRepository, disbursePayrollHandler);
    }

    @Test
    @DisplayName("Should approve payroll and invoke disburse handler")
    void shouldApproveSuccessfully() {
        PayrollRun run = PayrollRun.create(10L, 1L, YearMonth.of(2026, 9), 500L, List.of());
        run.submitForApproval(500L);

        when(payrollRunRepository.findById(10L)).thenReturn(Optional.of(run));

        handler.execute(new ApprovePayrollCommand(10L, 600L));

        assertThat(run.getStatus()).isEqualTo(PayrollStatus.APPROVED);
        assertThat(run.getApprovedBy()).isEqualTo(600L);
        verify(payrollRunRepository).save(run);
        verify(disbursePayrollHandler).execute(new DisbursePayrollCommand(10L));
    }

    @Test
    @DisplayName("Maker-Checker: Should reject self-approval")
    void shouldRejectSelfApproval() {
        PayrollRun run = PayrollRun.create(10L, 1L, YearMonth.of(2026, 9), 500L, List.of());
        run.submitForApproval(500L);

        when(payrollRunRepository.findById(10L)).thenReturn(Optional.of(run));

        assertThrows(SelfApprovalNotAllowedException.class, () ->
                handler.execute(new ApprovePayrollCommand(10L, 500L))
        );
    }

    @Test
    @DisplayName("Should throw PayrollRunNotFoundException when run does not exist")
    void shouldThrowIfNotFound() {
        when(payrollRunRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(PayrollRunNotFoundException.class, () ->
                handler.execute(new ApprovePayrollCommand(10L, 600L))
        );
    }
}
