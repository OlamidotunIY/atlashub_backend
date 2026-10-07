package com.atlashub.compliance.infrastructure.persistence.mappers;

import com.atlashub.compliance.domain.entities.ProviderOnboardingCase;
import com.atlashub.compliance.infrastructure.persistence.entities.ProviderOnboardingCaseJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProviderOnboardingCaseMapper extends DomainMapper<ProviderOnboardingCase, ProviderOnboardingCaseJpa> {
}
