package com.atlashub.eventbus.domain.repository;

import com.atlashub.eventbus.domain.model.OutboxMessage;
import com.atlashub.eventbus.domain.valueobject.OutboxStatus;
import java.util.List;
import java.util.Optional;

public interface OutboxMessageRepository {
    void save(OutboxMessage message);
    List<OutboxMessage> findPendingMessagesBatch(int batchSize);
    void saveAll(List<OutboxMessage> messages);
    Optional<OutboxMessage> findById(String id);
}
