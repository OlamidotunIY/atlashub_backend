package com.atlashub.iam.infrastructure.persistence.mappers;

import com.atlashub.iam.domain.entities.ApiKey;
import com.atlashub.iam.infrastructure.persistence.entities.ApiKeyJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    uses = {ValueObjectMapper.class}
)
public interface ApiKeyMapper extends DomainMapper<ApiKey, ApiKeyJpa> {
}
