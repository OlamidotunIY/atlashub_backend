package com.atlashub.pay.settlement.application.commands;

import com.atlashub.pay.settlement.application.commands.ConfirmSettlement.*;
import com.atlashub.pay.settlement.domain.entities.*;
import com.atlashub.pay.settlement.domain.repositories.*;
import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfirmSettlementHandlerTest {
    private SettlementRepository settlements;
    private SettlementCreditEvidenceRepository evidence;
    private ConfirmSettlementHandler handler;
    private final Money net = new Money(new BigDecimal("9850.00"), CurrencyCode.NGN);

    @BeforeEach void setUp(){
        settlements=mock(SettlementRepository.class);evidence=mock(SettlementCreditEvidenceRepository.class);
        handler=new ConfirmSettlementHandler(settlements,evidence);
    }
    private Settlement settlement(){return Settlement.create(100L,10L,ApiEnvironment.LIVE,PaymentProvider.PAYSTACK,
            "BATCH_001","SUB_123",new Money(new BigDecimal("10000.00"),CurrencyCode.NGN),net,
            new Money(new BigDecimal("150.00"),CurrencyCode.NGN),20L,List.of("TX_1"),ZonedDateTime.now(),null,"test");}
    private ConfirmSettlementCommand command(){return new ConfirmSettlementCommand(10L,ApiEnvironment.LIVE,20L,
            "ANCHOR_TX_REF",net,ZonedDateTime.now());}

    @Test void storesEvidenceAndConfirmsSingleMatchingSettlement(){
        when(evidence.findByEnvironmentAndAnchorTransferReference(ApiEnvironment.LIVE,"ANCHOR_TX_REF"))
                .thenReturn(Optional.empty());when(evidence.nextIdentity()).thenReturn(9L);
        when(evidence.save(any())).thenAnswer(i->i.getArgument(0));
        Settlement settlement=settlement();when(settlements.findAwaitingAnchorCredit(20L,ApiEnvironment.LIVE,net))
                .thenReturn(List.of(settlement));
        assertNull(handler.execute(command()));assertTrue(settlement.isConfirmed());
        verify(settlements).save(settlement);verify(evidence,atLeastOnce()).save(any());
    }

    @Test void storesUnmatchedEvidenceForLaterPolling(){
        when(evidence.findByEnvironmentAndAnchorTransferReference(ApiEnvironment.LIVE,"ANCHOR_TX_REF"))
                .thenReturn(Optional.empty());when(evidence.nextIdentity()).thenReturn(9L);
        when(evidence.save(any())).thenAnswer(i->i.getArgument(0));
        when(settlements.findAwaitingAnchorCredit(20L,ApiEnvironment.LIVE,net)).thenReturn(List.of());
        assertNull(handler.execute(command()));verify(settlements,never()).save(any());
    }

    @Test void duplicateCreditIsIdempotent(){
        var item=SettlementCreditEvidence.record(9L,10L,ApiEnvironment.LIVE,20L,"ANCHOR_TX_REF",net,ZonedDateTime.now());
        item.match(100L);when(evidence.findByEnvironmentAndAnchorTransferReference(ApiEnvironment.LIVE,"ANCHOR_TX_REF"))
                .thenReturn(Optional.of(item));assertNull(handler.execute(command()));verifyNoInteractions(settlements);
    }
}
