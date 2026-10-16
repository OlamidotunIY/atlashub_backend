package com.atlashub.pay.mandates.infrastructure.persistence.mappers;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.infrastructure.persistence.entities.PaymentMandateJpa;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
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
        imports = {Money.class, CurrencyCode.class, EmailAddress.class}
)
public interface PaymentMandateMapper extends DomainMapper<PaymentMandate, PaymentMandateJpa> {

    @Override
    @Mapping(target = "email", expression = "java(jpa.getEmail() == null ? null : new EmailAddress(jpa.getEmail()))")
    @Mapping(target = "amount", expression = "java(jpa.getAmount() == null ? null : Money.of(jpa.getAmount(), CurrencyCode.valueOf(jpa.getCurrency() != null ? jpa.getCurrency() : \"NGN\")))")
    PaymentMandate toDomain(PaymentMandateJpa jpa);

    @Override
    @Mapping(target = "email", expression = "java(domain.getEmail() == null ? null : domain.getEmail().value())")
    @Mapping(target = "amount", expression = "java(domain.getAmount() == null ? null : domain.getAmount().amount())")
    @Mapping(target = "currency", expression = "java(domain.getAmount() == null || domain.getAmount().currency() == null ? \"NGN\" : domain.getAmount().currency().name())")
    @Mapping(target = "version", ignore = true)
    PaymentMandateJpa toPersistence(PaymentMandate domain);
}
