package com.atlashub.pay.settlement.application.commands;

import com.atlashub.pay.settlement.application.commands.PollPaystackSettlements.PollPaystackSettlementsCommand;
import com.atlashub.pay.settlement.application.commands.PollPaystackSettlements.PollPaystackSettlementsHandler;
import com.atlashub.pay.settlement.application.commands.RecordSettlement.RecordSettlementHandler;
import com.atlashub.pay.settlement.domain.entities.SettlementPollCursor;
import com.atlashub.pay.settlement.domain.repositories.SettlementPollCursorRepository;
import com.atlashub.shared.application.port.SettlementProviderPort;
import com.atlashub.shared.application.port.SettlementRouteQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PollPaystackSettlementsHandlerTest {
    @Test void recordsBatchesAndAdvancesRouteCursor(){
        var routes=mock(SettlementRouteQueryPort.class);var provider=mock(SettlementProviderPort.class);
        var cursors=mock(SettlementPollCursorRepository.class);var records=mock(RecordSettlementHandler.class);
        var route=new SettlementRouteQueryPort.SettlementRoute(10L,ApiEnvironment.LIVE,"PAYSTACK","ACCT_sub",30L);
        when(routes.findActiveRoutes(ApiEnvironment.LIVE)).thenReturn(List.of(route));
        when(cursors.findByEnvironmentAndProviderAndSubaccountCode(ApiEnvironment.LIVE,"PAYSTACK","ACCT_sub"))
                .thenReturn(Optional.empty());when(cursors.nextIdentity()).thenReturn(5L);
        when(cursors.save(any())).thenAnswer(i->i.getArgument(0));
        Money gross=new Money(new BigDecimal("100.00"),CurrencyCode.NGN);
        Money fee=new Money(new BigDecimal("2.00"),CurrencyCode.NGN);
        Money net=new Money(new BigDecimal("98.00"),CurrencyCode.NGN);
        when(provider.fetchSuccessfulSettlements(ApiEnvironment.LIVE,"ACCT_sub",null)).thenReturn(List.of(
                new SettlementProviderPort.Batch("123",gross,net,fee,ZonedDateTime.now(),List.of("TX"))));
        int count=new PollPaystackSettlementsHandler(routes,provider,cursors,records)
                .execute(new PollPaystackSettlementsCommand());
        assertEquals(1,count);verify(records).execute(any());
        var captor=org.mockito.ArgumentCaptor.forClass(SettlementPollCursor.class);
        verify(cursors,org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals("123",captor.getAllValues().getLast().getLastProviderSettlementId());
    }
}
