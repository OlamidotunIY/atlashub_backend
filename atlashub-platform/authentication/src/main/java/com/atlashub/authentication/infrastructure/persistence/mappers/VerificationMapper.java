package com.atlashub.authentication.infrastructure.persistence.mappers;

import com.atlashub.authentication.domain.entities.Verification;
import com.atlashub.authentication.infrastructure.persistence.entities.VerificationJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface VerificationMapper extends DomainMapper<Verification, VerificationJpa> {
}
