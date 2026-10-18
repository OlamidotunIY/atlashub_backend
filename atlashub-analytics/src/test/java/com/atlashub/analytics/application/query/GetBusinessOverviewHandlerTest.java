package com.atlashub.analytics.application.query;

import com.atlashub.analytics.application.query.GetBusinessOverview.GetBusinessOverviewHandler;
import com.atlashub.analytics.application.query.GetBusinessOverview.GetBusinessOverviewQuery;
import com.atlashub.analytics.domain.repositories.DailySalesProjectionRepository;
import com.atlashub.analytics.domain.repositories.DeliveryPerformanceProjectionRepository;
import com.atlashub.analytics.domain.repositories.FinancialHealthProjectionRepository;
import com.atlashub.analytics.domain.repositories.HeadcountProjectionRepository;
import com.atlashub.analytics.domain.repositories.InventoryHealthProjectionRepository;
import com.atlashub.analytics.domain.repositories.TransactionVolumeProjectionRepository;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetBusinessOverviewHandlerTest {
    @Test
    void scopes_all_reads_to_the_requested_organization_and_environment() {
        DailySalesProjectionRepository sales=mock(DailySalesProjectionRepository.class); TransactionVolumeProjectionRepository payments=mock(TransactionVolumeProjectionRepository.class); HeadcountProjectionRepository people=mock(HeadcountProjectionRepository.class); DeliveryPerformanceProjectionRepository logistics=mock(DeliveryPerformanceProjectionRepository.class); InventoryHealthProjectionRepository inventory=mock(InventoryHealthProjectionRepository.class); FinancialHealthProjectionRepository finance=mock(FinancialHealthProjectionRepository.class);
        when(sales.findByOrgEnvAndDateRange(anyLong(),anyString(),isNull(),any(),any())).thenReturn(List.of()); when(payments.findByOrgEnvAndMonth(anyLong(),anyString(),anyString())).thenReturn(java.util.Optional.empty()); when(people.findByOrgEnv(anyLong(),anyString())).thenReturn(java.util.Optional.empty()); when(logistics.findByOrgEnvAndMonth(anyLong(),anyString(),anyString())).thenReturn(java.util.Optional.empty()); when(inventory.findByOrgEnvAndOutlet(anyLong(),anyString(),isNull())).thenReturn(List.of()); when(finance.findLatestByOrgEnv(anyLong(),anyString())).thenReturn(java.util.Optional.empty());
        var result=new GetBusinessOverviewHandler(sales,payments,people,logistics,inventory,finance).execute(new GetBusinessOverviewQuery(9L,"sandbox",LocalDate.of(2026,10,18)));
        assertEquals(0,result.sales().transactions()); assertEquals(false,result.finance().available()); verify(sales).findByOrgEnvAndDateRange(eq(9L),eq("sandbox"),isNull(),any(),any()); verify(inventory).findByOrgEnvAndOutlet(9L,"sandbox",null);
    }
}
