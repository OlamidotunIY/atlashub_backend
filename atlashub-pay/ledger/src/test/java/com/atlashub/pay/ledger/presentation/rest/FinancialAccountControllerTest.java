package com.atlashub.pay.ledger.presentation.rest;

import com.atlashub.pay.ledger.application.commands.CreateBusinessAccount.CreateBusinessAccountHandler;
import com.atlashub.pay.ledger.application.queries.FinancialAccountResults.BankAccountResult;
import com.atlashub.pay.ledger.application.queries.FinancialAccountResults.FinancialAccountResult;
import com.atlashub.pay.ledger.application.queries.GetFinancialAccount.GetFinancialAccountHandler;
import com.atlashub.pay.ledger.application.queries.GetFinancialAccount.GetFinancialAccountQuery;
import com.atlashub.pay.ledger.application.queries.ListFinancialAccounts.ListFinancialAccountsHandler;
import com.atlashub.pay.ledger.application.queries.ListFinancialAccounts.ListFinancialAccountsQuery;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialAccountControllerTest {
    @Mock private ListFinancialAccountsHandler listHandler;
    @Mock private GetFinancialAccountHandler getHandler;
    @Mock private CreateBusinessAccountHandler createHandler;
    @InjectMocks private FinancialAccountController controller;

    @Test
    void listReturnsSummariesWithoutBankAccountContract() {
        ZonedDateTime now = ZonedDateTime.now();
        when(listHandler.execute(any(ListFinancialAccountsQuery.class))).thenReturn(List.of(
                new FinancialAccountResult(1L, "AtlasHub Ltd", "Operating Account", "OPERATING", "BUSINESS",
                        "NGN", "ACTIVE", false, Set.of(), new BigDecimal("5000"), new BigDecimal("4000"),
                        new BigDecimal("1000"), now, null, now)));
        AuthenticatedPrincipal principal = principal();

        var response = controller.list(principal);

        assertEquals(1, response.getBody().data().size());
        assertEquals("Operating Account", response.getBody().data().getFirst().name());
        assertEquals("AtlasHub Ltd", response.getBody().data().getFirst().businessName());
        assertEquals(new BigDecimal("4000"), response.getBody().data().getFirst().availableBalance());
        ArgumentCaptor<ListFinancialAccountsQuery> query = ArgumentCaptor.forClass(ListFinancialAccountsQuery.class);
        verify(listHandler).execute(query.capture());
        assertEquals(42L, query.getValue().organizationId());
        assertEquals("LIVE", query.getValue().environment());
    }

    @Test
    void getReturnsBankAccountForIndividualOperatingAccount() {
        ZonedDateTime now = ZonedDateTime.now();
        BankAccountResult bank = new BankAccountResult(
                50L, "AtlasHub Ltd", "******1234", "Anchor MFB", "090270", "NGN", "ACTIVE", now);
        when(getHandler.execute(any(GetFinancialAccountQuery.class))).thenReturn(
                new FinancialAccountResult(1L, "AtlasHub Ltd", "Operating Account", "OPERATING", "BUSINESS",
                        "NGN", "ACTIVE", false, Set.of(), new BigDecimal("5000"), new BigDecimal("4000"),
                        new BigDecimal("1000"), now, bank, now));

        var response = controller.get(principal(), 1L);

        assertNotNull(response.getBody().data().bankAccount());
        assertEquals("******1234", response.getBody().data().bankAccount().maskedAccountNumber());
        ArgumentCaptor<GetFinancialAccountQuery> query = ArgumentCaptor.forClass(GetFinancialAccountQuery.class);
        verify(getHandler).execute(query.capture());
        assertEquals(42L, query.getValue().organizationId());
        assertEquals(1L, query.getValue().accountId());
    }

    private AuthenticatedPrincipal principal() {
        return new AuthenticatedPrincipal(7L, 42L, "LIVE", "session", "token",
                ZonedDateTime.now().plusHours(1));
    }
}
