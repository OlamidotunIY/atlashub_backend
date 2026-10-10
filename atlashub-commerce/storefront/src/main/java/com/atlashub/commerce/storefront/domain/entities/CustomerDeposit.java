package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.exceptions.InvalidOrderStateException;
import com.atlashub.commerce.storefront.domain.valueobject.DepositStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.util.Objects;

@Getter
public class CustomerDeposit extends AggregateRoot<Long> {

    private final Long id;
    private final Long salesOrderId;
    private Money amountPaid;
    private Money balanceRemaining;
    private DepositStatus status;

    public CustomerDeposit(
            Long id,
            Long salesOrderId,
            Money amountPaid,
            Money balanceRemaining,
            DepositStatus status
    ) {
        this.id = id;
        this.salesOrderId = salesOrderId;
        this.amountPaid = amountPaid;
        this.balanceRemaining = balanceRemaining;
        this.status = status;
    }

    public static CustomerDeposit create(
            Long id,
            Long salesOrderId,
            Money initialDeposit,
            Money totalAmount
    ) {
        Objects.requireNonNull(id, "Deposit ID must not be null");
        Objects.requireNonNull(salesOrderId, "Sales order ID must not be null");
        Objects.requireNonNull(initialDeposit, "Initial deposit must not be null");
        Objects.requireNonNull(totalAmount, "Total amount must not be null");

        Money remaining = totalAmount.subtract(initialDeposit);
        DepositStatus initialStatus = remaining.isZero() || remaining.isNegative()
                ? DepositStatus.FULFILLED
                : DepositStatus.ACTIVE;

        return new CustomerDeposit(id, salesOrderId, initialDeposit, remaining, initialStatus);
    }

    public void addPayment(Money amount) {
        if (status != DepositStatus.ACTIVE) {
            throw new InvalidOrderStateException("Cannot add payment to deposit in status: " + status);
        }
        Objects.requireNonNull(amount, "Payment amount must not be null");
        this.amountPaid = this.amountPaid.add(amount);
        this.balanceRemaining = this.balanceRemaining.subtract(amount);
        if (this.balanceRemaining.isZero() || this.balanceRemaining.isNegative()) {
            this.status = DepositStatus.FULFILLED;
        }
    }

    public void recall() {
        if (status != DepositStatus.ACTIVE) {
            throw new InvalidOrderStateException("Cannot recall deposit in status: " + status);
        }
        this.status = DepositStatus.RECALLED;
    }

    @Override
    public Long getId() {
        return id;
    }
}
