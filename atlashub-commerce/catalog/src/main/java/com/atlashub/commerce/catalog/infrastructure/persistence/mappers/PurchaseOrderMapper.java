package com.atlashub.commerce.catalog.infrastructure.persistence.mappers;

import com.atlashub.commerce.catalog.domain.entities.PurchaseOrder;
import com.atlashub.commerce.catalog.domain.entities.PurchaseOrderItem;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.PurchaseOrderJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class, PurchaseOrderItemMapper.class}
)
public interface PurchaseOrderMapper extends DomainMapper<PurchaseOrder, PurchaseOrderJpa> {

    @Override
    @Mapping(target = "items", ignore = true)
    PurchaseOrder toDomain(PurchaseOrderJpa record);

    @Mapping(target = "id", source = "record.id")
    @Mapping(target = "organizationId", source = "record.organizationId")
    @Mapping(target = "outletId", source = "record.outletId")
    @Mapping(target = "supplierId", source = "record.supplierId")
    @Mapping(target = "status", source = "record.status")
    @Mapping(target = "totalAmount", source = "record.totalAmount")
    @Mapping(target = "expectedDeliveryDate", source = "record.expectedDeliveryDate")
    @Mapping(target = "createdAt", source = "record.createdAt")
    @Mapping(target = "updatedAt", source = "record.updatedAt")
    @Mapping(target = "items", source = "items")
    PurchaseOrder toDomain(PurchaseOrderJpa record, List<PurchaseOrderItem> items);
}
