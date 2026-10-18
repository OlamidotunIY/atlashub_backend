package com.atlashub.hr.leave.application.query.GetLeaveBalance;

import com.atlashub.hr.leave.domain.entities.LeaveBalance;
import com.atlashub.hr.leave.domain.entities.LeaveType;
import com.atlashub.hr.leave.domain.repositories.LeaveBalanceRepository;
import com.atlashub.hr.leave.domain.repositories.LeaveTypeRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GetLeaveBalanceHandler extends Query<GetLeaveBalanceQuery, List<LeaveBalanceResult>> {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public GetLeaveBalanceHandler(
            LeaveBalanceRepository leaveBalanceRepository,
            LeaveTypeRepository leaveTypeRepository
    ) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    @Override
    public List<LeaveBalanceResult> execute(GetLeaveBalanceQuery query) {
        int year = query.year() != null ? query.year() : LocalDate.now().getYear();
        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeIdAndYear(query.employeeId(), year);

        return balances.stream()
                .map(this::toResult)
                .toList();
    }

    private LeaveBalanceResult toResult(LeaveBalance balance) {
        String typeName = leaveTypeRepository.findById(balance.getLeaveTypeId())
                .map(LeaveType::getName)
                .orElse("Unknown");

        return new LeaveBalanceResult(
                balance.getLeaveTypeId(),
                typeName,
                balance.getYear(),
                balance.getEntitlement(),
                balance.getUsed(),
                balance.getRemaining()
        );
    }
}
