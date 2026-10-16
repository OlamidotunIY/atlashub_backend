package com.atlashub.accounting.budget.presentation.rest;

import com.atlashub.accounting.budget.application.command.ApproveBudget.ApproveBudgetCommand;
import com.atlashub.accounting.budget.application.command.ApproveBudget.ApproveBudgetHandler;
import com.atlashub.accounting.budget.application.command.CloseBudget.CloseBudgetCommand;
import com.atlashub.accounting.budget.application.command.CloseBudget.CloseBudgetHandler;
import com.atlashub.accounting.budget.application.command.CreateBudget.CreateBudgetCommand;
import com.atlashub.accounting.budget.application.command.CreateBudget.CreateBudgetHandler;
import com.atlashub.accounting.budget.application.query.GetBudgetVariance.BudgetVarianceResult;
import com.atlashub.accounting.budget.application.query.GetBudgetVariance.GetBudgetVarianceHandler;
import com.atlashub.accounting.budget.application.query.GetBudgetVariance.GetBudgetVarianceQuery;
import com.atlashub.accounting.budget.application.query.ListBudgets.BudgetResult;
import com.atlashub.accounting.budget.application.query.ListBudgets.ListBudgetsHandler;
import com.atlashub.accounting.budget.application.query.ListBudgets.ListBudgetsQuery;
import com.atlashub.accounting.budget.domain.valueobject.BudgetStatus;
import com.atlashub.accounting.budget.presentation.dto.BudgetResponse;
import com.atlashub.accounting.budget.presentation.dto.BudgetVarianceResponse;
import com.atlashub.accounting.budget.presentation.dto.CreateBudgetRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountingBudgetControllerTest {

    private CreateBudgetHandler createBudgetHandler;
    private ApproveBudgetHandler approveBudgetHandler;
    private CloseBudgetHandler closeBudgetHandler;
    private ListBudgetsHandler listBudgetsHandler;
    private GetBudgetVarianceHandler getBudgetVarianceHandler;
    private AccountingBudgetController controller;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        createBudgetHandler = mock(CreateBudgetHandler.class);
        approveBudgetHandler = mock(ApproveBudgetHandler.class);
        closeBudgetHandler = mock(CloseBudgetHandler.class);
        listBudgetsHandler = mock(ListBudgetsHandler.class);
        getBudgetVarianceHandler = mock(GetBudgetVarianceHandler.class);

        controller = new AccountingBudgetController(
                createBudgetHandler,
                approveBudgetHandler,
                closeBudgetHandler,
                listBudgetsHandler,
                getBudgetVarianceHandler
        );

        principal = mock(AuthenticatedPrincipal.class);
        when(principal.activeOrganizationId()).thenReturn(10L);
        when(principal.userId()).thenReturn(50L);
    }

    @Test
    @DisplayName("Should create budget and return 201 Created")
    void shouldCreateBudget() {
        CreateBudgetRequest request = new CreateBudgetRequest(
                1001L,
                "Marketing Q3",
                "2026-Q3",
                new BigDecimal("2000000.00"),
                "NGN"
        );

        when(createBudgetHandler.execute(any(CreateBudgetCommand.class))).thenReturn(101L);

        ResponseEntity<ApiResponse<Long>> response = controller.createBudget(request, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(101L, response.getBody().data());

        ArgumentCaptor<CreateBudgetCommand> captor = ArgumentCaptor.forClass(CreateBudgetCommand.class);
        verify(createBudgetHandler).execute(captor.capture());
        CreateBudgetCommand cmd = captor.getValue();
        assertEquals(10L, cmd.organizationId());
        assertEquals(1001L, cmd.accountId());
        assertEquals(50L, cmd.initiatedBy());
    }

    @Test
    @DisplayName("Should approve budget and return 200 OK")
    void shouldApproveBudget() {
        ResponseEntity<ApiResponse<Void>> response = controller.approveBudget(101L, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ArgumentCaptor<ApproveBudgetCommand> captor = ArgumentCaptor.forClass(ApproveBudgetCommand.class);
        verify(approveBudgetHandler).execute(captor.capture());
        ApproveBudgetCommand cmd = captor.getValue();
        assertEquals(101L, cmd.budgetId());
        assertEquals(50L, cmd.approverId());
    }

    @Test
    @DisplayName("Should close budget and return 200 OK")
    void shouldCloseBudget() {
        ResponseEntity<ApiResponse<Void>> response = controller.closeBudget(101L, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ArgumentCaptor<CloseBudgetCommand> captor = ArgumentCaptor.forClass(CloseBudgetCommand.class);
        verify(closeBudgetHandler).execute(captor.capture());
        assertEquals(101L, captor.getValue().budgetId());
    }

    @Test
    @DisplayName("Should list budgets and return 200 OK")
    void shouldListBudgets() {
        BudgetResult result = new BudgetResult(
                101L,
                10L,
                1001L,
                "Marketing Q3",
                "2026-Q3",
                new BigDecimal("2000000.00"),
                "NGN",
                BudgetStatus.APPROVED,
                50L,
                60L,
                null
        );

        when(listBudgetsHandler.execute(any(ListBudgetsQuery.class))).thenReturn(List.of(result));

        ResponseEntity<ApiResponse<List<BudgetResponse>>> response = controller.listBudgets(BudgetStatus.APPROVED, "2026-Q3", principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<BudgetResponse> data = response.getBody().data();
        assertEquals(1, data.size());
        assertEquals("Marketing Q3", data.getFirst().name());
    }

    @Test
    @DisplayName("Should get budget variance and return 200 OK")
    void shouldGetBudgetVariance() {
        BudgetVarianceResult result = new BudgetVarianceResult(
                101L,
                1001L,
                "2026-Q3",
                new BigDecimal("2000000.00"),
                new BigDecimal("1500000.00"),
                new BigDecimal("500000.00"),
                new BigDecimal("25.00"),
                "NGN"
        );

        when(getBudgetVarianceHandler.execute(any(GetBudgetVarianceQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<BudgetVarianceResponse>> response = controller.getBudgetVariance(101L, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        BudgetVarianceResponse data = response.getBody().data();
        assertEquals(101L, data.budgetId());
        assertThat(data.variance()).isEqualByComparingTo(new BigDecimal("500000.00"));
        assertThat(data.variancePercentage()).isEqualByComparingTo(new BigDecimal("25.00"));
    }
}
