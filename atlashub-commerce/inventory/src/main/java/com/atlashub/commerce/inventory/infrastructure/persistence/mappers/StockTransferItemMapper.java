package com.atlashub.commerce.inventory.infrastructure.persistence.mappers;

import com.atlashub.commerce.inventory.domain.entities.StockTransferItem;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockTransferItemJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface StockTransferItemMapper {

    StockTransferItem toDomain(StockTransferItemJpa record);

    StockTransferItemJpa toPersistence(StockTransferItem domain);
}
