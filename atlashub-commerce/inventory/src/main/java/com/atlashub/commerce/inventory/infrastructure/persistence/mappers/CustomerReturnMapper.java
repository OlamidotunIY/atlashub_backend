package com.atlashub.commerce.inventory.infrastructure.persistence.mappers;

import com.atlashub.commerce.inventory.domain.entities.CustomerReturn;
import com.atlashub.commerce.inventory.domain.entities.ReturnItem;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.CustomerReturnJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {ValueObjectMapper.class, ReturnItemMapper.class})
public interface CustomerReturnMapper extends DomainMapper<CustomerReturn, CustomerReturnJpa> {

    @Override
    @Mapping(target = "items", ignore = true)
    CustomerReturn toDomain(CustomerReturnJpa record);

    @Mapping(target = "id", source = "record.id")
    @Mapping(target = "organizationId", source = "record.organizationId")
    @Mapping(target = "outletId", source = "record.outletId")
    @Mapping(target = "salesOrderId", source = "record.salesOrderId")
    @Mapping(target = "customerId", source = "record.customerId")
    @Mapping(target = "refundAmount", source = "record.refundAmount")
    @Mapping(target = "reason", source = "record.reason")
    @Mapping(target = "refundMethod", source = "record.refundMethod")
    @Mapping(target = "status", source = "record.status")
    @Mapping(target = "createdAt", source = "record.createdAt")
    @Mapping(target = "items", source = "items")
    CustomerReturn toDomain(CustomerReturnJpa record, List<ReturnItem> items);
}
