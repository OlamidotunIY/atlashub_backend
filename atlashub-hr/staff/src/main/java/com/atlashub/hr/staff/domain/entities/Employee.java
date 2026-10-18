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
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
public class Employee extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long userId;
    private final String employeeNumber;
    private String firstName;
    private String lastName;
    private final EmailAddress email;
    private PhoneNumber phone;
    private final LocalDate dateOfBirth;
    private String address;
    private String nationality;
    private final LocalDate hireDate;
    private LocalDate terminationDate;
    private String designation;
    private String department;
    private Long reportingManagerId;
    private Long salaryGradeId;
    private Money baseSalary;
    private EmployeeStatus status;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Employee(
            Long id,
            Long organizationId,
            Long userId,
            String employeeNumber,
            String firstName,
            String lastName,
            EmailAddress email,
            PhoneNumber phone,
            LocalDate dateOfBirth,
            String address,
            String nationality,
            LocalDate hireDate,
            LocalDate terminationDate,
            String designation,
            String department,
            Long reportingManagerId,
            Long salaryGradeId,
            Money baseSalary,
            EmployeeStatus status,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.userId = userId;
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.nationality = nationality;
        this.hireDate = hireDate;
        this.terminationDate = terminationDate;
        this.designation = designation;
        this.department = department;
        this.reportingManagerId = reportingManagerId;
        this.salaryGradeId = salaryGradeId;
        this.baseSalary = baseSalary;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Employee create(
            Long id,
            Long organizationId,
            Long userId,
            String employeeNumber,
            String firstName,
            String lastName,
            EmailAddress email,
            PhoneNumber phone,
            LocalDate dateOfBirth,
            String address,
            String nationality,
            LocalDate hireDate,
            String designation,
            String department,
            Long reportingManagerId,
            Long salaryGradeId,
            Money baseSalary
    ) {
        if (id == null) {
            throw new InvalidStaffOperationException("Employee ID cannot be null");
        }
        if (organizationId == null) {
            throw new InvalidStaffOperationException("Organization ID cannot be null");
        }
        if (userId == null) {
            throw new InvalidStaffOperationException("User ID cannot be null");
        }
        if (employeeNumber == null || employeeNumber.isBlank()) {
            throw new InvalidStaffOperationException("Employee number cannot be null or blank");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new InvalidStaffOperationException("First name cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new InvalidStaffOperationException("Last name cannot be null or blank");
        }
        if (email == null) {
            throw new InvalidStaffOperationException("Email address cannot be null");
        }
        if (phone == null) {
            throw new InvalidStaffOperationException("Phone number cannot be null");
        }
        if (dateOfBirth == null) {
            throw new InvalidStaffOperationException("Date of birth cannot be null");
        }
        if (hireDate == null) {
            throw new InvalidStaffOperationException("Hire date cannot be null");
        }
        if (hireDate.isAfter(LocalDate.now())) {
            throw new InvalidStaffOperationException("Hire date cannot be in the future");
        }
        if (designation == null || designation.isBlank()) {
            throw new InvalidStaffOperationException("Designation cannot be null or blank");
        }
        if (department == null || department.isBlank()) {
            throw new InvalidStaffOperationException("Department cannot be null or blank");
        }
        if (salaryGradeId == null) {
            throw new InvalidStaffOperationException("Salary grade ID cannot be null");
        }
        if (baseSalary == null || baseSalary.amount() == null || baseSalary.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidStaffOperationException("Base salary must be greater than zero");
        }

        ZonedDateTime now = ZonedDateTime.now();
        Employee employee = new Employee(
                id,
                organizationId,
                userId,
                employeeNumber,
                firstName,
                lastName,
                email,
                phone,
                dateOfBirth,
                address,
                nationality,
                hireDate,
                null,
                designation,
                department,
                reportingManagerId,
                salaryGradeId,
                baseSalary,
                EmployeeStatus.PROBATION,
                now,
                now
        );

        employee.registerEvent(EmployeeOnboardedEvent.of(
                id,
                organizationId,
                userId,
                employeeNumber,
                firstName,
                lastName,
                email.value(),
                designation,
                department,
                hireDate,
                baseSalary.amount(),
                baseSalary.currency().name()
        ));

        return employee;
    }

    public void updateBioData(String firstName, String lastName, PhoneNumber phone, String address, String nationality) {
        if (this.status == EmployeeStatus.TERMINATED) {
            throw new InvalidEmployeeStateException("Cannot update bio-data for terminated employee");
        }
        if (firstName != null && !firstName.isBlank()) {
            this.firstName = firstName;
        }
        if (lastName != null && !lastName.isBlank()) {
            this.lastName = lastName;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (address != null) {
            this.address = address;
        }
        if (nationality != null) {
            this.nationality = nationality;
        }
        this.updatedAt = ZonedDateTime.now();

        registerEvent(EmployeeProfileUpdatedEvent.of(
                this.id,
                this.organizationId,
                this.firstName,
                this.lastName,
                this.phone != null ? this.phone.value() : null,
                this.address,
                this.nationality
        ));
    }

    public void activate() {
        if (this.status != EmployeeStatus.PROBATION) {
            throw new InvalidEmployeeStateException("Only employees on PROBATION can be activated. Current status: " + this.status);
        }
        this.status = EmployeeStatus.ACTIVE;
        this.updatedAt = ZonedDateTime.now();
    }

    public void promote(String newDesignation, Long newGradeId, Money newBaseSalary) {
        if (this.status == EmployeeStatus.TERMINATED) {
            throw new InvalidEmployeeStateException("Cannot promote a terminated employee");
        }
        if (newDesignation == null || newDesignation.isBlank()) {
            throw new InvalidStaffOperationException("New designation cannot be null or blank");
        }
        if (newGradeId == null) {
            throw new InvalidStaffOperationException("New salary grade ID cannot be null");
        }
        if (newBaseSalary == null || newBaseSalary.amount() == null || newBaseSalary.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidStaffOperationException("New base salary must be greater than zero");
        }

        this.designation = newDesignation;
        this.salaryGradeId = newGradeId;
        this.baseSalary = newBaseSalary;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(EmployeePromotedEvent.of(
                this.id,
                this.organizationId,
                this.designation,
                this.salaryGradeId,
                this.baseSalary.amount(),
                this.baseSalary.currency().name()
        ));
    }

    public void suspend(String reason) {
        if (this.status != EmployeeStatus.ACTIVE) {
            throw new InvalidEmployeeStateException("Only ACTIVE employees can be suspended. Current status: " + this.status);
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidStaffOperationException("Suspension reason cannot be null or blank");
        }

        this.status = EmployeeStatus.SUSPENDED;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(EmployeeSuspendedEvent.of(
                this.id,
                this.organizationId,
                this.userId,
                reason
        ));
    }

    public void reinstate() {
        if (this.status != EmployeeStatus.SUSPENDED) {
            throw new InvalidEmployeeStateException("Only SUSPENDED employees can be reinstated. Current status: " + this.status);
        }
        this.status = EmployeeStatus.ACTIVE;
        this.updatedAt = ZonedDateTime.now();
    }

    public void terminate(LocalDate terminationDate, boolean hasActiveLoan) {
        if (this.status == EmployeeStatus.TERMINATED) {
            throw new InvalidEmployeeStateException("Employee is already terminated");
        }
        if (hasActiveLoan) {
            throw new EmployeeHasOutstandingLoanException();
        }
        if (terminationDate == null) {
            throw new InvalidStaffOperationException("Termination date cannot be null");
        }

        this.status = EmployeeStatus.TERMINATED;
        this.terminationDate = terminationDate;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(EmployeeTerminatedEvent.of(
                this.id,
                this.organizationId,
                this.userId,
                this.terminationDate
        ));
    }

    public void placeOnLeave() {
        if (this.status != EmployeeStatus.ACTIVE) {
            throw new InvalidEmployeeStateException("Only ACTIVE employees can be placed on leave. Current status: " + this.status);
        }
        this.status = EmployeeStatus.ON_LEAVE;
        this.updatedAt = ZonedDateTime.now();
    }

    public void returnFromLeave() {
        if (this.status != EmployeeStatus.ON_LEAVE) {
            throw new InvalidEmployeeStateException("Only employees ON_LEAVE can return to active status. Current status: " + this.status);
        }
        this.status = EmployeeStatus.ACTIVE;
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
