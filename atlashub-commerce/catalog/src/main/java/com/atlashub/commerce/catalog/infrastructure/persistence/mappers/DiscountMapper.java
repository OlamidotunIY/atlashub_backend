package com.atlashub.commerce.catalog.infrastructure.persistence.mappers;

import com.atlashub.commerce.catalog.domain.entities.Discount;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.DiscountJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface DiscountMapper extends DomainMapper<Discount, DiscountJpa> {
}
