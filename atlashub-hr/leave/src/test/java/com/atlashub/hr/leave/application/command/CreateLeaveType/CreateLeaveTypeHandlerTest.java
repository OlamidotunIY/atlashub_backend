package com.atlashub.hr.leave.application.command.CreateLeaveType;

import com.atlashub.hr.leave.domain.entities.LeaveType;
import com.atlashub.hr.leave.domain.repositories.LeaveTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateLeaveTypeHandlerTest {

    private LeaveTypeRepository leaveTypeRepository;
    private CreateLeaveTypeHandler handler;

    @BeforeEach
    void setUp() {
        leaveTypeRepository = Mockito.mock(LeaveTypeRepository.class);
        handler = new CreateLeaveTypeHandler(leaveTypeRepository);
    }

    @Test
    @DisplayName("Should create and save leave type successfully")
    void shouldCreateLeaveType() {
        CreateLeaveTypeCommand command = new CreateLeaveTypeCommand(
                10L,
                "Maternity Leave",
                90,
                true,
                true,
                false,
                0
        );

        when(leaveTypeRepository.save(any(LeaveType.class))).thenAnswer(invocation -> {
            LeaveType lt = invocation.getArgument(0);
            return new LeaveType(50L, lt.getOrganizationId(), lt.getName(), lt.getMaxDaysPerYear(),
                    lt.getIsPaid(), lt.getRequiresApproval(), lt.getCarryOverAllowed(), lt.getMaxCarryOverDays());
        });

        CreateLeaveTypeResponse response = handler.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.leaveTypeId()).isEqualTo(50L);
        verify(leaveTypeRepository).save(any(LeaveType.class));
    }
}
