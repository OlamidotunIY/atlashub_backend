package com.atlashub.support.infrastructure.persistence.mappers;

import com.atlashub.support.domain.entities.TicketSlaConfig;
import com.atlashub.support.infrastructure.persistence.entities.TicketSlaConfigJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TicketSlaConfigMapper {

    TicketSlaConfig toDomain(TicketSlaConfigJpa jpa);

    @Mapping(target = "version", ignore = true)
    TicketSlaConfigJpa toPersistence(TicketSlaConfig domain);
}
