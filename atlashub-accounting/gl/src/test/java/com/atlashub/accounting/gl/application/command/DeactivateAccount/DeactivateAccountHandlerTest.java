package com.atlashub.accounting.gl.application.command.DeactivateAccount;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.accounting.gl.domain.exceptions.AccountNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.AccountRepository;
import com.atlashub.accounting.gl.domain.valueobject.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeactivateAccountHandlerTest {

    private AccountRepository accountRepository;
    private DeactivateAccountHandler handler;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        handler = new DeactivateAccountHandler(accountRepository);
    }

    @Test
    void execute_deactivatesAccountSuccessfully() {
        Account account = Account.create(101L, 10L, "1000", "Operating Cash", AccountType.ASSET, null, false);
        when(accountRepository.findById(101L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeactivateAccountResult result = handler.execute(new DeactivateAccountCommand(101L, 10L));

        assertEquals(101L, result.accountId());
        assertFalse(result.isActive());
        verify(accountRepository).save(account);
    }

    @Test
    void execute_accountNotFound_throwsException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> handler.execute(new DeactivateAccountCommand(999L, 10L)));
    }

    @Test
    void execute_wrongOrganization_throwsAccountNotFoundException() {
        Account account = Account.create(101L, 20L, "1000", "Operating Cash", AccountType.ASSET, null, false);
        when(accountRepository.findById(101L)).thenReturn(Optional.of(account));

        assertThrows(AccountNotFoundException.class,
                () -> handler.execute(new DeactivateAccountCommand(101L, 10L)));
    }
}
