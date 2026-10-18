package com.atlashub.hr.staff.domain.entities;

import com.atlashub.hr.staff.domain.exceptions.InvalidStaffOperationException;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalaryGradeTest {

    @Test
    @DisplayName("Should create SalaryGrade and update base salary")
    void shouldCreateAndManageSalaryGrade() {
        SalaryGrade grade = SalaryGrade.create(
                1L,
                10L,
                "Grade L5 - Senior",
                Money.of(new BigDecimal("800000.00"), CurrencyCode.NGN)
        );

        assertNotNull(grade);
        assertEquals(1L, grade.getId());
        assertEquals(10L, grade.getOrganizationId());
        assertEquals("Grade L5 - Senior", grade.getName());
        assertThat(grade.getBaseSalary().amount()).isEqualByComparingTo(new BigDecimal("800000.00"));

        grade.updateBaseSalary(Money.of(new BigDecimal("850000.00"), CurrencyCode.NGN));
        assertThat(grade.getBaseSalary().amount()).isEqualByComparingTo(new BigDecimal("850000.00"));
    }

    @Test
    @DisplayName("Should fail SalaryGrade creation on invalid inputs")
    void shouldFailOnInvalidInputs() {
        assertThrows(InvalidStaffOperationException.class, () -> SalaryGrade.create(
                null,
                10L,
                "Grade",
                Money.of(new BigDecimal("100.00"), CurrencyCode.NGN)
        ));

        assertThrows(InvalidStaffOperationException.class, () -> SalaryGrade.create(
                1L,
                10L,
                "",
                Money.of(new BigDecimal("100.00"), CurrencyCode.NGN)
        ));

        assertThrows(InvalidStaffOperationException.class, () -> SalaryGrade.create(
                1L,
                10L,
                "Grade",
                Money.of(BigDecimal.ZERO, CurrencyCode.NGN)
        ));
    }
}
