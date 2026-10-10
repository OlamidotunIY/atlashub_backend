package com.atlashub.commerce.catalog.infrastructure.persistence.mappers;

import com.atlashub.commerce.catalog.domain.entities.PurchaseOrderItem;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.PurchaseOrderItemJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface PurchaseOrderItemMapper {

    PurchaseOrderItem toDomain(PurchaseOrderItemJpa record);

    PurchaseOrderItemJpa toPersistence(PurchaseOrderItem domain);
}
