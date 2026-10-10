package com.atlashub.commerce.inventory.infrastructure.persistence.mappers;

import com.atlashub.commerce.inventory.domain.entities.StockCount;
import com.atlashub.commerce.inventory.domain.entities.StockCountItem;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockCountJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class, StockCountItemMapper.class}
)
public interface StockCountMapper extends DomainMapper<StockCount, StockCountJpa> {

    @Override
    @Mapping(target = "items", ignore = true)
    StockCount toDomain(StockCountJpa record);

    @Mapping(target = "id", source = "record.id")
    @Mapping(target = "organizationId", source = "record.organizationId")
    @Mapping(target = "outletId", source = "record.outletId")
    @Mapping(target = "status", source = "record.status")
    @Mapping(target = "startedAt", source = "record.startedAt")
    @Mapping(target = "reconciledAt", source = "record.reconciledAt")
    @Mapping(target = "items", source = "items")
    StockCount toDomain(StockCountJpa record, List<StockCountItem> items);
}
