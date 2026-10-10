package com.atlashub.commerce.storefront.infrastructure.persistence.mappers;

import com.atlashub.commerce.storefront.domain.entities.HospitalityTable;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.HospitalityTableJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface HospitalityTableMapper extends DomainMapper<HospitalityTable, HospitalityTableJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    HospitalityTableJpa toPersistence(HospitalityTable domain);
}
