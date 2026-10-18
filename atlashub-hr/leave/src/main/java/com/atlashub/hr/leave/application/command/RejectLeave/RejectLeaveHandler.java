package com.atlashub.hr.leave.application.command.RejectLeave;

import com.atlashub.hr.leave.domain.entities.LeaveApplication;
import com.atlashub.hr.leave.domain.exceptions.LeaveApplicationNotFoundException;
import com.atlashub.hr.leave.domain.repositories.LeaveApplicationRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RejectLeaveHandler extends Command<RejectLeaveCommand, Void> {

    private final LeaveApplicationRepository leaveApplicationRepository;

    public RejectLeaveHandler(LeaveApplicationRepository leaveApplicationRepository) {
        this.leaveApplicationRepository = leaveApplicationRepository;
    }

    @Override
    @Transactional
    public Void execute(RejectLeaveCommand command) {
        LeaveApplication application = leaveApplicationRepository.findById(command.leaveApplicationId())
                .orElseThrow(() -> new LeaveApplicationNotFoundException(command.leaveApplicationId()));

        application.reject(command.reason());
        leaveApplicationRepository.save(application);

        return null;
    }
}
