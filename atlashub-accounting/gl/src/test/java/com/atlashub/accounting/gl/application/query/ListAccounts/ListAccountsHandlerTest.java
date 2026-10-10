package com.atlashub.accounting.gl.application.query.ListAccounts;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.accounting.gl.domain.repositories.AccountRepository;
import com.atlashub.accounting.gl.domain.valueobject.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListAccountsHandlerTest {

    private AccountRepository accountRepository;
    private ListAccountsHandler handler;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        handler = new ListAccountsHandler(accountRepository);
    }

    @Test
    void execute_returnsListOfAccounts() {
        Account acc1 = Account.create(1L, 10L, "1000", "Cash", AccountType.ASSET, null, false);
        Account acc2 = Account.create(2L, 10L, "2000", "Payable", AccountType.LIABILITY, null, false);

        when(accountRepository.findByOrganizationId(10L)).thenReturn(List.of(acc1, acc2));

        List<AccountSummaryResult> results = handler.execute(new ListAccountsQuery(10L));

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("1000", results.get(0).code());
        assertEquals("2000", results.get(1).code());
    }

    @Test
    void execute_emptyList_returnsEmpty() {
        when(accountRepository.findByOrganizationId(10L)).thenReturn(List.of());

        List<AccountSummaryResult> results = handler.execute(new ListAccountsQuery(10L));

        assertNotNull(results);
        assertEquals(0, results.size());
    }
}
