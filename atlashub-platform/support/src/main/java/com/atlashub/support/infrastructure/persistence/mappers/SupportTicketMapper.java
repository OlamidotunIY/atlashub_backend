package com.atlashub.support.infrastructure.persistence.mappers;

import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import com.atlashub.support.domain.entities.SupportTicket;
import com.atlashub.support.infrastructure.persistence.entities.SupportTicketJpa;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface SupportTicketMapper extends DomainMapper<SupportTicket, SupportTicketJpa> {
}
