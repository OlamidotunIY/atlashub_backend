package com.atlashub.pay.settlement.application.queries;

import com.atlashub.pay.settlement.application.queries.GetSettlementDetails.GetSettlementDetailsHandler;
import com.atlashub.pay.settlement.application.queries.GetSettlementDetails.GetSettlementDetailsQuery;
import com.atlashub.pay.settlement.application.queries.GetSettlementDetails.SettlementResult;
import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.domain.exceptions.SettlementNotFoundException;
import com.atlashub.pay.settlement.domain.repositories.SettlementRepository;
import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetSettlementDetailsHandlerTest {

    private SettlementRepository repository;
    private GetSettlementDetailsHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(SettlementRepository.class);
        handler = new GetSettlementDetailsHandler(repository);
    }

    @Test
    void execute_existingSettlement_returnsSettlementResult() {
        Settlement settlement = Settlement.create(
                100L,
                10L,
                PaymentProvider.PAYSTACK,
                "BATCH_001",
                new Money(new BigDecimal("10000.00"), CurrencyCode.NGN),
                new Money(new BigDecimal("9850.00"), CurrencyCode.NGN),
                new Money(new BigDecimal("150.00"), CurrencyCode.NGN),
                20L,
                ZonedDateTime.now(),
                "daily batch"
        );

        when(repository.findById(100L)).thenReturn(Optional.of(settlement));

        SettlementResult result = handler.execute(new GetSettlementDetailsQuery(100L, 10L, ApiEnvironment.LIVE));

        assertNotNull(result);
        assertEquals(100L, result.id());
        assertEquals(10L, result.organizationId());
        assertEquals(PaymentProvider.PAYSTACK, result.provider());
        assertEquals("BATCH_001", result.providerSettlementId());
        assertEquals(new Money(new BigDecimal("9850.00"), CurrencyCode.NGN), result.amount());
        assertEquals(SettlementStatus.AWAITING_ANCHOR_CREDIT, result.status());
        assertEquals("daily batch", result.description());
    }

    @Test
    void execute_nonExistingSettlement_throwsSettlementNotFoundException() {
        when(repository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(SettlementNotFoundException.class, () ->
                handler.execute(new GetSettlementDetailsQuery(100L, 10L, ApiEnvironment.LIVE)));
    }
}
