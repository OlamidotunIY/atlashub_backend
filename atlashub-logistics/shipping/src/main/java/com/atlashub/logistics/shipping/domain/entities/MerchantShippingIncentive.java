package com.atlashub.logistics.shipping.domain.entities;

import com.atlashub.logistics.shipping.domain.events.MerchantShippingIncentiveCreditedEvent;
import com.atlashub.logistics.shipping.domain.exceptions.InvalidShipmentStatusException;
import com.atlashub.logistics.shipping.domain.valueobject.IncentiveStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class MerchantShippingIncentive extends AggregateRoot<Long> {

    public static final BigDecimal INCENTIVE_PERCENTAGE = new BigDecimal("0.10");

    private final Long id;
    private final Long organizationId;
    private final String billingYearMonth;
    private final int eligibleShipmentCount;
    private final Money totalBaselineCarrierCost;
    private final Money totalActualCarrierCost;
    private final Money totalNetSavings;
    private final BigDecimal incentivePercentage;
    private final Money rebateAmount;
    private IncentiveStatus status;
    private ZonedDateTime creditedAt;
    private final ZonedDateTime createdAt;

    public MerchantShippingIncentive(
            Long id,
            Long organizationId,
            String billingYearMonth,
            int eligibleShipmentCount,
            Money totalBaselineCarrierCost,
            Money totalActualCarrierCost,
            Money totalNetSavings,
            BigDecimal incentivePercentage,
            Money rebateAmount,
            IncentiveStatus status,
            ZonedDateTime creditedAt,
            ZonedDateTime createdAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.billingYearMonth = billingYearMonth;
        this.eligibleShipmentCount = eligibleShipmentCount;
        this.totalBaselineCarrierCost = totalBaselineCarrierCost;
        this.totalActualCarrierCost = totalActualCarrierCost;
        this.totalNetSavings = totalNetSavings;
        this.incentivePercentage = incentivePercentage != null ? incentivePercentage : INCENTIVE_PERCENTAGE;
        this.rebateAmount = rebateAmount;
        this.status = status;
        this.creditedAt = creditedAt;
        this.createdAt = createdAt;
    }

    public static MerchantShippingIncentive calculate(
            Long id,
            Long organizationId,
            String billingYearMonth,
            int eligibleShipmentCount,
            Money totalBaselineCarrierCost,
            Money totalActualCarrierCost,
            ZonedDateTime now
    ) {
        Money netSavings = Money.zero(totalBaselineCarrierCost.currency());
        if (totalBaselineCarrierCost.isGreaterThan(totalActualCarrierCost)) {
            netSavings = totalBaselineCarrierCost.subtract(totalActualCarrierCost);
        }
        Money rebate = netSavings.multiply(INCENTIVE_PERCENTAGE);

        return new MerchantShippingIncentive(
                id,
                organizationId,
                billingYearMonth,
                eligibleShipmentCount,
                totalBaselineCarrierCost,
                totalActualCarrierCost,
                netSavings,
                INCENTIVE_PERCENTAGE,
                rebate,
                IncentiveStatus.CALCULATED,
                null,
                now
        );
    }

    public void markCredited(ZonedDateTime now, String correlationId) {
        if (this.status == IncentiveStatus.CREDITED) {
            throw new InvalidShipmentStatusException("Incentive rebate has already been credited");
        }
        this.status = IncentiveStatus.CREDITED;
        this.creditedAt = now;

        registerEvent(MerchantShippingIncentiveCreditedEvent.of(
                this.id,
                correlationId,
                new MerchantShippingIncentiveCreditedEvent.Payload(
                        this.id,
                        this.organizationId,
                        this.billingYearMonth,
                        this.eligibleShipmentCount,
                        this.rebateAmount,
                        now
                )
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
