package com.atlashub.accounts.infrastructure.persistence.mappers;

import com.atlashub.accounts.domain.model.User;
import com.atlashub.accounts.infrastructure.persistence.entities.UserJPA;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface UserMapper extends DomainMapper<User, UserJPA> {
}
