package com.atlashub.hr.leave.application.command.CancelLeave;

import com.atlashub.hr.leave.domain.entities.LeaveApplication;
import com.atlashub.hr.leave.domain.exceptions.LeaveApplicationNotFoundException;
import com.atlashub.hr.leave.domain.repositories.LeaveApplicationRepository;
import com.atlashub.hr.leave.domain.repositories.LeaveBalanceRepository;
import com.atlashub.hr.leave.domain.valueobject.LeaveStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CancelLeaveHandler extends Command<CancelLeaveCommand, Void> {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    public CancelLeaveHandler(
            LeaveApplicationRepository leaveApplicationRepository,
            LeaveBalanceRepository leaveBalanceRepository
    ) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    @Override
    @Transactional
    public Void execute(CancelLeaveCommand command) {
        LeaveApplication application = leaveApplicationRepository.findById(command.leaveApplicationId())
                .orElseThrow(() -> new LeaveApplicationNotFoundException(command.leaveApplicationId()));

        if (application.getStatus() == LeaveStatus.APPROVED) {
            int year = application.getStartDate().getYear();
            leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYearWithLock(
                    application.getEmployeeId(),
                    application.getLeaveTypeId(),
                    year
            ).ifPresent(balance -> {
                balance.restore(application.getDaysRequested());
                leaveBalanceRepository.save(balance);
            });
        }

        application.cancel();
        leaveApplicationRepository.save(application);

        return null;
    }
}
