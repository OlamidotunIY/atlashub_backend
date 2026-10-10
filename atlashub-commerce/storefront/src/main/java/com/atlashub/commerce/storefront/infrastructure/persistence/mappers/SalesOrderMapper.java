package com.atlashub.commerce.storefront.infrastructure.persistence.mappers;

import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.SalesOrderJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class, SalesOrderItemMapper.class}
)
public interface SalesOrderMapper extends DomainMapper<SalesOrder, SalesOrderJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    SalesOrderJpa toPersistence(SalesOrder domain);

    @Override
    @Mapping(target = "items", ignore = true)
    SalesOrder toDomain(SalesOrderJpa record);

    @Mapping(target = "id", source = "record.id")
    @Mapping(target = "organizationId", source = "record.organizationId")
    @Mapping(target = "outletId", source = "record.outletId")
    @Mapping(target = "vendorId", source = "record.vendorId")
    @Mapping(target = "customerId", source = "record.customerId")
    @Mapping(target = "cashierId", source = "record.cashierId")
    @Mapping(target = "tillId", source = "record.tillId")
    @Mapping(target = "type", source = "record.type")
    @Mapping(target = "status", source = "record.status")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "discountId", source = "record.discountId")
    @Mapping(target = "totalGross", source = "record.totalGross")
    @Mapping(target = "totalDiscount", source = "record.totalDiscount")
    @Mapping(target = "totalTax", source = "record.totalTax")
    @Mapping(target = "totalNet", source = "record.totalNet")
    @Mapping(target = "paymentMethod", source = "record.paymentMethod")
    @Mapping(target = "chargeReference", source = "record.chargeReference")
    @Mapping(target = "saleDate", source = "record.saleDate")
    @Mapping(target = "deliveryAddress", source = "record.deliveryAddress")
    SalesOrder toDomain(SalesOrderJpa record, List<SalesOrderItem> items);
}
