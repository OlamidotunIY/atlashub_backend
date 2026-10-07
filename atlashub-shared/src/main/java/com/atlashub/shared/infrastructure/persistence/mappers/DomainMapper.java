package com.atlashub.shared.infrastructure.persistence.mappers;

import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

public interface DomainMapper<TDomain extends AggregateRoot<Long>, TRecord extends BaseJpaEntity> {
    
    TDomain toDomain(TRecord record);
    
    @Mapping(target = "version", ignore = true)
    TRecord toPersistence(TDomain domain);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updatePersistence(TDomain domain, @MappingTarget TRecord record);
}
