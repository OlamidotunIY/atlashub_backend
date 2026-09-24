package com.atlashub.eventbus.adapter.out.persistence.repository;

import com.atlashub.eventbus.adapter.out.persistence.entity.OutboxMessageJpaEntity;
import com.atlashub.eventbus.domain.valueobject.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataOutboxMessageRepository extends JpaRepository<OutboxMessageJpaEntity, String> {

    @Query("SELECT o FROM OutboxMessageJpaEntity o WHERE o.status = :status ORDER BY o.createdAt ASC")
    List<OutboxMessageJpaEntity> findByStatusOrderByCreatedAtAsc(@Param("status") OutboxStatus status, Pageable pageable);

    @Query("SELECT o FROM OutboxMessageJpaEntity o WHERE o.payload LIKE %:eventId%")
    List<OutboxMessageJpaEntity> findByPayloadContaining(@Param("eventId") String eventId);
}
