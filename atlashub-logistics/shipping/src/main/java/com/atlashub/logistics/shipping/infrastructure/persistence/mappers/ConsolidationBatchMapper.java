package com.atlashub.logistics.shipping.infrastructure.persistence.mappers;

import com.atlashub.logistics.shipping.domain.entities.ConsolidationBatch;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.ConsolidationBatchJpa;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ShippingJsonMapper.class}
)
public interface ConsolidationBatchMapper extends DomainMapper<ConsolidationBatch, ConsolidationBatchJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "shipmentIdsJson", source = "shipmentIdsView")
    @Mapping(target = "totalBaselineCostAmount", source = "totalBaselineCost.amount")
    @Mapping(target = "totalBaselineCostCurrency", source = "totalBaselineCost.currency")
    @Mapping(target = "actualBatchCostAmount", source = "actualBatchCost.amount")
    @Mapping(target = "actualBatchCostCurrency", source = "actualBatchCost.currency")
    ConsolidationBatchJpa toPersistence(ConsolidationBatch domain);

    @Override
    default ConsolidationBatch toDomain(ConsolidationBatchJpa jpa) {
        if (jpa == null) return null;
        ShippingJsonMapper jsonMapper = new ShippingJsonMapper(new com.fasterxml.jackson.databind.ObjectMapper());
        List<Long> ids = jsonMapper.jsonToShipmentIds(jpa.getShipmentIdsJson());
        CurrencyCode baseCur = (jpa.getTotalBaselineCostCurrency() != null && !jpa.getTotalBaselineCostCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getTotalBaselineCostCurrency())
                : CurrencyCode.NGN;
        Money baseline = (jpa.getTotalBaselineCostAmount() != null)
                ? Money.of(jpa.getTotalBaselineCostAmount(), baseCur)
                : null;

        CurrencyCode actCur = (jpa.getActualBatchCostCurrency() != null && !jpa.getActualBatchCostCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getActualBatchCostCurrency())
                : CurrencyCode.NGN;
        Money actual = (jpa.getActualBatchCostAmount() != null)
                ? Money.of(jpa.getActualBatchCostAmount(), actCur)
                : null;

        return new ConsolidationBatch(
                jpa.getId(),
                jpa.getBatchReference(),
                jpa.getOriginState(),
                jpa.getOriginCity(),
                jpa.getDestinationState(),
                jpa.getDestinationCity(),
                jpa.getRouteType(),
                ids,
                jpa.getTotalWeightKg(),
                jpa.getLatestDispatchAt(),
                baseline,
                actual,
                jpa.getStatus(),
                jpa.getCarrierChannel(),
                jpa.getCarrierCode(),
                jpa.getDispatchedAt(),
                jpa.getCreatedAt()
        );
    }
}
