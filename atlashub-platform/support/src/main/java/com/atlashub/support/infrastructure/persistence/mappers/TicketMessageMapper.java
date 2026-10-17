package com.atlashub.support.infrastructure.persistence.mappers;

import com.atlashub.support.domain.entities.TicketMessage;
import com.atlashub.support.infrastructure.persistence.entities.TicketMessageJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {SupportJsonMapper.class}
)
public interface TicketMessageMapper {

    @Mapping(target = "attachmentUrls", source = "attachmentUrlsJson")
    @Mapping(target = "isInternalNote", source = "internalNote")
    TicketMessage toDomain(TicketMessageJpa jpa);

    @Mapping(target = "attachmentUrlsJson", source = "attachmentUrls")
    @Mapping(target = "isInternalNote", source = "internalNote")
    @Mapping(target = "version", ignore = true)
    TicketMessageJpa toPersistence(TicketMessage domain);
}
