package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.exceptions.CreditLimitExceededException;
import com.atlashub.commerce.storefront.domain.exceptions.CustomerCreditBlockedException;
import com.atlashub.commerce.storefront.domain.exceptions.InvalidOrderStateException;
import com.atlashub.commerce.storefront.domain.valueobject.CreditStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.util.Objects;

@Getter
public class CustomerCredit extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long customerId;
    private Money creditLimit;
    private Money outstandingDebt;
    private CreditStatus status;

    public CustomerCredit(
            Long id,
            Long organizationId,
            Long customerId,
            Money creditLimit,
            Money outstandingDebt,
            CreditStatus status
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.customerId = customerId;
        this.creditLimit = creditLimit;
        this.outstandingDebt = outstandingDebt;
        this.status = status;
    }

    public static CustomerCredit create(
            Long id,
            Long organizationId,
            Long customerId,
            Money creditLimit
    ) {
        Objects.requireNonNull(id, "Credit ID must not be null");
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(customerId, "Customer ID must not be null");
        Objects.requireNonNull(creditLimit, "Credit limit must not be null");

        return new CustomerCredit(
                id,
                organizationId,
                customerId,
                creditLimit,
                Money.zero(creditLimit.currency()),
                CreditStatus.SETTLED
        );
    }

    public void extendCredit(Money amount) {
        Objects.requireNonNull(amount, "Amount must not be null");
        if (status == CreditStatus.BLOCKED) {
            throw new CustomerCreditBlockedException(customerId);
        }
        Money newDebt = outstandingDebt.add(amount);
        if (newDebt.isGreaterThan(creditLimit)) {
            throw new CreditLimitExceededException("Credit limit of " + creditLimit + " exceeded by requested extension");
        }
        this.outstandingDebt = newDebt;
        this.status = newDebt.compareTo(creditLimit) == 0 ? CreditStatus.OVER_LIMIT : CreditStatus.WITHIN_LIMIT;
    }

    public void settle(Money amount) {
        Objects.requireNonNull(amount, "Amount must not be null");
        if (amount.isGreaterThan(outstandingDebt)) {
            throw new InvalidOrderStateException("Settlement amount cannot exceed outstanding debt");
        }
        this.outstandingDebt = this.outstandingDebt.subtract(amount);
        if (this.outstandingDebt.isZero()) {
            this.status = CreditStatus.SETTLED;
        } else if (this.outstandingDebt.isLessThanOrEqual(creditLimit)) {
            this.status = CreditStatus.WITHIN_LIMIT;
        }
    }

    public void block() {
        this.status = CreditStatus.BLOCKED;
    }

    public void unblock() {
        if (this.status != CreditStatus.BLOCKED) {
            return;
        }
        if (this.outstandingDebt.isZero()) {
            this.status = CreditStatus.SETTLED;
        } else if (this.outstandingDebt.isGreaterThan(creditLimit)) {
            this.status = CreditStatus.OVER_LIMIT;
        } else {
            this.status = CreditStatus.WITHIN_LIMIT;
        }
    }

    public void updateCreditLimit(Money newLimit) {
        Objects.requireNonNull(newLimit, "New credit limit must not be null");
        this.creditLimit = newLimit;
        if (status != CreditStatus.BLOCKED) {
            if (outstandingDebt.isGreaterThan(newLimit)) {
                this.status = CreditStatus.OVER_LIMIT;
            } else if (outstandingDebt.isZero()) {
                this.status = CreditStatus.SETTLED;
            } else {
                this.status = CreditStatus.WITHIN_LIMIT;
            }
        }
    }

    @Override
    public Long getId() {
        return id;
    }
}
