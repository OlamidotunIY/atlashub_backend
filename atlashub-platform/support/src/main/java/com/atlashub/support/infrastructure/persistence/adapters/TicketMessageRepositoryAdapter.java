package com.atlashub.support.infrastructure.persistence.adapters;

import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import com.atlashub.support.domain.entities.TicketMessage;
import com.atlashub.support.domain.repositories.TicketMessageRepository;
import com.atlashub.support.infrastructure.persistence.entities.TicketMessageJpa;
import com.atlashub.support.infrastructure.persistence.mappers.TicketMessageMapper;
import com.atlashub.support.infrastructure.persistence.repositories.SpringDataTicketMessageRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TicketMessageRepositoryAdapter implements TicketMessageRepository {

    private final SpringDataTicketMessageRepository springDataRepo;
    private final TicketMessageMapper mapper;
    private final DomainSequenceGenerator sequenceGenerator;

    public TicketMessageRepositoryAdapter(
            SpringDataTicketMessageRepository springDataRepo,
            TicketMessageMapper mapper,
            DomainSequenceGenerator sequenceGenerator
    ) {
        this.springDataRepo = springDataRepo;
        this.mapper = mapper;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("support_message_seq");
    }

    @Override
    public TicketMessage save(TicketMessage entity) {
        TicketMessageJpa jpa = mapper.toPersistence(entity);
        TicketMessageJpa saved = springDataRepo.save(jpa);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<TicketMessage> findById(Long id) {
        return springDataRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataRepo.existsById(id);
    }

    @Override
    public List<TicketMessage> findAll() {
        return springDataRepo.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId) {
        return springDataRepo.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
