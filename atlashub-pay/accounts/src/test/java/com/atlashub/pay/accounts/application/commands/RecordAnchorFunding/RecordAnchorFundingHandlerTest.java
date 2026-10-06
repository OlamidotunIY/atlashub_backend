package com.atlashub.pay.accounts.application.commands.RecordAnchorFunding;

import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.domain.events.ReservedAccountFundedEvent;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.ReservedAccountRepository;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RecordAnchorFundingHandlerTest {
    @Test
    void records_reserved_account_funding_as_a_domain_event() {
        ReservedAccountRepository reservedRepository = mock(ReservedAccountRepository.class);
        BusinessDepositAccountRepository depositRepository = mock(BusinessDepositAccountRepository.class);
        ReservedAccount account = ReservedAccount.request(1L, 2L, ApiEnvironment.TEST,
                ReservedAccountOwnerType.CUSTOMER, "customer-1", 3L, "anchor-sub",
                "ninepsb", "request-1", CurrencyCode.NGN);
        account.markSubmitted("anchor-reserved", "anchor-customer");
        account.pullDomainEvents();
        when(reservedRepository.findByAnchorReservedAccountIdAndEnvironment(
                "anchor-reserved", ApiEnvironment.TEST)).thenReturn(Optional.of(account));

        new RecordAnchorFundingHandler(reservedRepository, depositRepository).execute(
                new RecordAnchorFundingCommand("TEST", "anchor-reserved", null, "transfer-1",
                        new BigDecimal("1000"), "NGN", "Ada", "090405", ZonedDateTime.now()));

        assertTrue(account.peekDomainEvents().stream().anyMatch(ReservedAccountFundedEvent.class::isInstance));
        verify(reservedRepository).save(account);
        verifyNoInteractions(depositRepository);
    }
}
