package com.atlashub.eventbus.adapter.in.internal;

import com.atlashub.eventbus.adapter.out.persistence.entity.EventDeliveryTrackerJpaEntity;
import com.atlashub.eventbus.adapter.out.persistence.repository.JpaEventDeliveryTrackerRepository;
import com.atlashub.shared.application.port.EventTrackerPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class EventTrackerApiImpl implements
        EventTrackerPort,
        com.atlashub.eventbus.application.port.EventTrackerPort {

    private final JpaEventDeliveryTrackerRepository trackerRepository;
    
    // In-memory registry for Dynamic Consumer Subscriptions
    private final ConcurrentMap<String, Set<String>> subscriptions = new ConcurrentHashMap<>();

    public EventTrackerApiImpl(JpaEventDeliveryTrackerRepository trackerRepository) {
        this.trackerRepository = trackerRepository;
    }

    @Override
    public void registerSubscription(String eventType, String consumerId) {
        subscriptions.computeIfAbsent(eventType, k -> ConcurrentHashMap.newKeySet()).add(consumerId);
    }

    @Override
    @Transactional
    public void markSuccess(String eventId, String consumerId) {
        Optional<EventDeliveryTrackerJpaEntity> entityOpt = trackerRepository.findById(
                new EventDeliveryTrackerJpaEntity.TrackerId(eventId, consumerId));
        
        if (entityOpt.isPresent()) {
            entityOpt.get().updateStatus("SUCCESS");
        } else {
            trackerRepository.save(new EventDeliveryTrackerJpaEntity(eventId, consumerId, "SUCCESS"));
        }
    }

    @Override
    @Transactional
    public void markDlq(String eventId, String consumerId) {
        Optional<EventDeliveryTrackerJpaEntity> entityOpt = trackerRepository.findById(
                new EventDeliveryTrackerJpaEntity.TrackerId(eventId, consumerId));
        
        if (entityOpt.isPresent()) {
            entityOpt.get().updateStatus("DLQ");
        } else {
            trackerRepository.save(new EventDeliveryTrackerJpaEntity(eventId, consumerId, "DLQ"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProcessed(String eventId, String consumerId) {
        return trackerRepository.findById(new EventDeliveryTrackerJpaEntity.TrackerId(eventId, consumerId))
                .map(t -> "SUCCESS".equals(t.getStatus()))
                .orElse(false);
    }

    @Override
    public Set<String> getExpectedConsumers(String eventType) {
        return subscriptions.getOrDefault(eventType, Collections.emptySet());
    }

    @Override
    @Transactional
    public void createPendingTrackers(String eventId, Set<String> consumerIds) {
        for (String consumerId : consumerIds) {
            Optional<EventDeliveryTrackerJpaEntity> existing = trackerRepository.findById(
                    new EventDeliveryTrackerJpaEntity.TrackerId(eventId, consumerId));
            if (existing.isEmpty()) {
                trackerRepository.save(new EventDeliveryTrackerJpaEntity(eventId, consumerId, "PENDING"));
            }
        }
    }
}
