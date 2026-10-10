package com.atlashub.accounting.gl.application.command.CreateAccount;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.accounting.gl.domain.exceptions.DuplicateAccountCodeException;
import com.atlashub.accounting.gl.domain.repositories.AccountRepository;
import com.atlashub.accounting.gl.domain.valueobject.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateAccountHandlerTest {

    private AccountRepository accountRepository;
    private CreateAccountHandler handler;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        handler = new CreateAccountHandler(accountRepository);
    }

    @Test
    void execute_createsAndSavesAccount() {
        CreateAccountCommand command = new CreateAccountCommand(
                10L,
                "1000",
                "Operating Cash",
                AccountType.ASSET,
                null,
                false
        );

        when(accountRepository.findByOrganizationIdAndCode(10L, "1000")).thenReturn(Optional.empty());
        when(accountRepository.nextIdentity()).thenReturn(101L);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateAccountResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(101L, result.id());
        assertEquals(10L, result.organizationId());
        assertEquals("1000", result.code());
        assertEquals("Operating Cash", result.name());
        assertEquals(AccountType.ASSET, result.type());
        assertTrue(result.isActive());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account savedAccount = captor.getValue();
        assertEquals(101L, savedAccount.getId());
        assertEquals("1000", savedAccount.getCode());
    }

    @Test
    void execute_duplicateCode_throwsException() {
        CreateAccountCommand command = new CreateAccountCommand(
                10L,
                "1000",
                "Operating Cash",
                AccountType.ASSET,
                null,
                false
        );

        Account existing = Account.create(100L, 10L, "1000", "Existing", AccountType.ASSET, null, false);
        when(accountRepository.findByOrganizationIdAndCode(10L, "1000")).thenReturn(Optional.of(existing));

        assertThrows(DuplicateAccountCodeException.class, () -> handler.execute(command));
    }
}
