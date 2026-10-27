package com.atlashub.eventbus.domain.repository;

import com.atlashub.eventbus.domain.model.OutboxMessage;
import com.atlashub.eventbus.domain.valueobject.OutboxStatus;
import java.util.List;
import java.util.Optional;
import java.time.ZonedDateTime;

public interface OutboxMessageRepository {
    void save(OutboxMessage message);
    List<OutboxMessage> findPendingMessagesBatch(int batchSize);
    void saveAll(List<OutboxMessage> messages);
    Optional<OutboxMessage> findById(String id);
    Optional<OutboxMessage> findByEventId(String eventId);
    List<OutboxMessage> findReplayCandidates(String topic, ZonedDateTime from, ZonedDateTime to, int limit);
}
