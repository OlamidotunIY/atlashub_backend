package com.atlashub.hr.loan.domain.entities;

import com.atlashub.hr.loan.domain.events.LoanApprovedEvent;
import com.atlashub.hr.loan.domain.exceptions.InvalidLoanStateException;
import com.atlashub.hr.loan.domain.valueobject.LoanStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
public class EmployeeLoan extends AggregateRoot<Long> {

    private final Long id;
    private final Long employeeId;
    private final Long organizationId;
    private final Money principalAmount;
    private Money outstandingBalance;
    private final Money monthlyDeduction;
    private ZonedDateTime disbursedAt;
    private LoanStatus status;
    private Long approvedBy;
    private LocalDate startDate;

    public EmployeeLoan(
            Long id,
            Long employeeId,
            Long organizationId,
            Money principalAmount,
            Money outstandingBalance,
            Money monthlyDeduction,
            ZonedDateTime disbursedAt,
            LoanStatus status,
            Long approvedBy,
            LocalDate startDate
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.organizationId = organizationId;
        this.principalAmount = principalAmount;
        this.outstandingBalance = outstandingBalance;
        this.monthlyDeduction = monthlyDeduction;
        this.disbursedAt = disbursedAt;
        this.status = status;
        this.approvedBy = approvedBy;
        this.startDate = startDate;
    }

    public static EmployeeLoan create(
            Long id,
            Long employeeId,
            Long organizationId,
            Money principalAmount,
            Money monthlyDeduction
    ) {
        if (employeeId == null) {
            throw new InvalidLoanStateException("Employee ID must not be null");
        }
        if (organizationId == null) {
            throw new InvalidLoanStateException("Organization ID must not be null");
        }
        if (principalAmount == null || !principalAmount.isPositive()) {
            throw new InvalidLoanStateException("Principal amount must be positive");
        }
        if (monthlyDeduction == null || !monthlyDeduction.isPositive()) {
            throw new InvalidLoanStateException("Monthly deduction must be positive");
        }
        if (!principalAmount.currency().equals(monthlyDeduction.currency())) {
            throw new InvalidLoanStateException("Currency mismatch between principal and deduction");
        }

        return new EmployeeLoan(
                id,
                employeeId,
                organizationId,
                principalAmount,
                principalAmount,
                monthlyDeduction,
                null,
                LoanStatus.PENDING_APPROVAL,
                null,
                null
        );
    }

    public void approve(Long approverId, LocalDate startDate) {
        if (this.status != LoanStatus.PENDING_APPROVAL) {
            throw new InvalidLoanStateException("Only pending loans can be approved");
        }
        if (approverId == null) {
            throw new InvalidLoanStateException("Approver ID must not be null");
        }
        if (startDate == null) {
            throw new InvalidLoanStateException("Start date must not be null");
        }

        this.status = LoanStatus.APPROVED;
        this.approvedBy = approverId;
        this.startDate = startDate;

        registerEvent(LoanApprovedEvent.of(
                id,
                employeeId,
                organizationId,
                principalAmount.amount(),
                principalAmount.currency().name(),
                monthlyDeduction.amount(),
                startDate,
                approverId
        ));
    }

    public void disburse() {
        if (this.status != LoanStatus.APPROVED) {
            throw new InvalidLoanStateException("Only approved loans can be disbursed");
        }
        this.status = LoanStatus.ACTIVE;
        this.disbursedAt = ZonedDateTime.now();
    }

    public void recordRepayment(Money amount) {
        if (this.status != LoanStatus.ACTIVE && this.status != LoanStatus.TERMINATION_PENDING) {
            throw new InvalidLoanStateException("Cannot record repayment for loan in state " + status);
        }
        if (amount == null || !amount.isPositive()) {
            throw new InvalidLoanStateException("Repayment amount must be positive");
        }

        this.outstandingBalance = this.outstandingBalance.subtract(amount);
        if (this.outstandingBalance.amount().compareTo(BigDecimal.ZERO) <= 0) {
            this.outstandingBalance = Money.of(BigDecimal.ZERO, this.principalAmount.currency());
            this.status = LoanStatus.FULLY_REPAID;
        }
    }

    public void cancel() {
        if (this.status != LoanStatus.PENDING_APPROVAL) {
            throw new InvalidLoanStateException("Only pending loans can be cancelled");
        }
        this.status = LoanStatus.CANCELLED;
    }

    public void markTerminationPending() {
        if (this.status == LoanStatus.ACTIVE) {
            this.status = LoanStatus.TERMINATION_PENDING;
        }
    }

    @Override
    public Long getId() {
        return id;
    }
}
