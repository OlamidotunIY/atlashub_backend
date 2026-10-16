package com.atlashub.hr.leave.domain.entities;

import com.atlashub.shared.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LeaveTypeTest {

    @Test
    @DisplayName("Should create LeaveType with valid invariants")
    void shouldCreateLeaveTypeSuccessfully() {
        LeaveType type = LeaveType.create(
                1L,
                10L,
                "Annual Leave",
                20,
                true,
                true,
                true,
                5
        );

        assertThat(type.getId()).isEqualTo(1L);
        assertThat(type.getOrganizationId()).isEqualTo(10L);
        assertThat(type.getName()).isEqualTo("Annual Leave");
        assertThat(type.getMaxDaysPerYear()).isEqualTo(20);
        assertThat(type.getIsPaid()).isTrue();
        assertThat(type.getRequiresApproval()).isTrue();
        assertThat(type.getCarryOverAllowed()).isTrue();
        assertThat(type.getMaxCarryOverDays()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should reject LeaveType with null organizationId")
    void shouldRejectNullOrganizationId() {
        assertThrows(BusinessRuleException.class, () ->
                LeaveType.create(1L, null, "Sick Leave", 10, true, false, false, 0)
        );
    }

    @Test
    @DisplayName("Should reject LeaveType with blank name")
    void shouldRejectBlankName() {
        assertThrows(BusinessRuleException.class, () ->
                LeaveType.create(1L, 10L, "   ", 10, true, false, false, 0)
        );
    }

    @Test
    @DisplayName("Should reject LeaveType with non-positive maxDaysPerYear")
    void shouldRejectNonPositiveMaxDays() {
        assertThrows(BusinessRuleException.class, () ->
                LeaveType.create(1L, 10L, "Sick Leave", 0, true, false, false, 0)
        );
    }
}
