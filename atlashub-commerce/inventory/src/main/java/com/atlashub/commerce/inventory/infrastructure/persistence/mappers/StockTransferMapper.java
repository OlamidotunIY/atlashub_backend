package com.atlashub.commerce.inventory.infrastructure.persistence.mappers;

import com.atlashub.commerce.inventory.domain.entities.StockTransfer;
import com.atlashub.commerce.inventory.domain.entities.StockTransferItem;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.StockTransferJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class, StockTransferItemMapper.class}
)
public interface StockTransferMapper extends DomainMapper<StockTransfer, StockTransferJpa> {

    @Override
    @Mapping(target = "items", ignore = true)
    StockTransfer toDomain(StockTransferJpa record);

    @Mapping(target = "id", source = "record.id")
    @Mapping(target = "organizationId", source = "record.organizationId")
    @Mapping(target = "sourceOutletId", source = "record.sourceOutletId")
    @Mapping(target = "destinationOutletId", source = "record.destinationOutletId")
    @Mapping(target = "status", source = "record.status")
    @Mapping(target = "requestedAt", source = "record.requestedAt")
    @Mapping(target = "receivedAt", source = "record.receivedAt")
    @Mapping(target = "items", source = "items")
    StockTransfer toDomain(StockTransferJpa record, List<StockTransferItem> items);
}
