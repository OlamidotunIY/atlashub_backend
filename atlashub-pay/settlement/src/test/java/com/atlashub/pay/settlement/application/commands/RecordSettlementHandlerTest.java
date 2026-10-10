package com.atlashub.pay.settlement.application.commands;

import com.atlashub.pay.settlement.application.commands.RecordSettlement.RecordSettlementCommand;
import com.atlashub.pay.settlement.application.commands.RecordSettlement.RecordSettlementHandler;
import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.domain.repositories.SettlementCreditEvidenceRepository;
import com.atlashub.pay.settlement.domain.repositories.SettlementRepository;
import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecordSettlementHandlerTest {

    private SettlementRepository repository;
    private RecordSettlementHandler handler;
    private SettlementCreditEvidenceRepository evidenceRepository;

    @BeforeEach
    void setUp() {
        repository = mock(SettlementRepository.class);
        evidenceRepository = mock(SettlementCreditEvidenceRepository.class);
        handler = new RecordSettlementHandler(repository, evidenceRepository);
    }

    @Test
    void execute_validCommand_savesAndReturnsSettlementId() {
        when(repository.findByProviderAndEnvironmentAndProviderSettlementId(
                PaymentProvider.PAYSTACK, ApiEnvironment.LIVE, "BATCH_001")).thenReturn(Optional.empty());
        when(repository.nextIdentity()).thenReturn(999L);
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(evidenceRepository.findUnmatched(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        RecordSettlementCommand command = new RecordSettlementCommand(
                10L,
                ApiEnvironment.LIVE,
                PaymentProvider.PAYSTACK,
                "BATCH_001",
                "SUB_123",
                20L,
                new Money(new BigDecimal("10000.00"), CurrencyCode.NGN),
                new Money(new BigDecimal("9850.00"), CurrencyCode.NGN),
                new Money(new BigDecimal("150.00"), CurrencyCode.NGN),
                ZonedDateTime.now(),
                List.of("TX_1", "TX_2")
        );

        Long settlementId = handler.execute(command);

        assertEquals(999L, settlementId);

        ArgumentCaptor<Settlement> captor = ArgumentCaptor.forClass(Settlement.class);
        verify(repository).save(captor.capture());
        Settlement saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(999L, saved.getId());
        assertEquals(10L, saved.getOrganizationId());
        assertEquals(PaymentProvider.PAYSTACK, saved.getProvider());
        assertEquals("BATCH_001", saved.getProviderSettlementId());
        assertEquals(SettlementStatus.AWAITING_ANCHOR_CREDIT, saved.getStatus());
    }

    @Test
    void execute_duplicateProviderSettlementId_returnsExistingId() {
        Settlement existing = mock(Settlement.class);
        when(existing.getId()).thenReturn(777L);
        when(repository.findByProviderAndEnvironmentAndProviderSettlementId(
                PaymentProvider.PAYSTACK, ApiEnvironment.LIVE, "BATCH_001")).thenReturn(Optional.of(existing));

        RecordSettlementCommand command = new RecordSettlementCommand(
                10L,
                ApiEnvironment.LIVE,
                PaymentProvider.PAYSTACK,
                "BATCH_001",
                "SUB_123",
                20L,
                new Money(new BigDecimal("10000.00"), CurrencyCode.NGN),
                new Money(new BigDecimal("9850.00"), CurrencyCode.NGN),
                new Money(new BigDecimal("150.00"), CurrencyCode.NGN),
                ZonedDateTime.now(),
                List.of("TX_1")
        );

        assertEquals(777L, handler.execute(command));
    }
}
