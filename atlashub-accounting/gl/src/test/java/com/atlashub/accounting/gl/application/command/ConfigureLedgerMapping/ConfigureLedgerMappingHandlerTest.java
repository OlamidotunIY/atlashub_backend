package com.atlashub.accounting.gl.application.command.ConfigureLedgerMapping;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.accounting.gl.domain.entities.LedgerAccountMapping;
import com.atlashub.accounting.gl.domain.exceptions.AccountNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.AccountRepository;
import com.atlashub.accounting.gl.domain.repositories.LedgerAccountMappingRepository;
import com.atlashub.accounting.gl.domain.valueobject.AccountType;
import com.atlashub.accounting.gl.domain.valueobject.SourceSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigureLedgerMappingHandlerTest {

    private LedgerAccountMappingRepository mappingRepository;
    private AccountRepository accountRepository;
    private ConfigureLedgerMappingHandler handler;

    @BeforeEach
    void setUp() {
        mappingRepository = mock(LedgerAccountMappingRepository.class);
        accountRepository = mock(AccountRepository.class);
        handler = new ConfigureLedgerMappingHandler(mappingRepository, accountRepository);
    }

    @Test
    void execute_createsNewMappingWhenNoneExists() {
        Account debitAccount = Account.create(1L, 10L, "1000", "Cash", AccountType.ASSET, null, false);
        Account creditAccount = Account.create(2L, 10L, "4000", "Revenue", AccountType.REVENUE, null, false);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(debitAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(creditAccount));
        when(mappingRepository.findByOrganizationIdAndSourceSystem(10L, SourceSystem.COMMERCE_CHECKOUT)).thenReturn(Optional.empty());
        when(mappingRepository.nextIdentity()).thenReturn(50L);
        when(mappingRepository.save(any(LedgerAccountMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConfigureLedgerMappingCommand command = new ConfigureLedgerMappingCommand(
                10L,
                SourceSystem.COMMERCE_CHECKOUT,
                1L,
                2L,
                "Checkout payment mapping"
        );

        ConfigureLedgerMappingResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(50L, result.id());
        assertEquals(10L, result.organizationId());
        assertEquals(SourceSystem.COMMERCE_CHECKOUT, result.sourceSystem());
        assertEquals(1L, result.debitAccountId());
        assertEquals(2L, result.creditAccountId());
        verify(mappingRepository).save(any(LedgerAccountMapping.class));
    }

    @Test
    void execute_updatesExistingMapping() {
        Account debitAccount = Account.create(1L, 10L, "1000", "Cash", AccountType.ASSET, null, false);
        Account creditAccount = Account.create(2L, 10L, "4000", "Revenue", AccountType.REVENUE, null, false);

        LedgerAccountMapping existing = LedgerAccountMapping.create(
                50L,
                10L,
                SourceSystem.COMMERCE_CHECKOUT,
                10L,
                20L,
                "Old mapping"
        );

        when(accountRepository.findById(1L)).thenReturn(Optional.of(debitAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(creditAccount));
        when(mappingRepository.findByOrganizationIdAndSourceSystem(10L, SourceSystem.COMMERCE_CHECKOUT)).thenReturn(Optional.of(existing));
        when(mappingRepository.save(any(LedgerAccountMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConfigureLedgerMappingCommand command = new ConfigureLedgerMappingCommand(
                10L,
                SourceSystem.COMMERCE_CHECKOUT,
                1L,
                2L,
                "Updated mapping"
        );

        ConfigureLedgerMappingResult result = handler.execute(command);

        assertEquals(50L, result.id());
        assertEquals(1L, result.debitAccountId());
        assertEquals(2L, result.creditAccountId());
        assertEquals("Updated mapping", result.description());
        verify(mappingRepository).save(existing);
    }

    @Test
    void execute_debitAccountNotFound_throwsAccountNotFoundException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        ConfigureLedgerMappingCommand command = new ConfigureLedgerMappingCommand(
                10L,
                SourceSystem.COMMERCE_CHECKOUT,
                999L,
                2L,
                "Mapping"
        );

        assertThrows(AccountNotFoundException.class, () -> handler.execute(command));
    }

    @Test
    void execute_creditAccountNotFound_throwsAccountNotFoundException() {
        Account debitAccount = Account.create(1L, 10L, "1000", "Cash", AccountType.ASSET, null, false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(debitAccount));
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        ConfigureLedgerMappingCommand command = new ConfigureLedgerMappingCommand(
                10L,
                SourceSystem.COMMERCE_CHECKOUT,
                1L,
                999L,
                "Mapping"
        );

        assertThrows(AccountNotFoundException.class, () -> handler.execute(command));
    }
}
