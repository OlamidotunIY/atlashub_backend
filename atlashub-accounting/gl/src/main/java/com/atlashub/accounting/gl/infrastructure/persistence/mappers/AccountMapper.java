package com.atlashub.accounting.gl.infrastructure.persistence.mappers;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.AccountJpa;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ValueObjectMapper.class}
)
public interface AccountMapper extends DomainMapper<Account, AccountJpa> {
}
