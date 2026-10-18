package com.atlashub.hr.payroll.application.query.ListPayrollRuns;

import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ListPayrollRunsHandlerTest {

    private PayrollRunRepository payrollRunRepository;
    private ListPayrollRunsHandler handler;

    @BeforeEach
    void setUp() {
        payrollRunRepository = Mockito.mock(PayrollRunRepository.class);
        handler = new ListPayrollRunsHandler(payrollRunRepository);
    }

    @Test
    @DisplayName("Should list payroll runs by org and status")
    void shouldListByOrgAndStatus() {
        PayrollRun run = PayrollRun.create(10L, 1L, YearMonth.of(2026, 9), 500L, List.of());
        when(payrollRunRepository.findByOrganizationIdAndStatus(1L, PayrollStatus.DRAFT))
                .thenReturn(List.of(run));

        List<PayrollRunResult> results = handler.execute(new ListPayrollRunsQuery(1L, PayrollStatus.DRAFT));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should return empty list when orgId is null")
    void shouldReturnEmptyWhenNoOrg() {
        List<PayrollRunResult> results = handler.execute(new ListPayrollRunsQuery(null, null));
        assertThat(results).isEmpty();
    }
}
