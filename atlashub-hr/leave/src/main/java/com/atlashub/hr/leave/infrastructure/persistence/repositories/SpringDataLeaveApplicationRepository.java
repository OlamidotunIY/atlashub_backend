package com.atlashub.hr.leave.infrastructure.persistence.repositories;

import com.atlashub.hr.leave.domain.valueobject.LeaveStatus;
import com.atlashub.hr.leave.infrastructure.persistence.entities.LeaveApplicationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SpringDataLeaveApplicationRepository extends JpaRepository<LeaveApplicationJpaEntity, Long> {

    List<LeaveApplicationJpaEntity> findByOrganizationId(Long organizationId);

    List<LeaveApplicationJpaEntity> findByOrganizationIdAndStatus(Long organizationId, LeaveStatus status);

    List<LeaveApplicationJpaEntity> findByEmployeeId(Long employeeId);

    List<LeaveApplicationJpaEntity> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    List<LeaveApplicationJpaEntity> findByOrganizationIdAndEmployeeIdAndStatus(Long organizationId, Long employeeId, LeaveStatus status);

    List<LeaveApplicationJpaEntity> findByOrganizationIdAndEmployeeId(Long organizationId, Long employeeId);

    @Query("SELECT COUNT(a) > 0 FROM LeaveApplicationJpaEntity a " +
            "WHERE a.employeeId = :employeeId " +
            "AND a.status IN :statuses " +
            "AND a.startDate <= :endDate " +
            "AND a.endDate >= :startDate")
    boolean existsByEmployeeIdAndStatusInAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("statuses") List<LeaveStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
