package com.atlashub.pay.txquery.presentation.rest;

import com.atlashub.pay.txquery.application.queries.GetTransactionByReference.GetTransactionByReferenceHandler;
import com.atlashub.pay.txquery.application.queries.GetTransactionDetails.GetTransactionDetailsHandler;
import com.atlashub.pay.txquery.application.queries.GetTransactionVolume.GetTransactionVolumeHandler;
import com.atlashub.pay.txquery.application.queries.ListTransactions.ListTransactionsHandler;
import com.atlashub.pay.txquery.application.queries.ListTransactions.ListTransactionsQuery;
import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.pay.txquery.domain.valueobject.TransactionType;
import com.atlashub.pay.txquery.presentation.dto.TransactionResponseMapper;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialTransactionControllerTest {
    @Mock private ListTransactionsHandler listHandler;
    @Mock private GetTransactionDetailsHandler detailsHandler;
    @Mock private GetTransactionByReferenceHandler referenceHandler;
    @Mock private GetTransactionVolumeHandler volumeHandler;
    @Mock private TransactionResponseMapper mapper;
    @InjectMocks private FinancialTransactionController controller;

    @Test
    void scopesAdvancedSearchToAuthenticatedOrganizationAndEnvironment() {
        when(listHandler.execute(any())).thenReturn(new PageResult<>(List.of(), 1, 25, 31, 2));
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                7L, 42L, "LIVE", "session", "token", ZonedDateTime.now().plusHours(1));

        var response = controller.list(principal, TransactionType.CHARGE, TransactionStatus.SUCCESSFUL,
                null, "card", "paystack", "COMMERCE", "ORDER-1", "CUSTOMER", "CUST-1", 9L,
                "NGN", "PAY-1", "customer", new BigDecimal("100"), new BigDecimal("5000"),
                ZonedDateTime.parse("2026-10-01T00:00:00Z"), ZonedDateTime.parse("2026-11-01T00:00:00Z"),
                1, 25, "asc");

        assertEquals(31, response.getBody().data().totalElements());
        ArgumentCaptor<ListTransactionsQuery> query = ArgumentCaptor.forClass(ListTransactionsQuery.class);
        verify(listHandler).execute(query.capture());
        assertEquals(42L, query.getValue().organizationId());
        assertEquals("LIVE", query.getValue().environment().name());
        assertEquals(TransactionType.CHARGE, query.getValue().type());
        assertEquals("CUST-1", query.getValue().partyReferenceId());
        assertEquals(1, query.getValue().page());
        assertEquals(25, query.getValue().size());
    }
}
