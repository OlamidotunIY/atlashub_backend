package com.atlashub.hr.leave.application.query.ListLeaveApplications;

import com.atlashub.hr.leave.domain.valueobject.LeaveStatus;

public record ListLeaveApplicationsQuery(
        Long organizationId,
        Long employeeId,
        LeaveStatus status
) {}
