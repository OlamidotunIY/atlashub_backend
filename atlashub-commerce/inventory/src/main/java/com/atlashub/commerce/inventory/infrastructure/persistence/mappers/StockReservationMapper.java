package com.atlashub.commerce.inventory.infrastructure.persistence.mappers;

import com.atlashub.commerce.inventory.domain.entities.StockReservation;
import com.atlashub.commerce.inventory.domain.entities.StockReservationItem;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockReservationJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class, StockReservationItemMapper.class}
)
public interface StockReservationMapper extends DomainMapper<StockReservation, StockReservationJpa> {

    @Override
    @Mapping(target = "items", ignore = true)
    StockReservation toDomain(StockReservationJpa record);

    @Mapping(target = "id", source = "record.id")
    @Mapping(target = "salesOrderId", source = "record.salesOrderId")
    @Mapping(target = "organizationId", source = "record.organizationId")
    @Mapping(target = "outletId", source = "record.outletId")
    @Mapping(target = "status", source = "record.status")
    @Mapping(target = "failureReason", source = "record.failureReason")
    @Mapping(target = "createdAt", source = "record.createdAt")
    @Mapping(target = "updatedAt", source = "record.updatedAt")
    @Mapping(target = "items", source = "items")
    StockReservation toDomain(StockReservationJpa record, List<StockReservationItem> items);
}
