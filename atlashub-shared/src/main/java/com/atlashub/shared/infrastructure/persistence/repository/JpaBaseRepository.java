package com.atlashub.shared.infrastructure.persistence.repository;

import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public abstract class JpaBaseRepository<TDomain extends AggregateRoot<Long>, TRecord extends BaseJpaEntity> implements Repository<TDomain> {
    protected final JpaRepository<TRecord, Long> springDataRepository;
    protected final DomainMapper<TDomain, TRecord> mapper;
    protected final DomainSequenceGenerator sequenceGenerator;
    protected final DomainEventPublisher eventPublisher;

    protected JpaBaseRepository(JpaRepository<TRecord, Long> springDataRepository, DomainMapper<TDomain, TRecord> mapper, DomainSequenceGenerator sequenceGenerator, DomainEventPublisher eventPublisher) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
        this.sequenceGenerator = sequenceGenerator;
        this.eventPublisher = eventPublisher;
    }

    protected abstract String getSequenceName();

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity(getSequenceName());
    }

    @Override
    public Optional<TDomain> findById(Long id) {
        return springDataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }

    @Override
    @Transactional
    public TDomain save(TDomain entity) {
        TRecord record = mapper.toPersistence(entity);

        List<DomainEvent<?>> events = entity.pullDomainEvents();

        for (DomainEvent<?> event : events) {
            eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
        }

        TRecord saved = springDataRepository.save(record);

        return mapper.toDomain(saved);
    }
}
