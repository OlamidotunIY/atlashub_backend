package com.atlashub.pay.controls.infrastructure.persistence.mappers;

import com.atlashub.pay.controls.domain.entities.PaymentControlDecision;
import com.atlashub.pay.controls.infrastructure.persistence.entities.PaymentControlDecisionJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ValueObjectMapper.class, ControlsJsonMapper.class}
)
public interface PaymentControlDecisionMapper extends DomainMapper<PaymentControlDecision, PaymentControlDecisionJpa> {

    @Override
    @Mapping(target = "version", ignore = true)
    PaymentControlDecisionJpa toPersistence(PaymentControlDecision domain);

    @Override
    PaymentControlDecision toDomain(PaymentControlDecisionJpa jpa);
}
