package com.atlashub.hr.leave.infrastructure.persistence.repositories;

import com.atlashub.hr.leave.infrastructure.persistence.entities.LeaveBalanceJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataLeaveBalanceRepository extends JpaRepository<LeaveBalanceJpaEntity, Long> {

    Optional<LeaveBalanceJpaEntity> findByEmployeeIdAndLeaveTypeIdAndYear(
            Long employeeId,
            Long leaveTypeId,
            Integer year
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM LeaveBalanceJpaEntity b WHERE b.employeeId = :employeeId AND b.leaveTypeId = :leaveTypeId AND b.year = :year")
    Optional<LeaveBalanceJpaEntity> findByEmployeeIdAndLeaveTypeIdAndYearWithLock(
            @Param("employeeId") Long employeeId,
            @Param("leaveTypeId") Long leaveTypeId,
            @Param("year") Integer year
    );

    List<LeaveBalanceJpaEntity> findByEmployeeIdAndYear(Long employeeId, Integer year);

    List<LeaveBalanceJpaEntity> findByEmployeeId(Long employeeId);
}
