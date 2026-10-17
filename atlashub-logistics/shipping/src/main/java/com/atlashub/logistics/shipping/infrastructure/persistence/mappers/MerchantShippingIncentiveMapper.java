package com.atlashub.logistics.shipping.infrastructure.persistence.mappers;

import com.atlashub.logistics.shipping.domain.entities.MerchantShippingIncentive;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.MerchantShippingIncentiveJpa;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MerchantShippingIncentiveMapper extends DomainMapper<MerchantShippingIncentive, MerchantShippingIncentiveJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "totalBaselineCostAmount", source = "totalBaselineCarrierCost.amount")
    @Mapping(target = "totalBaselineCostCurrency", source = "totalBaselineCarrierCost.currency")
    @Mapping(target = "totalActualCostAmount", source = "totalActualCarrierCost.amount")
    @Mapping(target = "totalActualCostCurrency", source = "totalActualCarrierCost.currency")
    @Mapping(target = "totalNetSavingsAmount", source = "totalNetSavings.amount")
    @Mapping(target = "totalNetSavingsCurrency", source = "totalNetSavings.currency")
    @Mapping(target = "rebateAmount", source = "rebateAmount.amount")
    @Mapping(target = "rebateCurrency", source = "rebateAmount.currency")
    MerchantShippingIncentiveJpa toPersistence(MerchantShippingIncentive domain);

    @Override
    default MerchantShippingIncentive toDomain(MerchantShippingIncentiveJpa jpa) {
        if (jpa == null) return null;
        CurrencyCode baseCur = (jpa.getTotalBaselineCostCurrency() != null && !jpa.getTotalBaselineCostCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getTotalBaselineCostCurrency())
                : CurrencyCode.NGN;
        Money baseline = (jpa.getTotalBaselineCostAmount() != null)
                ? Money.of(jpa.getTotalBaselineCostAmount(), baseCur)
                : null;

        CurrencyCode actCur = (jpa.getTotalActualCostCurrency() != null && !jpa.getTotalActualCostCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getTotalActualCostCurrency())
                : CurrencyCode.NGN;
        Money actual = (jpa.getTotalActualCostAmount() != null)
                ? Money.of(jpa.getTotalActualCostAmount(), actCur)
                : null;

        CurrencyCode netCur = (jpa.getTotalNetSavingsCurrency() != null && !jpa.getTotalNetSavingsCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getTotalNetSavingsCurrency())
                : CurrencyCode.NGN;
        Money netSavings = (jpa.getTotalNetSavingsAmount() != null)
                ? Money.of(jpa.getTotalNetSavingsAmount(), netCur)
                : null;

        CurrencyCode rebCur = (jpa.getRebateCurrency() != null && !jpa.getRebateCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getRebateCurrency())
                : CurrencyCode.NGN;
        Money rebate = Money.of(jpa.getRebateAmount(), rebCur);

        return new MerchantShippingIncentive(
                jpa.getId(),
                jpa.getOrganizationId(),
                jpa.getBillingYearMonth(),
                jpa.getEligibleShipmentCount(),
                baseline,
                actual,
                netSavings,
                jpa.getIncentivePercentage(),
                rebate,
                jpa.getStatus(),
                jpa.getCreditedAt(),
                jpa.getCreatedAt()
        );
    }
}
