package com.atlashub.pay.transfers.infrastructure.persistence.mappers;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.infrastructure.persistence.entities.PayoutJpa;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ValueObjectMapper.class},
        imports = {Money.class, CurrencyCode.class}
)
public interface PayoutMapper extends DomainMapper<Payout, PayoutJpa> {

    @Override
    @Mapping(target = "amount", expression = "java(jpa.getAmount() == null ? null : Money.of(jpa.getAmount(), CurrencyCode.valueOf(jpa.getCurrency() != null ? jpa.getCurrency() : \"NGN\")))")
    Payout toDomain(PayoutJpa jpa);

    @Override
    @Mapping(target = "amount", expression = "java(domain.getAmount() == null ? null : domain.getAmount().amount())")
    @Mapping(target = "currency", expression = "java(domain.getAmount() == null || domain.getAmount().currency() == null ? \"NGN\" : domain.getAmount().currency().name())")
    @Mapping(target = "version", ignore = true)
    PayoutJpa toPersistence(Payout domain);
}
