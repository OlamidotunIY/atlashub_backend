package com.atlashub.commerce.catalog.infrastructure.persistence.mappers;

import com.atlashub.commerce.catalog.domain.entities.Vendor;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.VendorJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface VendorMapper extends DomainMapper<Vendor, VendorJpa> {
}
