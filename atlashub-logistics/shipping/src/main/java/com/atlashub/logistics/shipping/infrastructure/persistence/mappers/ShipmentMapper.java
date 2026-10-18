package com.atlashub.logistics.shipping.infrastructure.persistence.mappers;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.ShipmentJpa;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ShipmentMapper extends DomainMapper<Shipment, ShipmentJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "originStreet", source = "originAddress.street")
    @Mapping(target = "originCity", source = "originAddress.city")
    @Mapping(target = "originState", source = "originAddress.state")
    @Mapping(target = "originCountry", source = "originAddress.country")
    @Mapping(target = "originPostalCode", source = "originAddress.postalCode")
    @Mapping(target = "destStreet", source = "destinationAddress.street")
    @Mapping(target = "destCity", source = "destinationAddress.city")
    @Mapping(target = "destState", source = "destinationAddress.state")
    @Mapping(target = "destCountry", source = "destinationAddress.country")
    @Mapping(target = "destPostalCode", source = "destinationAddress.postalCode")
    @Mapping(target = "lengthCm", source = "dimensions.lengthCm")
    @Mapping(target = "widthCm", source = "dimensions.widthCm")
    @Mapping(target = "heightCm", source = "dimensions.heightCm")
    @Mapping(target = "declaredAmount", source = "declaredValue.amount")
    @Mapping(target = "declaredCurrency", source = "declaredValue.currency")
    @Mapping(target = "customerFeeAmount", source = "customerShippingFee.amount")
    @Mapping(target = "customerFeeCurrency", source = "customerShippingFee.currency")
    @Mapping(target = "baselineSingleCostAmount", source = "baselineSingleCost.amount")
    @Mapping(target = "baselineSingleCostCurrency", source = "baselineSingleCost.currency")
    @Mapping(target = "allocatedActualCostAmount", source = "allocatedActualCost.amount")
    @Mapping(target = "allocatedActualCostCurrency", source = "allocatedActualCost.currency")
    ShipmentJpa toPersistence(Shipment domain);

    @Override
    default Shipment toDomain(ShipmentJpa jpa) {
        if (jpa == null) return null;
        Address origin = (jpa.getOriginCity() != null || jpa.getOriginState() != null)
                ? new Address(jpa.getOriginStreet(), jpa.getOriginCity(), jpa.getOriginState(), jpa.getOriginCountry(), jpa.getOriginPostalCode())
                : null;
        Address dest = (jpa.getDestCity() != null || jpa.getDestState() != null)
                ? new Address(jpa.getDestStreet(), jpa.getDestCity(), jpa.getDestState(), jpa.getDestCountry(), jpa.getDestPostalCode())
                : null;
        Dimensions dims = (jpa.getLengthCm() != null || jpa.getWidthCm() != null || jpa.getHeightCm() != null)
                ? new Dimensions(jpa.getLengthCm(), jpa.getWidthCm(), jpa.getHeightCm())
                : null;
        com.atlashub.shared.domain.valueobject.CurrencyCode decCur = (jpa.getDeclaredCurrency() != null && !jpa.getDeclaredCurrency().isBlank())
                ? com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(jpa.getDeclaredCurrency())
                : com.atlashub.shared.domain.valueobject.CurrencyCode.NGN;
        Money declared = (jpa.getDeclaredAmount() != null)
                ? Money.of(jpa.getDeclaredAmount(), decCur)
                : null;

        com.atlashub.shared.domain.valueobject.CurrencyCode feeCur = (jpa.getCustomerFeeCurrency() != null && !jpa.getCustomerFeeCurrency().isBlank())
                ? com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(jpa.getCustomerFeeCurrency())
                : com.atlashub.shared.domain.valueobject.CurrencyCode.NGN;
        Money customerFee = (jpa.getCustomerFeeAmount() != null)
                ? Money.of(jpa.getCustomerFeeAmount(), feeCur)
                : null;

        com.atlashub.shared.domain.valueobject.CurrencyCode baseCur = (jpa.getBaselineSingleCostCurrency() != null && !jpa.getBaselineSingleCostCurrency().isBlank())
                ? com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(jpa.getBaselineSingleCostCurrency())
                : com.atlashub.shared.domain.valueobject.CurrencyCode.NGN;
        Money baselineCost = (jpa.getBaselineSingleCostAmount() != null)
                ? Money.of(jpa.getBaselineSingleCostAmount(), baseCur)
                : null;

        com.atlashub.shared.domain.valueobject.CurrencyCode allocCur = (jpa.getAllocatedActualCostCurrency() != null && !jpa.getAllocatedActualCostCurrency().isBlank())
                ? com.atlashub.shared.domain.valueobject.CurrencyCode.valueOf(jpa.getAllocatedActualCostCurrency())
                : com.atlashub.shared.domain.valueobject.CurrencyCode.NGN;
        Money allocatedCost = (jpa.getAllocatedActualCostAmount() != null)
                ? Money.of(jpa.getAllocatedActualCostAmount(), allocCur)
                : null;

        return new Shipment(
                jpa.getId(),
                jpa.getOrganizationId(),
                jpa.getOrderId(),
                jpa.getTrackingNumber(),
                jpa.getType(),
                jpa.getServiceTier(),
                jpa.getRouteType(),
                jpa.getFulfillmentChannel(),
                origin,
                dest,
                jpa.getSenderId(),
                jpa.getReceiverId(),
                jpa.getWeightKg(),
                dims,
                declared,
                customerFee,
                baselineCost,
                allocatedCost,
                jpa.getConsolidationBatchId(),
                jpa.getDeliveryCode(),
                jpa.getDeliveryWindowStart(),
                jpa.getDeliveryWindowEnd(),
                jpa.getLatestDispatchAt(),
                jpa.getCurrentMilestone(),
                jpa.getStatus(),
                jpa.getDeliveryAttempts(),
                jpa.getFailureFaultType(),
                jpa.getFailureReason(),
                jpa.getRiderId(),
                jpa.getVehicleId(),
                jpa.getCarrier3PLCode(),
                jpa.getCarrier3PLReference(),
                jpa.getProofOfDeliveryUrl(),
                jpa.getCurrentLocation(),
                jpa.getCurrentLatitude(),
                jpa.getCurrentLongitude(),
                jpa.isIncentiveEligible(),
                jpa.getDispatchedAt(),
                jpa.getDeliveredAt(),
                jpa.getCreatedAt()
        );
    }
}
