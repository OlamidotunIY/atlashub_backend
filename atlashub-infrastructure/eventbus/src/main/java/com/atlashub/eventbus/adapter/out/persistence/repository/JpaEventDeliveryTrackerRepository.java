package com.atlashub.eventbus.adapter.out.persistence.repository;

import com.atlashub.eventbus.adapter.out.persistence.entity.EventDeliveryTrackerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface JpaEventDeliveryTrackerRepository extends JpaRepository<EventDeliveryTrackerJpaEntity, EventDeliveryTrackerJpaEntity.TrackerId> {

    @Modifying
    @Query(value = """
            INSERT INTO event_delivery_tracker (event_id, consumer_id, status, updated_at)
            VALUES (:eventId, :consumerId, :status, :updatedAt)
            ON DUPLICATE KEY UPDATE status = :status, updated_at = :updatedAt
            """, nativeQuery = true)
    void upsertStatus(@Param("eventId") String eventId, @Param("consumerId") String consumerId,
                      @Param("status") String status, @Param("updatedAt") ZonedDateTime updatedAt);

    @Modifying
    @Query(value = """
            INSERT INTO event_delivery_tracker (event_id, consumer_id, status, updated_at)
            VALUES (:eventId, :consumerId, 'PENDING', :updatedAt)
            ON DUPLICATE KEY UPDATE event_id = event_id
            """, nativeQuery = true)
    void insertPendingIfAbsent(@Param("eventId") String eventId, @Param("consumerId") String consumerId,
                               @Param("updatedAt") ZonedDateTime updatedAt);

    @Query("SELECT t FROM EventDeliveryTrackerJpaEntity t WHERE t.status = :status AND t.updatedAt < :beforeTime")
    List<EventDeliveryTrackerJpaEntity> findByStatusAndUpdatedAtBefore(
            @Param("status") String status, 
            @Param("beforeTime") ZonedDateTime beforeTime
    );
}
