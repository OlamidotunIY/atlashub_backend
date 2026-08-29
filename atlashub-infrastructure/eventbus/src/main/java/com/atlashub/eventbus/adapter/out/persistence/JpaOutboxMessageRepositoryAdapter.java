package com.atlashub.eventbus.adapter.out.persistence;

import com.atlashub.eventbus.adapter.out.persistence.repository.SpringDataOutboxMessageRepository;
import com.atlashub.eventbus.domain.model.OutboxMessage;
import com.atlashub.eventbus.domain.repository.OutboxMessageRepository;
import com.atlashub.eventbus.adapter.out.persistence.entity.OutboxMessageJpaEntity;
import com.atlashub.eventbus.domain.valueobject.OutboxStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JpaOutboxMessageRepositoryAdapter implements OutboxMessageRepository {

    private final SpringDataOutboxMessageRepository repository;

    public JpaOutboxMessageRepositoryAdapter(SpringDataOutboxMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(OutboxMessage message) {
        OutboxMessageJpaEntity entity = repository.findById(message.getId()).orElse(null);
        if (entity != null) {
            entity.updateStatus(message.getStatus(), message.getProcessedAt());
            repository.save(entity);
        } else {
            repository.save(mapToEntity(message));
        }
    }

    @Override
    public List<OutboxMessage> findPendingMessagesBatch(int batchSize) {
        return repository.findByStatusOrderByCreatedAtAsc(
            OutboxStatus.PENDING,
            PageRequest.of(0, batchSize)
        ).stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public void saveAll(List<OutboxMessage> messages) {
        List<OutboxMessageJpaEntity> entitiesToSave = messages.stream().map(domain -> {
            OutboxMessageJpaEntity entity = repository.findById(domain.getId()).orElse(null);
            if (entity != null) {
                entity.updateStatus(domain.getStatus(), domain.getProcessedAt());
                return entity;
            } else {
                return mapToEntity(domain);
            }
        }).collect(Collectors.toList());
        repository.saveAll(entitiesToSave);
    }

    @Override
    public Optional<OutboxMessage> findById(String id) {
        return repository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<OutboxMessage> findByEventId(String eventId) {
        // 1. Fast path: future events will have eventId as their primary key
        Optional<OutboxMessageJpaEntity> entity = repository.findById(eventId);
        if (entity.isPresent()) {
            return entity.map(this::mapToDomain);
        }

        // 2. Backward compatibility fallback: old events have random UUIDs as PK,
        // so we must search the JSON payload to find the eventId
        List<OutboxMessageJpaEntity> fallbackEntities = repository.findByPayloadContaining(eventId);
        if (!fallbackEntities.isEmpty()) {
            return Optional.of(mapToDomain(fallbackEntities.get(0)));
        }

        return Optional.empty();
    }

    private OutboxMessageJpaEntity mapToEntity(OutboxMessage domain) {
        return new OutboxMessageJpaEntity(
                domain.getId(),
                domain.getTopic(),
                domain.getPayload(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getProcessedAt(),
                null
        );
    }

    private OutboxMessage mapToDomain(OutboxMessageJpaEntity entity) {
        return new OutboxMessage(
                entity.getId(),
                entity.getTopic(),
                entity.getPayload(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getProcessedAt()
        );
    }
}
