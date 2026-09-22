package com.atlashub.authentication.infrastructure.persistence.mappers;

import com.atlashub.authentication.domain.entities.OtpVerification;
import com.atlashub.authentication.infrastructure.persistence.entities.OtpVerificationJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface OtpVerificationMapper extends DomainMapper<OtpVerification, OtpVerificationJpa> {
}
