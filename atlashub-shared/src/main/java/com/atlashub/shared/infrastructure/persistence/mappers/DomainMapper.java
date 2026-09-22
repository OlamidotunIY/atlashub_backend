package com.atlashub.shared.infrastructure.persistence.mappers;

import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import org.mapstruct.Mapping;

public interface DomainMapper<TDomain extends AggregateRoot<Long>, TRecord extends BaseJpaEntity> {
    
    TDomain toDomain(TRecord record);
    
    @Mapping(target = "version", ignore = true)
    TRecord toPersistence(TDomain domain);
}
