package com.atlashub.accounting.gl.application.query.GetAccountById;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.accounting.gl.domain.exceptions.AccountNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.AccountRepository;
import com.atlashub.accounting.gl.domain.valueobject.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAccountByIdHandlerTest {

    private AccountRepository accountRepository;
    private GetAccountByIdHandler handler;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        handler = new GetAccountByIdHandler(accountRepository);
    }

    @Test
    void execute_returnsAccountResultSuccessfully() {
        Account account = Account.create(101L, 10L, "1000", "Operating Cash", AccountType.ASSET, null, false);
        when(accountRepository.findById(101L)).thenReturn(Optional.of(account));

        AccountResult result = handler.execute(new GetAccountByIdQuery(101L, 10L));

        assertNotNull(result);
        assertEquals(101L, result.id());
        assertEquals(10L, result.organizationId());
        assertEquals("1000", result.code());
        assertEquals("Operating Cash", result.name());
        assertEquals(AccountType.ASSET, result.type());
    }

    @Test
    void execute_accountNotFound_throwsAccountNotFoundException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> handler.execute(new GetAccountByIdQuery(999L, 10L)));
    }

    @Test
    void execute_wrongOrganization_throwsAccountNotFoundException() {
        Account account = Account.create(101L, 20L, "1000", "Operating Cash", AccountType.ASSET, null, false);
        when(accountRepository.findById(101L)).thenReturn(Optional.of(account));

        assertThrows(AccountNotFoundException.class,
                () -> handler.execute(new GetAccountByIdQuery(101L, 10L)));
    }
}
