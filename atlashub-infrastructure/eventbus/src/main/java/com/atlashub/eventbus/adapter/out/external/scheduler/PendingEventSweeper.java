package com.atlashub.eventbus.adapter.out.external.scheduler;

import com.atlashub.eventbus.adapter.out.persistence.entity.EventDeliveryTrackerJpaEntity;
import com.atlashub.eventbus.adapter.out.persistence.repository.JpaEventDeliveryTrackerRepository;
import com.atlashub.eventbus.application.port.MessageBrokerPort;
import com.atlashub.eventbus.domain.model.OutboxMessage;
import com.atlashub.eventbus.domain.repository.OutboxMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class PendingEventSweeper {

    private static final Logger log = LoggerFactory.getLogger(PendingEventSweeper.class);
    private static final String LOCK_KEY = "lock:event-delivery-sweeper";
    
    private final JpaEventDeliveryTrackerRepository trackerRepository;
    private final OutboxMessageRepository outboxMessageRepository;
    private final MessageBrokerPort messageBrokerPort;
    private final StringRedisTemplate redisTemplate;

    public PendingEventSweeper(JpaEventDeliveryTrackerRepository trackerRepository,
                               OutboxMessageRepository outboxMessageRepository,
                               MessageBrokerPort messageBrokerPort,
                               StringRedisTemplate redisTemplate) {
        this.trackerRepository = trackerRepository;
        this.outboxMessageRepository = outboxMessageRepository;
        this.messageBrokerPort = messageBrokerPort;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedDelayString = "300000") // Run every 5 minutes
    public void sweepPendingEvents() {
        // Attempt to acquire Redis Lock for 4 minutes
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "locked", Duration.ofMinutes(4));
        if (Boolean.FALSE.equals(acquired)) {
            log.trace("Sweeper lock is already acquired by another instance. Skipping.");
            return;
        }

        try {
            // Find trackers pending for more than 10 minutes
            ZonedDateTime tenMinsAgo = ZonedDateTime.now().minusMinutes(10);
            List<EventDeliveryTrackerJpaEntity> pendingTrackers = trackerRepository.findByStatusAndUpdatedAtBefore("PENDING", tenMinsAgo);

            if (!pendingTrackers.isEmpty()) {
                log.info("Found {} pending event trackers that need to be resent.", pendingTrackers.size());

                for (EventDeliveryTrackerJpaEntity tracker : pendingTrackers) {
                    // Fetch original outbox payload
                    Optional<OutboxMessage> outboxMessageOpt = outboxMessageRepository.findByEventId(tracker.getEventId());
                    
                    if (outboxMessageOpt.isPresent()) {
                        OutboxMessage message = outboxMessageOpt.get();
                        log.info("Resending event {} for consumer {}", tracker.getEventId(), tracker.getConsumerId());
                        messageBrokerPort.send(message.getTopic(), message.getId(), message.getPayload());
                        
                        // Update tracker timestamp to prevent immediate resending
                        tracker.updateStatus("PENDING"); 
                        trackerRepository.save(tracker);
                    } else {
                        log.warn("Outbox message not found for pending event ID: {}", tracker.getEventId());
                        // If there's no payload anymore, we can't retry. Mark DLQ.
                        tracker.updateStatus("DLQ");
                        trackerRepository.save(tracker);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occurred during pending event sweep", e);
        } finally {
            // Optional: We can delete the lock or let it expire. Letting it expire ensures safety across clock drifts.
        }
    }
}
