package com.atlashub.hr.leave.application.command.ApplyForLeave;

import com.atlashub.hr.leave.application.command.ApproveLeave.ApproveLeaveCommand;
import com.atlashub.hr.leave.application.command.ApproveLeave.ApproveLeaveHandler;
import com.atlashub.hr.leave.domain.entities.LeaveApplication;
import com.atlashub.hr.leave.domain.entities.LeaveType;
import com.atlashub.hr.leave.domain.exceptions.LeaveDatesConflictException;
import com.atlashub.hr.leave.domain.exceptions.LeaveTypeNotFoundException;
import com.atlashub.hr.leave.domain.repositories.LeaveApplicationRepository;
import com.atlashub.hr.leave.domain.repositories.LeaveTypeRepository;
import com.atlashub.hr.leave.domain.valueobject.LeaveStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Component
public class ApplyForLeaveHandler extends Command<ApplyForLeaveCommand, ApplyForLeaveResponse> {

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final ApproveLeaveHandler approveLeaveHandler;

    public ApplyForLeaveHandler(
            LeaveTypeRepository leaveTypeRepository,
            LeaveApplicationRepository leaveApplicationRepository,
            ApproveLeaveHandler approveLeaveHandler
    ) {
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.approveLeaveHandler = approveLeaveHandler;
    }

    @Override
    @Transactional
    public ApplyForLeaveResponse execute(ApplyForLeaveCommand command) {
        LeaveType leaveType = leaveTypeRepository.findById(command.leaveTypeId())
                .orElseThrow(() -> new LeaveTypeNotFoundException(command.leaveTypeId()));

        int daysRequested = calculateWorkingDays(command.startDate(), command.endDate());

        boolean hasConflict = leaveApplicationRepository.existsByEmployeeIdAndStatusInAndDateRange(
                command.employeeId(),
                List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED),
                command.startDate(),
                command.endDate()
        );

        if (hasConflict) {
            throw new LeaveDatesConflictException();
        }

        LeaveApplication application = LeaveApplication.create(
                null,
                command.employeeId(),
                command.organizationId(),
                command.leaveTypeId(),
                command.startDate(),
                command.endDate(),
                daysRequested,
                command.reason()
        );

        LeaveApplication saved = leaveApplicationRepository.save(application);

        if (Boolean.FALSE.equals(leaveType.getRequiresApproval())) {
            approveLeaveHandler.execute(new ApproveLeaveCommand(saved.getId(), 0L));
        }

        return new ApplyForLeaveResponse(saved.getId());
    }

    private int calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        int workingDays = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            current = current.plusDays(1);
        }
        return workingDays > 0 ? workingDays : 1;
    }
}
