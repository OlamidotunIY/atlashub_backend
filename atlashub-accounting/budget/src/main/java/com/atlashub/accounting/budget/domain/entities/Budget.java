package com.atlashub.accounting.budget.domain.entities;

import com.atlashub.accounting.budget.domain.events.BudgetApprovedEvent;
import com.atlashub.accounting.budget.domain.exceptions.BudgetAlreadyApprovedException;
import com.atlashub.accounting.budget.domain.exceptions.InvalidBudgetOperationException;
import com.atlashub.accounting.budget.domain.exceptions.InvalidBudgetStateException;
import com.atlashub.accounting.budget.domain.exceptions.SelfApprovalNotAllowedException;
import com.atlashub.accounting.budget.domain.valueobject.BudgetStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
public class Budget extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long accountId;
    private final String name;
    private final String period;
    private final Money budgetedAmount;
    private BudgetStatus status;
    private final Long initiatedBy;
    private Long approvedBy;
    private ZonedDateTime approvedAt;

    public Budget(
            Long id,
            Long organizationId,
            Long accountId,
            String name,
            String period,
            Money budgetedAmount,
            BudgetStatus status,
            Long initiatedBy,
            Long approvedBy,
            ZonedDateTime approvedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.accountId = accountId;
        this.name = name;
        this.period = period;
        this.budgetedAmount = budgetedAmount;
        this.status = status;
        this.initiatedBy = initiatedBy;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
    }

    public static Budget create(
            Long id,
            Long organizationId,
            Long accountId,
            String name,
            String period,
            Money budgetedAmount,
            Long initiatedBy
    ) {
        if (id == null) {
            throw new InvalidBudgetOperationException("Budget ID cannot be null");
        }
        if (organizationId == null) {
            throw new InvalidBudgetOperationException("Organization ID cannot be null");
        }
        if (accountId == null) {
            throw new InvalidBudgetOperationException("Account ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidBudgetOperationException("Budget name cannot be null or blank");
        }
        if (period == null || period.isBlank()) {
            throw new InvalidBudgetOperationException("Budget period cannot be null or blank");
        }
        if (budgetedAmount == null || budgetedAmount.amount() == null || budgetedAmount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBudgetOperationException("Budgeted amount must be greater than zero");
        }
        if (initiatedBy == null) {
            throw new InvalidBudgetOperationException("Initiated by cannot be null");
        }

        return new Budget(
                id,
                organizationId,
                accountId,
                name,
                period,
                budgetedAmount,
                BudgetStatus.DRAFT,
                initiatedBy,
                null,
                null
        );
    }

    public void approve(Long approverId) {
        if (this.status == BudgetStatus.APPROVED) {
            throw new BudgetAlreadyApprovedException();
        }
        if (this.status != BudgetStatus.DRAFT) {
            throw new InvalidBudgetStateException("Cannot approve a budget in status: " + this.status);
        }
        if (approverId == null) {
            throw new InvalidBudgetOperationException("Approver ID cannot be null");
        }
        if (approverId.equals(this.initiatedBy)) {
            throw new SelfApprovalNotAllowedException();
        }

        this.status = BudgetStatus.APPROVED;
        this.approvedBy = approverId;
        this.approvedAt = ZonedDateTime.now();

        registerEvent(BudgetApprovedEvent.of(
                this.id,
                this.organizationId,
                this.accountId,
                this.name,
                this.period,
                this.budgetedAmount.amount(),
                this.budgetedAmount.currency().name(),
                this.approvedBy,
                this.approvedAt
        ));
    }

    public void close() {
        if (this.status != BudgetStatus.APPROVED) {
            throw new InvalidBudgetStateException("Cannot close a budget that is not in APPROVED status. Current status: " + this.status);
        }
        this.status = BudgetStatus.CLOSED;
    }

    @Override
    public Long getId() {
        return id;
    }
}
