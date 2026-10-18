package com.atlashub.hr.leave.domain.entities;

import com.atlashub.hr.leave.domain.exceptions.InsufficientLeaveBalanceException;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LeaveBalanceTest {

    @Test
    @DisplayName("Should create LeaveBalance successfully")
    void shouldCreateLeaveBalance() {
        LeaveBalance balance = LeaveBalance.create(1L, 100L, 5L, 2026, 20);

        assertThat(balance.getId()).isEqualTo(1L);
        assertThat(balance.getEmployeeId()).isEqualTo(100L);
        assertThat(balance.getLeaveTypeId()).isEqualTo(5L);
        assertThat(balance.getYear()).isEqualTo(2026);
        assertThat(balance.getEntitlement()).isEqualTo(20);
        assertThat(balance.getUsed()).isZero();
        assertThat(balance.getRemaining()).isEqualTo(20);
    }

    @Test
    @DisplayName("Should deduct days from remaining and add to used")
    void shouldDeductDaysSuccessfully() {
        LeaveBalance balance = LeaveBalance.create(1L, 100L, 5L, 2026, 20);

        balance.deduct(5);

        assertThat(balance.getUsed()).isEqualTo(5);
        assertThat(balance.getRemaining()).isEqualTo(15);
    }

    @Test
    @DisplayName("Should throw InsufficientLeaveBalanceException when deducting more than remaining")
    void shouldThrowWhenDeductingMoreThanRemaining() {
        LeaveBalance balance = LeaveBalance.create(1L, 100L, 5L, 2026, 5);

        assertThrows(InsufficientLeaveBalanceException.class, () -> balance.deduct(6));
    }

    @Test
    @DisplayName("Should restore deducted days")
    void shouldRestoreDaysSuccessfully() {
        LeaveBalance balance = LeaveBalance.create(1L, 100L, 5L, 2026, 20);
        balance.deduct(5);

        balance.restore(3);

        assertThat(balance.getUsed()).isEqualTo(2);
        assertThat(balance.getRemaining()).isEqualTo(18);
    }

    @Test
    @DisplayName("Should reject negative or null parameters during creation")
    void shouldRejectInvalidParameters() {
        assertThrows(BusinessRuleException.class, () ->
                LeaveBalance.create(1L, null, 5L, 2026, 20)
        );
        assertThrows(BusinessRuleException.class, () ->
                LeaveBalance.create(1L, 100L, null, 2026, 20)
        );
        assertThrows(BusinessRuleException.class, () ->
                LeaveBalance.create(1L, 100L, 5L, 0, 20)
        );
        assertThrows(BusinessRuleException.class, () ->
                LeaveBalance.create(1L, 100L, 5L, 2026, -1)
        );
    }
}
