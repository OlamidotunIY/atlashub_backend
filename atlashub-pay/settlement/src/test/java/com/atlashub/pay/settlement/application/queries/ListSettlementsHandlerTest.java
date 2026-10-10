package com.atlashub.pay.settlement.application.queries;

import com.atlashub.pay.settlement.application.queries.GetSettlementDetails.SettlementResult;
import com.atlashub.pay.settlement.application.queries.ListSettlements.ListSettlementsHandler;
import com.atlashub.pay.settlement.application.queries.ListSettlements.ListSettlementsQuery;
import com.atlashub.pay.settlement.domain.entities.Settlement;
import com.atlashub.pay.settlement.domain.repositories.SettlementRepository;
import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListSettlementsHandlerTest {

    private SettlementRepository repository;
    private ListSettlementsHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(SettlementRepository.class);
        handler = new ListSettlementsHandler(repository);
    }

    @Test
    void execute_validQuery_returnsPagedSettlementResults() {
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

        PageResult<Settlement> pageResult = new PageResult<>(
                List.of(settlement),
                0,
                10,
                1,
                1
        );

        ZonedDateTime from = ZonedDateTime.now().minusDays(7);
        ZonedDateTime to = ZonedDateTime.now();
        ListSettlementsQuery query = new ListSettlementsQuery(
                10L, ApiEnvironment.LIVE,
                SettlementStatus.AWAITING_ANCHOR_CREDIT,
                from,
                to,
                0,
                10
        );

        when(repository.findByOrganizationId(10L, ApiEnvironment.LIVE, SettlementStatus.AWAITING_ANCHOR_CREDIT, from, to, 0, 10))
                .thenReturn(pageResult);

        PageResult<SettlementResult> result = handler.execute(query);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());
        assertEquals(100L, result.content().getFirst().id());
        assertEquals(PaymentProvider.PAYSTACK, result.content().getFirst().provider());
    }

    @Test
    void execute_emptyResult_returnsEmptyPage() {
        PageResult<Settlement> emptyPage = new PageResult<>(
                List.of(),
                0,
                10,
                0,
                0
        );

        ListSettlementsQuery query = new ListSettlementsQuery(
                10L, ApiEnvironment.LIVE,
                null,
                null,
                null,
                0,
                10
        );

        when(repository.findByOrganizationId(10L, ApiEnvironment.LIVE, null, null, null, 0, 10))
                .thenReturn(emptyPage);

        PageResult<SettlementResult> result = handler.execute(query);

        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
    }
}
