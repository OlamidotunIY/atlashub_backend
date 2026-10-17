package com.atlashub.logistics.warehousing.infrastructure.persistence.mappers;

import com.atlashub.logistics.warehousing.domain.entities.StockTransfer;
import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.StockTransferItem;
import com.atlashub.logistics.warehousing.infrastructure.persistence.entities.StockTransferJpa;
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
        uses = {WarehousingJsonMapper.class}
)
public interface StockTransferMapper extends DomainMapper<StockTransfer, StockTransferJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "sourceStreet", source = "sourceAddress.street")
    @Mapping(target = "sourceCity", source = "sourceAddress.city")
    @Mapping(target = "sourceState", source = "sourceAddress.state")
    @Mapping(target = "sourceCountry", source = "sourceAddress.country")
    @Mapping(target = "sourcePostalCode", source = "sourceAddress.postalCode")
    @Mapping(target = "destStreet", source = "destinationAddress.street")
    @Mapping(target = "destCity", source = "destinationAddress.city")
    @Mapping(target = "destState", source = "destinationAddress.state")
    @Mapping(target = "destCountry", source = "destinationAddress.country")
    @Mapping(target = "destPostalCode", source = "destinationAddress.postalCode")
    @Mapping(target = "transferShippingFeeAmount", source = "transferShippingFee.amount")
    @Mapping(target = "transferShippingFeeCurrency", source = "transferShippingFee.currency")
    @Mapping(target = "itemsJson", source = "items")
    StockTransferJpa toPersistence(StockTransfer domain);

    @Override
    default StockTransfer toDomain(StockTransferJpa jpa) {
        if (jpa == null) {
            return null;
        }
        Address sourceAddress = new Address(
                jpa.getSourceStreet(),
                jpa.getSourceCity(),
                jpa.getSourceState(),
                jpa.getSourceCountry(),
                jpa.getSourcePostalCode()
        );
        Address destAddress = new Address(
                jpa.getDestStreet(),
                jpa.getDestCity(),
                jpa.getDestState(),
                jpa.getDestCountry(),
                jpa.getDestPostalCode()
        );

        CurrencyCode feeCurrency = (jpa.getTransferShippingFeeCurrency() != null && !jpa.getTransferShippingFeeCurrency().isBlank())
                ? CurrencyCode.valueOf(jpa.getTransferShippingFeeCurrency())
                : CurrencyCode.NGN;
        Money fee = Money.of(jpa.getTransferShippingFeeAmount(), feeCurrency);

        WarehousingJsonMapper jsonMapper = new WarehousingJsonMapper(new com.fasterxml.jackson.databind.ObjectMapper());
        List<StockTransferItem> items = jsonMapper.jsonToItems(jpa.getItemsJson());

        return new StockTransfer(
                jpa.getId(),
                jpa.getOrganizationId(),
                jpa.getSourceId(),
                jpa.getSourceType(),
                sourceAddress,
                jpa.getDestinationId(),
                jpa.getDestinationType(),
                destAddress,
                jpa.getTotalWeightKg(),
                fee,
                jpa.getStatus(),
                jpa.getShipmentId(),
                items,
                jpa.getRequestedBy(),
                jpa.getApprovedBy(),
                jpa.getRequestedAt(),
                jpa.getReceivedAt()
        );
    }
}
