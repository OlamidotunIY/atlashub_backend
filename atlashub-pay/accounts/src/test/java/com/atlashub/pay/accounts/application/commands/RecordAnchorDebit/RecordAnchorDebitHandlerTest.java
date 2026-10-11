package com.atlashub.pay.accounts.application.commands.RecordAnchorDebit;

import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.events.OrganizationAccountDebitedEvent;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecordAnchorDebitHandlerTest {
    @Test
    void records_completed_debit_with_masked_recipient_account() {
        BusinessDepositAccountRepository repository = mock(BusinessDepositAccountRepository.class);
        BusinessDepositAccount account = BusinessDepositAccount.request(
                1L, 2L, ApiEnvironment.LIVE, 3L, "customer", CurrencyCode.NGN);
        when(repository.findByAnchorAccountIdAndEnvironment("anchor-account", ApiEnvironment.LIVE))
                .thenReturn(Optional.of(account));

        new RecordAnchorDebitHandler(repository).execute(new RecordAnchorDebitCommand(
                "LIVE", "anchor-account", "anchor-transfer", "business-reference",
                new BigDecimal("3000"), "NGN", "Ada Vendor", "1234567890", "090405",
                "Supplier payment", false, ZonedDateTime.now()));

        OrganizationAccountDebitedEvent event = (OrganizationAccountDebitedEvent) account.peekDomainEvents().getFirst();
        assertEquals("******7890", event.payload().maskedRecipientAccountNumber());
        assertTrue(event.payload().completedAt() != null);
        verify(repository).save(account);
    }
}
