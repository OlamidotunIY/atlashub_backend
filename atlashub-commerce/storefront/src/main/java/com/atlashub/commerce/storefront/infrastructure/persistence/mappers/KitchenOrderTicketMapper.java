package com.atlashub.commerce.storefront.infrastructure.persistence.mappers;

import com.atlashub.commerce.storefront.domain.entities.KitchenOrderTicket;
import com.atlashub.commerce.storefront.domain.entities.KotItem;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.KitchenOrderTicketJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class, KotItemMapper.class}
)
public interface KitchenOrderTicketMapper extends DomainMapper<KitchenOrderTicket, KitchenOrderTicketJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    KitchenOrderTicketJpa toPersistence(KitchenOrderTicket domain);

    @Override
    @Mapping(target = "items", ignore = true)
    KitchenOrderTicket toDomain(KitchenOrderTicketJpa record);

    @Mapping(target = "id", source = "record.id")
    @Mapping(target = "salesOrderId", source = "record.salesOrderId")
    @Mapping(target = "tableId", source = "record.tableId")
    @Mapping(target = "outletId", source = "record.outletId")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "status", source = "record.status")
    @Mapping(target = "sentAt", source = "record.sentAt")
    KitchenOrderTicket toDomain(KitchenOrderTicketJpa record, List<KotItem> items);
}
