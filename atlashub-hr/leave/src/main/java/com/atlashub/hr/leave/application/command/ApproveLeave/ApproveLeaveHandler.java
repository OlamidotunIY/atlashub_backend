package com.atlashub.hr.leave.application.command.ApproveLeave;

import com.atlashub.hr.leave.domain.entities.LeaveApplication;
import com.atlashub.hr.leave.domain.entities.LeaveBalance;
import com.atlashub.hr.leave.domain.entities.LeaveType;
import com.atlashub.hr.leave.domain.exceptions.LeaveApplicationNotFoundException;
import com.atlashub.hr.leave.domain.exceptions.LeaveTypeNotFoundException;
import com.atlashub.hr.leave.domain.repositories.LeaveApplicationRepository;
import com.atlashub.hr.leave.domain.repositories.LeaveBalanceRepository;
import com.atlashub.hr.leave.domain.repositories.LeaveTypeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApproveLeaveHandler extends Command<ApproveLeaveCommand, Void> {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public ApproveLeaveHandler(
            LeaveApplicationRepository leaveApplicationRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            LeaveTypeRepository leaveTypeRepository
    ) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @Override
    @Transactional
    public Void execute(ApproveLeaveCommand command) {
        LeaveApplication application = leaveApplicationRepository.findById(command.leaveApplicationId())
                .orElseThrow(() -> new LeaveApplicationNotFoundException(command.leaveApplicationId()));

        int year = application.getStartDate().getYear();

        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYearWithLock(
                application.getEmployeeId(),
                application.getLeaveTypeId(),
                year
        ).orElseGet(() -> {
            LeaveType type = leaveTypeRepository.findById(application.getLeaveTypeId())
                    .orElseThrow(() -> new LeaveTypeNotFoundException(application.getLeaveTypeId()));
            return LeaveBalance.create(null, application.getEmployeeId(), application.getLeaveTypeId(), year, type.getMaxDaysPerYear());
        });

        balance.deduct(application.getDaysRequested());
        application.approve(command.approverId());

        leaveBalanceRepository.save(balance);
        leaveApplicationRepository.save(application);

        return null;
    }
}
