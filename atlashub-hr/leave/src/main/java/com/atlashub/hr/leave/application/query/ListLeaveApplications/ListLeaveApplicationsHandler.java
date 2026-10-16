package com.atlashub.hr.leave.application.query.ListLeaveApplications;

import com.atlashub.hr.leave.domain.entities.LeaveApplication;
import com.atlashub.hr.leave.domain.entities.LeaveType;
import com.atlashub.hr.leave.domain.repositories.LeaveApplicationRepository;
import com.atlashub.hr.leave.domain.repositories.LeaveTypeRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListLeaveApplicationsHandler extends Query<ListLeaveApplicationsQuery, List<LeaveApplicationResult>> {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public ListLeaveApplicationsHandler(
            LeaveApplicationRepository leaveApplicationRepository,
            LeaveTypeRepository leaveTypeRepository
    ) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @Override
    public List<LeaveApplicationResult> execute(ListLeaveApplicationsQuery query) {
        List<LeaveApplication> applications;

        if (query.organizationId() != null && query.employeeId() != null && query.status() != null) {
            applications = leaveApplicationRepository.findByOrganizationIdAndEmployeeIdAndStatus(
                    query.organizationId(),
                    query.employeeId(),
                    query.status()
            );
        } else if (query.organizationId() != null && query.employeeId() != null) {
            applications = leaveApplicationRepository.findByOrganizationIdAndEmployeeId(
                    query.organizationId(),
                    query.employeeId()
            );
        } else if (query.employeeId() != null && query.status() != null) {
            applications = leaveApplicationRepository.findByEmployeeIdAndStatus(
                    query.employeeId(),
                    query.status()
            );
        } else if (query.employeeId() != null) {
            applications = leaveApplicationRepository.findByEmployeeId(query.employeeId());
        } else if (query.organizationId() != null && query.status() != null) {
            applications = leaveApplicationRepository.findByOrganizationIdAndStatus(
                    query.organizationId(),
                    query.status()
            );
        } else if (query.organizationId() != null) {
            applications = leaveApplicationRepository.findByOrganizationId(query.organizationId());
        } else {
            applications = List.of();
        }

        return applications.stream()
                .map(this::toResult)
                .toList();
    }

    private LeaveApplicationResult toResult(LeaveApplication application) {
        String typeName = leaveTypeRepository.findById(application.getLeaveTypeId())
                .map(LeaveType::getName)
                .orElse("Unknown");

        return new LeaveApplicationResult(
                application.getId(),
                application.getEmployeeId(),
                "Employee #" + application.getEmployeeId(),
                typeName,
                application.getStartDate(),
                application.getEndDate(),
                application.getDaysRequested(),
                application.getReason(),
                application.getStatus(),
                application.getApprovedBy(),
                application.getApprovedAt()
        );
    }
}
