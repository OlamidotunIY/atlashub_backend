package com.atlashub.commerce.storefront.infrastructure.persistence.mappers;

import com.atlashub.commerce.storefront.domain.entities.KotItem;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.KotItemJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface KotItemMapper {

    KotItem toDomain(KotItemJpa record);

    @Mapping(target = "version", ignore = true)
    KotItemJpa toPersistence(KotItem domain);
}
