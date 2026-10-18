package com.atlashub.analytics.presentation.rest;

import com.atlashub.analytics.application.query.GetBusinessOverview.BusinessOverviewResult;
import com.atlashub.analytics.application.query.GetBusinessOverview.GetBusinessOverviewHandler;
import com.atlashub.analytics.application.query.GetBusinessOverview.GetBusinessOverviewQuery;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalyticsOverviewControllerTest {
    @Test
    void derives_the_organization_from_the_authenticated_principal() {
        GetBusinessOverviewHandler handler=mock(GetBusinessOverviewHandler.class); when(handler.execute(any())).thenReturn(new BusinessOverviewResult(LocalDate.now().minusDays(29),LocalDate.now(),new BusinessOverviewResult.Sales(BigDecimal.ONE,1,BigDecimal.ONE),new BusinessOverviewResult.Payments(BigDecimal.ZERO,0,0,BigDecimal.ZERO),new BusinessOverviewResult.People(1,1,0),new BusinessOverviewResult.Logistics(0,0,0,BigDecimal.ZERO),new BusinessOverviewResult.Inventory(0,0,0,BigDecimal.ZERO),new BusinessOverviewResult.Finance(false,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,"UNAVAILABLE")));
        LocalDate asOfDate = LocalDate.of(2026, 10, 18);
        var response=new AnalyticsOverviewController(handler).getOverview(new AuthenticatedPrincipal(1L,7L,"live","s","t",ZonedDateTime.now().plusHours(1)),asOfDate);
        assertTrue(response.getBody().status()); assertEquals(1,response.getBody().data().sales().transactions()); verify(handler).execute(new GetBusinessOverviewQuery(7L,"live",asOfDate));
    }
}
