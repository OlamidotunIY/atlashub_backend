package com.atlashub.billing.infrastructure.persistence.mappers;

import com.atlashub.billing.domain.entities.InvoiceLineItem;
import com.atlashub.billing.infrastructure.persistence.entities.InvoiceLineItemJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface InvoiceLineItemMapper extends DomainMapper<InvoiceLineItem, InvoiceLineItemJpa> {
}
