package com.atlashub.hr.payroll.presentation.rest;

import com.atlashub.hr.payroll.application.command.ApprovePayroll.ApprovePayrollCommand;
import com.atlashub.hr.payroll.application.command.ApprovePayroll.ApprovePayrollHandler;
import com.atlashub.hr.payroll.application.command.InitiatePayroll.InitiatePayrollCommand;
import com.atlashub.hr.payroll.application.command.InitiatePayroll.InitiatePayrollHandler;
import com.atlashub.hr.payroll.application.command.InitiatePayroll.InitiatePayrollResponse;
import com.atlashub.hr.payroll.application.command.SubmitPayrollForApproval.SubmitPayrollForApprovalCommand;
import com.atlashub.hr.payroll.application.command.SubmitPayrollForApproval.SubmitPayrollForApprovalHandler;
import com.atlashub.hr.payroll.application.query.GetPayslip.GetPayslipHandler;
import com.atlashub.hr.payroll.application.query.GetPayslip.GetPayslipQuery;
import com.atlashub.hr.payroll.application.query.GetPayslip.PayslipResult;
import com.atlashub.hr.payroll.application.query.ListPayrollRuns.ListPayrollRunsHandler;
import com.atlashub.hr.payroll.application.query.ListPayrollRuns.ListPayrollRunsQuery;
import com.atlashub.hr.payroll.application.query.ListPayrollRuns.PayrollRunResult;
import com.atlashub.hr.payroll.application.query.ListPayslipsByEmployee.ListPayslipsByEmployeeHandler;
import com.atlashub.hr.payroll.application.query.ListPayslipsByEmployee.ListPayslipsByEmployeeQuery;
import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import com.atlashub.hr.payroll.domain.valueobject.PayslipStatus;
import com.atlashub.hr.payroll.presentation.dto.InitiatePayrollRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HrPayrollControllerTest {

    private InitiatePayrollHandler initiatePayrollHandler;
    private SubmitPayrollForApprovalHandler submitPayrollForApprovalHandler;
    private ApprovePayrollHandler approvePayrollHandler;
    private ListPayrollRunsHandler listPayrollRunsHandler;
    private GetPayslipHandler getPayslipHandler;
    private ListPayslipsByEmployeeHandler listPayslipsByEmployeeHandler;
    private HrPayrollController controller;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        initiatePayrollHandler = Mockito.mock(InitiatePayrollHandler.class);
        submitPayrollForApprovalHandler = Mockito.mock(SubmitPayrollForApprovalHandler.class);
        approvePayrollHandler = Mockito.mock(ApprovePayrollHandler.class);
        listPayrollRunsHandler = Mockito.mock(ListPayrollRunsHandler.class);
        getPayslipHandler = Mockito.mock(GetPayslipHandler.class);
        listPayslipsByEmployeeHandler = Mockito.mock(ListPayslipsByEmployeeHandler.class);

        controller = new HrPayrollController(
                initiatePayrollHandler,
                submitPayrollForApprovalHandler,
                approvePayrollHandler,
                listPayrollRunsHandler,
                getPayslipHandler,
                listPayslipsByEmployeeHandler
        );

        principal = new AuthenticatedPrincipal(
                500L,
                1L,
                "TEST",
                "session-1",
                "token-1",
                ZonedDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("POST /payroll/initiate should invoke InitiatePayrollHandler")
    void shouldInitiatePayroll() {
        InitiatePayrollRequest request = new InitiatePayrollRequest(1L, "2026-09");
        when(initiatePayrollHandler.execute(any(InitiatePayrollCommand.class)))
                .thenReturn(new InitiatePayrollResponse(10L));

        ResponseEntity<ApiResponse<com.atlashub.hr.payroll.presentation.dto.InitiatePayrollResponse>> response =
                controller.initiatePayroll(request, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().data().payrollRunId()).isEqualTo(10L);
        verify(initiatePayrollHandler).execute(new InitiatePayrollCommand(
                1L,
                YearMonth.of(2026, 9),
                500L
        ));
    }

    @Test
    @DisplayName("POST /payroll/{id}/submit should invoke SubmitPayrollForApprovalHandler")
    void shouldSubmitPayroll() {
        ResponseEntity<ApiResponse<Void>> response = controller.submitPayroll(10L, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(submitPayrollForApprovalHandler).execute(new SubmitPayrollForApprovalCommand(10L, 500L));
    }

    @Test
    @DisplayName("POST /payroll/{id}/approve should invoke ApprovePayrollHandler")
    void shouldApprovePayroll() {
        ResponseEntity<ApiResponse<Void>> response = controller.approvePayroll(10L, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(approvePayrollHandler).execute(new ApprovePayrollCommand(10L, 500L));
    }

    @Test
    @DisplayName("GET /payroll should invoke ListPayrollRunsHandler")
    void shouldListPayrollRuns() {
        PayrollRunResult run = new PayrollRunResult(
                10L,
                YearMonth.of(2026, 9),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                PayrollStatus.DRAFT,
                500L,
                null,
                null,
                ZonedDateTime.now()
        );
        when(listPayrollRunsHandler.execute(any(ListPayrollRunsQuery.class)))
                .thenReturn(List.of(run));

        ResponseEntity<ApiResponse<List<PayrollRunResult>>> response =
                controller.listPayrollRuns(null, PayrollStatus.DRAFT, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).hasSize(1);
        verify(listPayrollRunsHandler).execute(new ListPayrollRunsQuery(1L, PayrollStatus.DRAFT));
    }

    @Test
    @DisplayName("GET /payslips/{id} should invoke GetPayslipHandler")
    void shouldGetPayslip() {
        PayslipResult payslip = new PayslipResult(
                10L, 1L, 100L, "John",
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                List.of(),
                PayslipStatus.DRAFT
        );
        when(getPayslipHandler.execute(any(GetPayslipQuery.class))).thenReturn(payslip);

        ResponseEntity<ApiResponse<PayslipResult>> response = controller.getPayslip(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().id()).isEqualTo(10L);
        verify(getPayslipHandler).execute(new GetPayslipQuery(10L));
    }

    @Test
    @DisplayName("GET /payslips should invoke ListPayslipsByEmployeeHandler")
    void shouldListPayslips() {
        when(listPayslipsByEmployeeHandler.execute(any(ListPayslipsByEmployeeQuery.class)))
                .thenReturn(List.of());

        ResponseEntity<ApiResponse<List<PayslipResult>>> response = controller.listPayslips(100L, 2026);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(listPayslipsByEmployeeHandler).execute(new ListPayslipsByEmployeeQuery(100L, 2026));
    }
}
