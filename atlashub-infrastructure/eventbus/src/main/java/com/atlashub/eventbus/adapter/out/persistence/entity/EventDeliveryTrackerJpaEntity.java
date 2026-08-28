package com.atlashub.eventbus.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "event_delivery_tracker")
@IdClass(EventDeliveryTrackerJpaEntity.TrackerId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventDeliveryTrackerJpaEntity {

    @Id
    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Id
    @Column(name = "consumer_id", nullable = false)
    private String consumerId;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, SUCCESS, DLQ

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public EventDeliveryTrackerJpaEntity(String eventId, String consumerId, String status) {
        this.eventId = eventId;
        this.consumerId = consumerId;
        this.status = status;
        this.updatedAt = ZonedDateTime.now();
    }

    public void updateStatus(String status) {
        this.status = status;
        this.updatedAt = ZonedDateTime.now();
    }

    public static class TrackerId implements Serializable {
        private String eventId;
        private String consumerId;

        public TrackerId() {}

        public TrackerId(String eventId, String consumerId) {
            this.eventId = eventId;
            this.consumerId = consumerId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TrackerId trackerId = (TrackerId) o;
            return Objects.equals(eventId, trackerId.eventId) &&
                   Objects.equals(consumerId, trackerId.consumerId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventId, consumerId);
        }
    }
}
