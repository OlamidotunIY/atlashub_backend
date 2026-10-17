package com.atlashub.support.infrastructure.persistence.repositories;

import com.atlashub.support.domain.valueobject.TicketCategory;
import com.atlashub.support.domain.valueobject.TicketPriority;
import com.atlashub.support.domain.valueobject.TicketStatus;
import com.atlashub.support.infrastructure.persistence.entities.SupportTicketJpa;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataSupportTicketRepository extends JpaRepository<SupportTicketJpa, Long> {

    Optional<SupportTicketJpa> findByTicketNumber(String ticketNumber);

    List<SupportTicketJpa> findByOrganizationId(Long organizationId);

    List<SupportTicketJpa> findByOrganizationIdAndStatus(Long organizationId, TicketStatus status);

    List<SupportTicketJpa> findByAssignedToAndStatus(Long agentId, TicketStatus status);

    @Query("SELECT t FROM SupportTicketJpa t WHERE t.status IN (com.atlashub.support.domain.valueobject.TicketStatus.OPEN, com.atlashub.support.domain.valueobject.TicketStatus.ASSIGNED, com.atlashub.support.domain.valueobject.TicketStatus.IN_PROGRESS) " +
           "AND t.slaBreached = false " +
           "AND ((t.firstRespondedAt IS NULL AND t.firstResponseDeadline < :now) OR t.resolutionDeadline < :now)")
    List<SupportTicketJpa> findSlaOverdueTickets(@Param("now") ZonedDateTime now);

    @Query("SELECT t FROM SupportTicketJpa t WHERE t.status = com.atlashub.support.domain.valueobject.TicketStatus.RESOLVED AND t.resolvedAt < :cutoff")
    List<SupportTicketJpa> findIdleResolvedTickets(@Param("cutoff") ZonedDateTime cutoff);

    @Query("SELECT t FROM SupportTicketJpa t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:slaBreached IS NULL OR t.slaBreached = :slaBreached)")
    List<SupportTicketJpa> searchTickets(
            @Param("status") TicketStatus status,
            @Param("category") TicketCategory category,
            @Param("priority") TicketPriority priority,
            @Param("slaBreached") Boolean slaBreached
    );

    @Query("SELECT t FROM SupportTicketJpa t WHERE t.createdAt >= :from AND t.createdAt <= :to")
    List<SupportTicketJpa> findCreatedBetween(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);
}
