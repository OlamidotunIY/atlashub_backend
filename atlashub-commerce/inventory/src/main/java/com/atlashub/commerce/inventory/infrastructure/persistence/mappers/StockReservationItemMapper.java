package com.atlashub.commerce.inventory.infrastructure.persistence.mappers;

import com.atlashub.commerce.inventory.domain.entities.StockReservationItem;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockReservationItemJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface StockReservationItemMapper {

    StockReservationItem toDomain(StockReservationItemJpa record);

    StockReservationItemJpa toPersistence(StockReservationItem domain);
}
