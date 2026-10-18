package com.atlashub.hr.leave.application.query.GetLeaveBalance;

import com.atlashub.hr.leave.domain.entities.LeaveBalance;
import com.atlashub.hr.leave.domain.entities.LeaveType;
import com.atlashub.hr.leave.domain.repositories.LeaveBalanceRepository;
import com.atlashub.hr.leave.domain.repositories.LeaveTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GetLeaveBalanceHandlerTest {

    private LeaveBalanceRepository leaveBalanceRepository;
    private LeaveTypeRepository leaveTypeRepository;
    private GetLeaveBalanceHandler handler;

    @BeforeEach
    void setUp() {
        leaveBalanceRepository = Mockito.mock(LeaveBalanceRepository.class);
        leaveTypeRepository = Mockito.mock(LeaveTypeRepository.class);
        handler = new GetLeaveBalanceHandler(leaveBalanceRepository, leaveTypeRepository);
    }

    @Test
    @DisplayName("Should return leave balances with resolved leave type names")
    void shouldReturnLeaveBalances() {
        LeaveBalance balance = new LeaveBalance(1L, 100L, 5L, 2026, 20, 5, 15);
        LeaveType type = LeaveType.create(5L, 10L, "Annual Leave", 20, true, true, false, 0);

        when(leaveBalanceRepository.findByEmployeeIdAndYear(100L, 2026)).thenReturn(List.of(balance));
        when(leaveTypeRepository.findById(5L)).thenReturn(Optional.of(type));

        List<LeaveBalanceResult> results = handler.execute(new GetLeaveBalanceQuery(100L, 2026));

        assertThat(results).hasSize(1);
        LeaveBalanceResult result = results.get(0);
        assertThat(result.leaveTypeId()).isEqualTo(5L);
        assertThat(result.leaveTypeName()).isEqualTo("Annual Leave");
        assertThat(result.entitlement()).isEqualTo(20);
        assertThat(result.used()).isEqualTo(5);
        assertThat(result.remaining()).isEqualTo(15);
    }
}
