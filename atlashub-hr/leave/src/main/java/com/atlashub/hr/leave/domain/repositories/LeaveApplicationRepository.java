package com.atlashub.hr.leave.domain.repositories;

import com.atlashub.hr.leave.domain.entities.LeaveApplication;
import com.atlashub.hr.leave.domain.valueobject.LeaveStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveApplicationRepository extends Repository<LeaveApplication> {

    List<LeaveApplication> findByOrganizationId(Long organizationId);

    List<LeaveApplication> findByOrganizationIdAndStatus(Long organizationId, LeaveStatus status);

    List<LeaveApplication> findByEmployeeId(Long employeeId);

    List<LeaveApplication> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    List<LeaveApplication> findByOrganizationIdAndEmployeeIdAndStatus(Long organizationId, Long employeeId, LeaveStatus status);

    List<LeaveApplication> findByOrganizationIdAndEmployeeId(Long organizationId, Long employeeId);

    boolean existsByEmployeeIdAndStatusInAndDateRange(
            Long employeeId,
            List<LeaveStatus> statuses,
            LocalDate startDate,
            LocalDate endDate
    );
}
