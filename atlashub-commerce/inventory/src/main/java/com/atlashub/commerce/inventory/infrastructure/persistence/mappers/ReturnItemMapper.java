package com.atlashub.commerce.inventory.infrastructure.persistence.mappers;

import com.atlashub.commerce.inventory.domain.entities.ReturnItem;
import com.atlashub.commerce.inventory.infrastructure.persistence.entities.ReturnItemJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface ReturnItemMapper {

    ReturnItem toDomain(ReturnItemJpa record);

    ReturnItemJpa toPersistence(ReturnItem domain);
}
