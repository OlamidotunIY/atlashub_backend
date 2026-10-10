package com.atlashub.commerce.storefront.infrastructure.persistence.mappers;

import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.SalesOrderItemJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface SalesOrderItemMapper {

    SalesOrderItem toDomain(SalesOrderItemJpa record);

    @Mapping(target = "version", ignore = true)
    SalesOrderItemJpa toPersistence(SalesOrderItem domain);
}
