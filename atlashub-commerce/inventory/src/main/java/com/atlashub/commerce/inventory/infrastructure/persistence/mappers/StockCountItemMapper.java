package com.atlashub.commerce.inventory.infrastructure.persistence.mappers;

import com.atlashub.commerce.inventory.domain.entities.StockCountItem;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockCountItemJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface StockCountItemMapper {

    StockCountItem toDomain(StockCountItemJpa record);

    StockCountItemJpa toPersistence(StockCountItem domain);
}
