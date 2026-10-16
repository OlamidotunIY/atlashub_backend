package com.atlashub.hr.leave.domain.repositories;

import com.atlashub.hr.leave.domain.entities.LeaveBalance;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends Repository<LeaveBalance> {

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(Long employeeId, Long leaveTypeId, int year);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYearWithLock(Long employeeId, Long leaveTypeId, int year);

    List<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, int year);

    List<LeaveBalance> findByEmployeeId(Long employeeId);
}
