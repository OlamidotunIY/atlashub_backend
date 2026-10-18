package com.atlashub.analytics.application.query.GetBusinessOverview;

import com.atlashub.analytics.domain.entities.InventoryHealthProjection;
import com.atlashub.analytics.domain.repositories.DailySalesProjectionRepository;
import com.atlashub.analytics.domain.repositories.DeliveryPerformanceProjectionRepository;
import com.atlashub.analytics.domain.repositories.FinancialHealthProjectionRepository;
import com.atlashub.analytics.domain.repositories.HeadcountProjectionRepository;
import com.atlashub.analytics.domain.repositories.InventoryHealthProjectionRepository;
import com.atlashub.analytics.domain.repositories.TransactionVolumeProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Component
public class GetBusinessOverviewHandler extends Query<GetBusinessOverviewQuery, BusinessOverviewResult> {
    private final DailySalesProjectionRepository sales; private final TransactionVolumeProjectionRepository payments;
    private final HeadcountProjectionRepository people; private final DeliveryPerformanceProjectionRepository logistics;
    private final InventoryHealthProjectionRepository inventory; private final FinancialHealthProjectionRepository finance;
    public GetBusinessOverviewHandler(DailySalesProjectionRepository sales, TransactionVolumeProjectionRepository payments, HeadcountProjectionRepository people, DeliveryPerformanceProjectionRepository logistics, InventoryHealthProjectionRepository inventory, FinancialHealthProjectionRepository finance) {
        this.sales=sales; this.payments=payments; this.people=people; this.logistics=logistics; this.inventory=inventory; this.finance=finance;
    }
    @Override @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('analytics:reports:read')")
    public BusinessOverviewResult execute(GetBusinessOverviewQuery query) {
        String env=query.environment()!=null?query.environment():"live"; LocalDate to=query.asOfDate()!=null?query.asOfDate():LocalDate.now(); LocalDate from=to.minusDays(29); String month=String.format("%04d-%02d",to.getYear(),to.getMonthValue());
        var rows=sales.findByOrgEnvAndDateRange(query.organizationId(),env,null,from,to); long tx=rows.stream().mapToLong(r->r.getTotalTransactions()).sum(); BigDecimal net=rows.stream().map(r->r.getTotalNet()).reduce(BigDecimal.ZERO,BigDecimal::add); BigDecimal aov=tx==0?BigDecimal.ZERO:net.divide(BigDecimal.valueOf(tx),2,RoundingMode.HALF_UP);
        var pay=payments.findByOrgEnvAndMonth(query.organizationId(),env,month).orElse(null); var head=people.findByOrgEnv(query.organizationId(),env).orElse(null); var delivery=logistics.findByOrgEnvAndMonth(query.organizationId(),env,month).orElse(null);
        List<InventoryHealthProjection> stock=inventory.findByOrgEnvAndOutlet(query.organizationId(),env,null); int out=(int)stock.stream().filter(i->i.getCurrentStock()<=0).count(); int low=(int)stock.stream().filter(i->i.getCurrentStock()>0&&i.getCurrentStock()<=i.getReorderPoint()).count(); int dead=(int)stock.stream().filter(InventoryHealthProjection::isDeadStock).count(); BigDecimal value=stock.stream().map(InventoryHealthProjection::getInventoryValue).reduce(BigDecimal.ZERO,BigDecimal::add);
        var health=finance.findLatestByOrgEnv(query.organizationId(),env).orElse(null);
        return new BusinessOverviewResult(from,to,new BusinessOverviewResult.Sales(net,tx,aov),new BusinessOverviewResult.Payments(pay==null?BigDecimal.ZERO:pay.getTotalVolume(),pay==null?0:pay.getSuccessfulCharges(),pay==null?0:pay.getFailedCharges(),pay==null?BigDecimal.ZERO:pay.getTotalPayoutAmount()),new BusinessOverviewResult.People(head==null?0:head.getTotal(),head==null?0:head.getActive(),head==null?0:head.getSuspended()),new BusinessOverviewResult.Logistics(delivery==null?0:delivery.getTotalShipments(),delivery==null?0:delivery.getDelivered(),delivery==null?0:delivery.getFailed(),delivery==null?BigDecimal.ZERO:delivery.getDeliverySuccessRate()),new BusinessOverviewResult.Inventory(out,low,dead,value),new BusinessOverviewResult.Finance(health!=null,health==null?BigDecimal.ZERO:health.getCashBalance(),health==null?BigDecimal.ZERO:health.getAccountsReceivable(),health==null?BigDecimal.ZERO:health.getAccountsPayable(),health==null?"UNAVAILABLE":health.getReconciliationStatus()));
    }
}
