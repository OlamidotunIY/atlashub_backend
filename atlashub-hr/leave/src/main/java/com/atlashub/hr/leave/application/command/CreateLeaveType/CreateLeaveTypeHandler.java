package com.atlashub.hr.leave.application.command.CreateLeaveType;

import com.atlashub.hr.leave.domain.entities.LeaveType;
import com.atlashub.hr.leave.domain.repositories.LeaveTypeRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class CreateLeaveTypeHandler extends Command<CreateLeaveTypeCommand, CreateLeaveTypeResponse> {

    private final LeaveTypeRepository leaveTypeRepository;

    public CreateLeaveTypeHandler(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @Override
    public CreateLeaveTypeResponse execute(CreateLeaveTypeCommand command) {
        LeaveType leaveType = LeaveType.create(
                null,
                command.organizationId(),
                command.name(),
                command.maxDaysPerYear(),
                command.isPaid(),
                command.requiresApproval(),
                command.carryOverAllowed(),
                command.maxCarryOverDays()
        );

        LeaveType saved = leaveTypeRepository.save(leaveType);
        return new CreateLeaveTypeResponse(saved.getId());
    }
}
