package com.atlashub.hr.staff.domain.entities;

import com.atlashub.hr.staff.domain.events.EmployeeOnboardedEvent;
import com.atlashub.hr.staff.domain.events.EmployeeProfileUpdatedEvent;
import com.atlashub.hr.staff.domain.events.EmployeePromotedEvent;
import com.atlashub.hr.staff.domain.events.EmployeeSuspendedEvent;
import com.atlashub.hr.staff.domain.events.EmployeeTerminatedEvent;
import com.atlashub.hr.staff.domain.exceptions.EmployeeHasOutstandingLoanException;
import com.atlashub.hr.staff.domain.exceptions.InvalidEmployeeStateException;
import com.atlashub.hr.staff.domain.exceptions.InvalidStaffOperationException;
import com.atlashub.hr.staff.domain.valueobject.EmployeeStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmployeeTest {

    private Employee createSampleEmployee() {
        return Employee.create(
                1L,
                10L,
                100L,
                "EMP-001",
                "John",
                "Doe",
                new EmailAddress("john.doe@atlashub.com"),
                new PhoneNumber("+2348012345678"),
                LocalDate.of(1990, 1, 1),
                "123 Tech Lane, Lagos",
                "Nigerian",
                LocalDate.of(2023, 1, 15),
                "Senior Engineer",
                "Engineering",
                5L,
                2L,
                Money.of(new BigDecimal("750000.00"), CurrencyCode.NGN)
        );
    }

    @Test
    @DisplayName("Should create Employee on probation and register EmployeeOnboardedEvent")
    void shouldCreateEmployeeSuccessfully() {
        Employee employee = createSampleEmployee();

        assertNotNull(employee);
        assertEquals(1L, employee.getId());
        assertEquals(10L, employee.getOrganizationId());
        assertEquals(100L, employee.getUserId());
        assertEquals("EMP-001", employee.getEmployeeNumber());
        assertEquals("John", employee.getFirstName());
        assertEquals("Doe", employee.getLastName());
        assertEquals("john.doe@atlashub.com", employee.getEmail().value());
        assertEquals("+2348012345678", employee.getPhone().value());
        assertEquals(EmployeeStatus.PROBATION, employee.getStatus());
        assertThat(employee.getBaseSalary().amount()).isEqualByComparingTo(new BigDecimal("750000.00"));

        assertEquals(1, employee.peekDomainEvents().size());
        assertThat(employee.peekDomainEvents().getFirst()).isInstanceOf(EmployeeOnboardedEvent.class);
    }

    @Test
    @DisplayName("Should fail employee creation when required fields are missing or invalid")
    void shouldFailCreationOnInvalidInvariants() {
        assertThrows(InvalidStaffOperationException.class, () -> Employee.create(
                null,
                10L,
                100L,
                "EMP-001",
                "John",
                "Doe",
                new EmailAddress("john.doe@atlashub.com"),
                new PhoneNumber("+2348012345678"),
                LocalDate.of(1990, 1, 1),
                "Lagos",
                "Nigerian",
                LocalDate.of(2023, 1, 15),
                "Dev",
                "Engineering",
                null,
                2L,
                Money.of(new BigDecimal("500000.00"), CurrencyCode.NGN)
        ));

        assertThrows(InvalidStaffOperationException.class, () -> Employee.create(
                1L,
                10L,
                100L,
                "",
                "John",
                "Doe",
                new EmailAddress("john.doe@atlashub.com"),
                new PhoneNumber("+2348012345678"),
                LocalDate.of(1990, 1, 1),
                "Lagos",
                "Nigerian",
                LocalDate.of(2023, 1, 15),
                "Dev",
                "Engineering",
                null,
                2L,
                Money.of(new BigDecimal("500000.00"), CurrencyCode.NGN)
        ));

        assertThrows(InvalidStaffOperationException.class, () -> Employee.create(
                1L,
                10L,
                100L,
                "EMP-001",
                "John",
                "Doe",
                new EmailAddress("john.doe@atlashub.com"),
                new PhoneNumber("+2348012345678"),
                LocalDate.of(1990, 1, 1),
                "Lagos",
                "Nigerian",
                LocalDate.now().plusDays(10), // Future hire date
                "Dev",
                "Engineering",
                null,
                2L,
                Money.of(new BigDecimal("500000.00"), CurrencyCode.NGN)
        ));
    }

    @Test
    @DisplayName("Should activate employee from PROBATION to ACTIVE")
    void shouldActivateEmployee() {
        Employee employee = createSampleEmployee();
        assertEquals(EmployeeStatus.PROBATION, employee.getStatus());

        employee.activate();
        assertEquals(EmployeeStatus.ACTIVE, employee.getStatus());

        assertThrows(InvalidEmployeeStateException.class, employee::activate);
    }

    @Test
    @DisplayName("Should update bio data and register EmployeeProfileUpdatedEvent")
    void shouldUpdateBioData() {
        Employee employee = createSampleEmployee();
        employee.pullDomainEvents();

        employee.updateBioData("Johnny", "D.", new PhoneNumber("+2348099999999"), "New Address", "Ghanaian");

        assertEquals("Johnny", employee.getFirstName());
        assertEquals("D.", employee.getLastName());
        assertEquals("+2348099999999", employee.getPhone().value());
        assertEquals("New Address", employee.getAddress());
        assertEquals("Ghanaian", employee.getNationality());
        assertEquals(1, employee.peekDomainEvents().size());
        assertThat(employee.peekDomainEvents().getFirst()).isInstanceOf(EmployeeProfileUpdatedEvent.class);
    }

    @Test
    @DisplayName("Should promote employee and register EmployeePromotedEvent")
    void shouldPromoteEmployee() {
        Employee employee = createSampleEmployee();
        employee.pullDomainEvents();

        Money newSalary = Money.of(new BigDecimal("950000.00"), CurrencyCode.NGN);
        employee.promote("Principal Engineer", 3L, newSalary);

        assertEquals("Principal Engineer", employee.getDesignation());
        assertEquals(3L, employee.getSalaryGradeId());
        assertThat(employee.getBaseSalary().amount()).isEqualByComparingTo(new BigDecimal("950000.00"));
        assertEquals(1, employee.peekDomainEvents().size());
        assertThat(employee.peekDomainEvents().getFirst()).isInstanceOf(EmployeePromotedEvent.class);
    }

    @Test
    @DisplayName("Should suspend and reinstate employee")
    void shouldSuspendAndReinstateEmployee() {
        Employee employee = createSampleEmployee();
        employee.activate();
        employee.pullDomainEvents();

        employee.suspend("Gross misconduct under investigation");
        assertEquals(EmployeeStatus.SUSPENDED, employee.getStatus());
        assertEquals(1, employee.peekDomainEvents().size());
        assertThat(employee.peekDomainEvents().getFirst()).isInstanceOf(EmployeeSuspendedEvent.class);

        employee.reinstate();
        assertEquals(EmployeeStatus.ACTIVE, employee.getStatus());
    }

    @Test
    @DisplayName("Should terminate employee if no active loan, otherwise throw exception")
    void shouldTerminateEmployeeOrThrowOnActiveLoan() {
        Employee employee = createSampleEmployee();
        employee.activate();
        employee.pullDomainEvents();

        assertThrows(EmployeeHasOutstandingLoanException.class, () ->
                employee.terminate(LocalDate.now(), true));

        LocalDate termDate = LocalDate.now();
        employee.terminate(termDate, false);
        assertEquals(EmployeeStatus.TERMINATED, employee.getStatus());
        assertEquals(termDate, employee.getTerminationDate());
        assertEquals(1, employee.peekDomainEvents().size());
        assertThat(employee.peekDomainEvents().getFirst()).isInstanceOf(EmployeeTerminatedEvent.class);

        assertThrows(InvalidEmployeeStateException.class, () ->
                employee.terminate(termDate, false));
    }

    @Test
    @DisplayName("Should place on leave and return from leave")
    void shouldPlaceOnLeaveAndReturn() {
        Employee employee = createSampleEmployee();
        employee.activate();

        employee.placeOnLeave();
        assertEquals(EmployeeStatus.ON_LEAVE, employee.getStatus());

        employee.returnFromLeave();
        assertEquals(EmployeeStatus.ACTIVE, employee.getStatus());
    }
}
