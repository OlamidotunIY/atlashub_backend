package com.atlashub.hr.loan.presentation.rest;

import com.atlashub.hr.loan.application.command.ApplyForLoan.ApplyForLoanCommand;
import com.atlashub.hr.loan.application.command.ApplyForLoan.ApplyForLoanHandler;
import com.atlashub.hr.loan.application.command.ApplyForLoan.ApplyForLoanResponse;
import com.atlashub.hr.loan.application.command.ApproveLoan.ApproveLoanCommand;
import com.atlashub.hr.loan.application.command.ApproveLoan.ApproveLoanHandler;
import com.atlashub.hr.loan.application.command.CancelLoan.CancelLoanCommand;
import com.atlashub.hr.loan.application.command.CancelLoan.CancelLoanHandler;
import com.atlashub.hr.loan.application.query.ListLoans.ListLoansHandler;
import com.atlashub.hr.loan.application.query.ListLoans.ListLoansQuery;
import com.atlashub.hr.loan.application.query.ListLoans.LoanResult;
import com.atlashub.hr.loan.domain.valueobject.LoanStatus;
import com.atlashub.hr.loan.presentation.dto.ApplyForLoanRequest;
import com.atlashub.hr.loan.presentation.dto.ApproveLoanRequest;
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
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HrLoanControllerTest {

    private ApplyForLoanHandler applyForLoanHandler;
    private ApproveLoanHandler approveLoanHandler;
    private CancelLoanHandler cancelLoanHandler;
    private ListLoansHandler listLoansHandler;
    private HrLoanController controller;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        applyForLoanHandler = Mockito.mock(ApplyForLoanHandler.class);
        approveLoanHandler = Mockito.mock(ApproveLoanHandler.class);
        cancelLoanHandler = Mockito.mock(CancelLoanHandler.class);
        listLoansHandler = Mockito.mock(ListLoansHandler.class);

        controller = new HrLoanController(
                applyForLoanHandler,
                approveLoanHandler,
                cancelLoanHandler,
                listLoansHandler
        );

        principal = new AuthenticatedPrincipal(
                100L,
                10L,
                "TEST",
                "session-1",
                "token-1",
                ZonedDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("POST /loans/apply should invoke ApplyForLoanHandler")
    void shouldApplyForLoan() {
        ApplyForLoanRequest request = new ApplyForLoanRequest(
                100L,
                null,
                new BigDecimal("100000.00"),
                new BigDecimal("10000.00"),
                "NGN"
        );
        when(applyForLoanHandler.execute(any(ApplyForLoanCommand.class)))
                .thenReturn(new ApplyForLoanResponse(1L));

        ResponseEntity<ApiResponse<com.atlashub.hr.loan.presentation.dto.ApplyForLoanResponse>> response =
                controller.applyForLoan(request, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().data().loanId()).isEqualTo(1L);
        verify(applyForLoanHandler).execute(new ApplyForLoanCommand(
                100L,
                10L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN)
        ));
    }

    @Test
    @DisplayName("GET /loans should invoke ListLoansHandler")
    void shouldListLoans() {
        LoanResult loan = new LoanResult(
                1L,
                100L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN),
                LoanStatus.PENDING_APPROVAL,
                null,
                null,
                null
        );
        when(listLoansHandler.execute(any(ListLoansQuery.class))).thenReturn(List.of(loan));

        ResponseEntity<ApiResponse<List<LoanResult>>> response =
                controller.listLoans(null, 100L, LoanStatus.PENDING_APPROVAL, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).hasSize(1);
        verify(listLoansHandler).execute(new ListLoansQuery(10L, 100L, LoanStatus.PENDING_APPROVAL));
    }

    @Test
    @DisplayName("POST /loans/{id}/approve should invoke ApproveLoanHandler")
    void shouldApproveLoan() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        ApproveLoanRequest request = new ApproveLoanRequest(startDate);

        ResponseEntity<ApiResponse<Void>> response = controller.approveLoan(1L, request, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(approveLoanHandler).execute(new ApproveLoanCommand(1L, 100L, startDate));
    }

    @Test
    @DisplayName("POST /loans/{id}/cancel should invoke CancelLoanHandler")
    void shouldCancelLoan() {
        ResponseEntity<ApiResponse<Void>> response = controller.cancelLoan(1L, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(cancelLoanHandler).execute(new CancelLoanCommand(1L));
    }
}
