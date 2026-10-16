package com.atlashub.hr.payroll.application.command.InitiatePayroll;

import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.exceptions.DuplicatePayrollPeriodException;
import com.atlashub.hr.payroll.domain.exceptions.InsufficientApproversException;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.hr.payroll.domain.services.PayrollCalculationService;
import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import com.atlashub.shared.application.port.MembershipQueryPort;
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

class InitiatePayrollHandlerTest {

    private PayrollRunRepository payrollRunRepository;
    private MembershipQueryPort membershipQueryPort;
    private PayrollCalculationService payrollCalculationService;
    private InitiatePayrollHandler handler;

    @BeforeEach
    void setUp() {
        payrollRunRepository = Mockito.mock(PayrollRunRepository.class);
        membershipQueryPort = Mockito.mock(MembershipQueryPort.class);
        payrollCalculationService = Mockito.mock(PayrollCalculationService.class);

        handler = new InitiatePayrollHandler(
                payrollRunRepository,
                membershipQueryPort,
                payrollCalculationService
        );
    }

    @Test
    @DisplayName("Should initiate payroll when approvers >= 2 and period is fresh")
    void shouldInitiatePayrollSuccessfully() {
        when(membershipQueryPort.countMembersWithPermission(1L, "hr:payroll:approve")).thenReturn(2);
        when(payrollRunRepository.findByOrganizationIdAndPeriod(1L, YearMonth.of(2026, 9)))
                .thenReturn(Optional.empty());
        when(payrollCalculationService.calculatePayroll(1L, YearMonth.of(2026, 9))).thenReturn(List.of());

        PayrollRun saved = PayrollRun.create(10L, 1L, YearMonth.of(2026, 9), 500L, List.of());
        when(payrollRunRepository.save(any(PayrollRun.class))).thenReturn(saved);

        InitiatePayrollResponse response = handler.execute(new InitiatePayrollCommand(
                1L,
                YearMonth.of(2026, 9),
                500L
        ));

        assertThat(response).isNotNull();
        assertThat(response.payrollRunId()).isEqualTo(10L);
        verify(payrollRunRepository).save(any(PayrollRun.class));
    }

    @Test
    @DisplayName("Should throw InsufficientApproversException when approvers < 2")
    void shouldThrowIfInsufficientApprovers() {
        when(membershipQueryPort.countMembersWithPermission(1L, "hr:payroll:approve")).thenReturn(1);

        assertThrows(InsufficientApproversException.class, () ->
                handler.execute(new InitiatePayrollCommand(1L, YearMonth.of(2026, 9), 500L))
        );
    }

    @Test
    @DisplayName("Should throw DuplicatePayrollPeriodException when payroll for period already exists")
    void shouldThrowIfDuplicatePeriod() {
        when(membershipQueryPort.countMembersWithPermission(1L, "hr:payroll:approve")).thenReturn(2);

        PayrollRun existing = PayrollRun.create(10L, 1L, YearMonth.of(2026, 9), 500L, List.of());
        when(payrollRunRepository.findByOrganizationIdAndPeriod(1L, YearMonth.of(2026, 9)))
                .thenReturn(Optional.of(existing));

        assertThrows(DuplicatePayrollPeriodException.class, () ->
                handler.execute(new InitiatePayrollCommand(1L, YearMonth.of(2026, 9), 500L))
        );
    }
}
