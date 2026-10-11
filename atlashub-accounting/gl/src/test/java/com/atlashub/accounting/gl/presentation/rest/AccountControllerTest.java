package com.atlashub.accounting.gl.presentation.rest;

import com.atlashub.accounting.gl.application.command.CreateAccount.CreateAccountCommand;
import com.atlashub.accounting.gl.application.command.CreateAccount.CreateAccountHandler;
import com.atlashub.accounting.gl.application.command.CreateAccount.CreateAccountResult;
import com.atlashub.accounting.gl.application.command.DeactivateAccount.DeactivateAccountCommand;
import com.atlashub.accounting.gl.application.command.DeactivateAccount.DeactivateAccountHandler;
import com.atlashub.accounting.gl.application.command.DeactivateAccount.DeactivateAccountResult;
import com.atlashub.accounting.gl.application.query.GetAccountById.AccountResult;
import com.atlashub.accounting.gl.application.query.GetAccountById.GetAccountByIdHandler;
import com.atlashub.accounting.gl.application.query.GetAccountById.GetAccountByIdQuery;
import com.atlashub.accounting.gl.application.query.ListAccounts.AccountSummaryResult;
import com.atlashub.accounting.gl.application.query.ListAccounts.ListAccountsHandler;
import com.atlashub.accounting.gl.application.query.ListAccounts.ListAccountsQuery;
import com.atlashub.accounting.gl.domain.valueobject.AccountType;
import com.atlashub.accounting.gl.presentation.dto.AccountResponse;
import com.atlashub.accounting.gl.presentation.dto.CreateAccountRequest;
import com.atlashub.accounting.gl.presentation.dto.CreateAccountResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountControllerTest {

    private CreateAccountHandler createAccountHandler;
    private DeactivateAccountHandler deactivateAccountHandler;
    private GetAccountByIdHandler getAccountByIdHandler;
    private ListAccountsHandler listAccountsHandler;
    private AccountController controller;

    private final AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            1001L, 10L, "TEST", "session-1", "token-1", ZonedDateTime.now());

    @BeforeEach
    void setUp() {
        createAccountHandler = mock(CreateAccountHandler.class);
        deactivateAccountHandler = mock(DeactivateAccountHandler.class);
        getAccountByIdHandler = mock(GetAccountByIdHandler.class);
        listAccountsHandler = mock(ListAccountsHandler.class);
        controller = new AccountController(
                createAccountHandler, deactivateAccountHandler, getAccountByIdHandler, listAccountsHandler);
    }

    @Test
    void createAccount_delegatesToHandlerWithActiveOrg() {
        CreateAccountRequest request = new CreateAccountRequest(
                "1000", "Operating Cash", AccountType.ASSET, null, false);
        CreateAccountResult result = new CreateAccountResult(
                1L, 10L, "1000", "Operating Cash", AccountType.ASSET, null, false, true, ZonedDateTime.now());

        when(createAccountHandler.execute(any(CreateAccountCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<CreateAccountResponse>> response = controller.createAccount(principal, request);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals("1000", response.getBody().data().code());
        verify(createAccountHandler).execute(new CreateAccountCommand(
                10L, "1000", "Operating Cash", AccountType.ASSET, null, false));
    }

    @Test
    void getAccountById_delegatesToHandler() {
        AccountResult result = new AccountResult(
                1L, 10L, "1000", "Operating Cash", AccountType.ASSET, null, false, true, ZonedDateTime.now());

        when(getAccountByIdHandler.execute(any(GetAccountByIdQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<AccountResponse>> response = controller.getAccountById(principal, 1L);

        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().data().id());
        verify(getAccountByIdHandler).execute(new GetAccountByIdQuery(1L, 10L));
    }

    @Test
    void listAccounts_delegatesToHandler() {
        AccountSummaryResult summary = new AccountSummaryResult(
                1L, 10L, "1000", "Operating Cash", AccountType.ASSET, null, false, true, ZonedDateTime.now());

        when(listAccountsHandler.execute(any(ListAccountsQuery.class))).thenReturn(List.of(summary));

        ResponseEntity<ApiResponse<List<AccountResponse>>> response = controller.listAccounts(principal);

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().data().size());
        verify(listAccountsHandler).execute(new ListAccountsQuery(10L));
    }

    @Test
    void deactivateAccount_delegatesToHandler() {
        DeactivateAccountResult result = new DeactivateAccountResult(1L, false);
        when(deactivateAccountHandler.execute(any(DeactivateAccountCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<Void>> response = controller.deactivateAccount(principal, 1L);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        verify(deactivateAccountHandler).execute(new DeactivateAccountCommand(1L, 10L));
    }
}
