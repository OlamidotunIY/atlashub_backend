package com.atlashub.hr.leave.domain.entities;

import com.atlashub.hr.leave.domain.events.LeaveApprovedEvent;
import com.atlashub.hr.leave.domain.events.LeaveRejectedEvent;
import com.atlashub.hr.leave.domain.exceptions.InvalidLeaveStateException;
import com.atlashub.hr.leave.domain.valueobject.LeaveStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
public class LeaveApplication extends AggregateRoot<Long> {

    private final Long id;
    private final Long employeeId;
    private final Long organizationId;
    private final Long leaveTypeId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer daysRequested;
    private final String reason;
    private LeaveStatus status;
    private Long approvedBy;
    private ZonedDateTime approvedAt;

    public LeaveApplication(
            Long id,
            Long employeeId,
            Long organizationId,
            Long leaveTypeId,
            LocalDate startDate,
            LocalDate endDate,
            Integer daysRequested,
            String reason,
            LeaveStatus status,
            Long approvedBy,
            ZonedDateTime approvedAt
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.organizationId = organizationId;
        this.leaveTypeId = leaveTypeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.daysRequested = daysRequested;
        this.reason = reason;
        this.status = status;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
    }

    public static LeaveApplication create(
            Long id,
            Long employeeId,
            Long organizationId,
            Long leaveTypeId,
            LocalDate startDate,
            LocalDate endDate,
            Integer daysRequested,
            String reason
    ) {
        if (employeeId == null) {
            throw new InvalidLeaveStateException("Employee ID must not be null");
        }
        if (organizationId == null) {
            throw new InvalidLeaveStateException("Organization ID must not be null");
        }
        if (leaveTypeId == null) {
            throw new InvalidLeaveStateException("Leave type ID must not be null");
        }
        if (startDate == null || endDate == null) {
            throw new InvalidLeaveStateException("Leave start and end dates must not be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new InvalidLeaveStateException("Leave end date cannot be before start date");
        }
        if (daysRequested == null || daysRequested <= 0) {
            throw new InvalidLeaveStateException("Days requested must be greater than zero");
        }

        return new LeaveApplication(
                id,
                employeeId,
                organizationId,
                leaveTypeId,
                startDate,
                endDate,
                daysRequested,
                reason != null ? reason.trim() : null,
                LeaveStatus.PENDING,
                null,
                null
        );
    }

    public void approve(Long approverId) {
        if (this.status != LeaveStatus.PENDING) {
            throw new InvalidLeaveStateException("Application is not in PENDING state");
        }
        this.status = LeaveStatus.APPROVED;
        this.approvedBy = approverId;
        this.approvedAt = ZonedDateTime.now();

        registerEvent(LeaveApprovedEvent.of(
                id,
                employeeId,
                organizationId,
                leaveTypeId,
                startDate,
                endDate,
                daysRequested,
                approverId
        ));
    }

    public void reject(String rejectionReason) {
        if (this.status != LeaveStatus.PENDING) {
            throw new InvalidLeaveStateException("Application is not in PENDING state");
        }
        this.status = LeaveStatus.REJECTED;

        registerEvent(LeaveRejectedEvent.of(
                id,
                employeeId,
                organizationId,
                leaveTypeId,
                rejectionReason
        ));
    }

    public void cancel() {
        if (this.status != LeaveStatus.PENDING && this.status != LeaveStatus.APPROVED) {
            throw new InvalidLeaveStateException("Cannot cancel application in " + status + " state");
        }
        this.status = LeaveStatus.CANCELLED;
    }

    @Override
    public Long getId() {
        return id;
    }
}
