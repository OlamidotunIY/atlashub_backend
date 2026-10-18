package com.atlashub.logistics.returns.infrastructure.persistence.mappers;

import com.atlashub.logistics.returns.domain.entities.ReturnShipment;
import com.atlashub.logistics.returns.domain.valueobject.Address;
import com.atlashub.logistics.returns.infrastructure.persistence.entities.ReturnShipmentJpa;
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
public interface ReturnShipmentMapper extends DomainMapper<ReturnShipment, ReturnShipmentJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "originStreet", source = "originAddress.street")
    @Mapping(target = "originCity", source = "originAddress.city")
    @Mapping(target = "originState", source = "originAddress.state")
    @Mapping(target = "originCountry", source = "originAddress.country")
    @Mapping(target = "originPostalCode", source = "originAddress.postalCode")
    @Mapping(target = "originalItemAmount", source = "originalItemAmount.amount")
    @Mapping(target = "originalItemCurrency", source = "originalItemAmount.currency")
    @Mapping(target = "originalShippingFeeAmount", source = "originalShippingFee.amount")
    @Mapping(target = "originalShippingFeeCurrency", source = "originalShippingFee.currency")
    @Mapping(target = "returnShippingFeeAmount", source = "returnShippingFee.amount")
    @Mapping(target = "returnShippingFeeCurrency", source = "returnShippingFee.currency")
    @Mapping(target = "declaredValueAmount", source = "declaredValue.amount")
    @Mapping(target = "declaredValueCurrency", source = "declaredValue.currency")
    @Mapping(target = "customerRefundAmount", source = "customerRefundAmount.amount")
    @Mapping(target = "customerRefundCurrency", source = "customerRefundAmount.currency")
    @Mapping(target = "merchantPenaltyAmount", source = "merchantPenaltyAmount.amount")
    @Mapping(target = "merchantPenaltyCurrency", source = "merchantPenaltyAmount.currency")
    @Mapping(target = "merchantCompensationAmount", source = "merchantCompensationAmount.amount")
    @Mapping(target = "merchantCompensationCurrency", source = "merchantCompensationAmount.currency")
    ReturnShipmentJpa toPersistence(ReturnShipment domain);

    @Override
    default ReturnShipment toDomain(ReturnShipmentJpa jpa) {
        if (jpa == null) {
            return null;
        }

        Address originAddress = new Address(
                jpa.getOriginStreet(),
                jpa.getOriginCity(),
                jpa.getOriginState(),
                jpa.getOriginCountry(),
                jpa.getOriginPostalCode()
        );

        CurrencyCode itemCur = (jpa.getOriginalItemCurrency() != null && !jpa.getOriginalItemCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getOriginalItemCurrency())
                : CurrencyCode.NGN;
        Money originalItem = Money.of(jpa.getOriginalItemAmount(), itemCur);

        CurrencyCode shipCur = (jpa.getOriginalShippingFeeCurrency() != null && !jpa.getOriginalShippingFeeCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getOriginalShippingFeeCurrency())
                : CurrencyCode.NGN;
        Money originalShip = Money.of(jpa.getOriginalShippingFeeAmount(), shipCur);

        CurrencyCode retShipCur = (jpa.getReturnShippingFeeCurrency() != null && !jpa.getReturnShippingFeeCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getReturnShippingFeeCurrency())
                : CurrencyCode.NGN;
        Money returnShip = Money.of(jpa.getReturnShippingFeeAmount(), retShipCur);

        CurrencyCode declCur = (jpa.getDeclaredValueCurrency() != null && !jpa.getDeclaredValueCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getDeclaredValueCurrency())
                : CurrencyCode.NGN;
        Money declared = Money.of(jpa.getDeclaredValueAmount(), declCur);

        CurrencyCode refCur = (jpa.getCustomerRefundCurrency() != null && !jpa.getCustomerRefundCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getCustomerRefundCurrency())
                : CurrencyCode.NGN;
        Money refund = Money.of(jpa.getCustomerRefundAmount(), refCur);

        CurrencyCode penCur = (jpa.getMerchantPenaltyCurrency() != null && !jpa.getMerchantPenaltyCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getMerchantPenaltyCurrency())
                : CurrencyCode.NGN;
        Money penalty = Money.of(jpa.getMerchantPenaltyAmount(), penCur);

        CurrencyCode compCur = (jpa.getMerchantCompensationCurrency() != null && !jpa.getMerchantCompensationCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getMerchantCompensationCurrency())
                : CurrencyCode.NGN;
        Money compensation = Money.of(jpa.getMerchantCompensationAmount(), compCur);

        return new ReturnShipment(
                jpa.getId(),
                jpa.getOrganizationId(),
                jpa.getOriginalSalesOrderId(),
                jpa.getOriginalShipmentId(),
                jpa.getCustomerId(),
                originAddress,
                jpa.getDestinationOutletId(),
                jpa.getReturnTrackingNumber(),
                jpa.getReasonType(),
                jpa.getFaultType(),
                jpa.getDeliveryAttemptsConfirmed(),
                originalItem,
                originalShip,
                returnShip,
                declared,
                refund,
                penalty,
                compensation,
                jpa.isRefundCustomerShipping(),
                jpa.getStatus(),
                jpa.getInspectionNotes(),
                jpa.getCreatedAt(),
                jpa.getReceivedAt()
        );
    }
}
