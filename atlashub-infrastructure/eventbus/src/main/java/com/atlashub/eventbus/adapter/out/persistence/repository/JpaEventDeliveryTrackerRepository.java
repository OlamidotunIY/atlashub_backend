package com.atlashub.eventbus.adapter.out.persistence.repository;

import com.atlashub.eventbus.adapter.out.persistence.entity.EventDeliveryTrackerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface JpaEventDeliveryTrackerRepository extends JpaRepository<EventDeliveryTrackerJpaEntity, EventDeliveryTrackerJpaEntity.TrackerId> {

    @Query("SELECT t FROM EventDeliveryTrackerJpaEntity t WHERE t.status = :status AND t.updatedAt < :beforeTime")
    List<EventDeliveryTrackerJpaEntity> findByStatusAndUpdatedAtBefore(
            @Param("status") String status, 
            @Param("beforeTime") ZonedDateTime beforeTime
    );
}
