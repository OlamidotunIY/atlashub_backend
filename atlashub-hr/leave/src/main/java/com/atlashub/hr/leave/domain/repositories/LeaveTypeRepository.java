package com.atlashub.hr.leave.domain.repositories;

import com.atlashub.hr.leave.domain.entities.LeaveType;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface LeaveTypeRepository extends Repository<LeaveType> {

    List<LeaveType> findByOrganizationId(Long organizationId);
}
