package com.atlashub.accounting.ar.domain.entities;

import com.atlashub.accounting.ar.domain.events.CustomerReceivableCreatedEvent;
import com.atlashub.accounting.ar.domain.events.ReceivableCollectedEvent;
import com.atlashub.accounting.ar.domain.events.ReceivableWrittenOffEvent;
import com.atlashub.accounting.ar.domain.exceptions.InvalidReceivableOperationException;
import com.atlashub.accounting.ar.domain.exceptions.ReceivableAlreadyCollectedException;
import com.atlashub.accounting.ar.domain.exceptions.ReceivableWrittenOffException;
import com.atlashub.accounting.ar.domain.valueobject.ArStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
public class CustomerReceivable extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long customerId;
    private final Long salesOrderId;
    private final Money amount;
    private Money paidAmount;
    private final LocalDate dueDate;
    private ArStatus status;
    private final ZonedDateTime createdAt;

    public CustomerReceivable(Long id,
                              Long organizationId,
                              Long customerId,
                              Long salesOrderId,
                              Money amount,
                              Money paidAmount,
                              LocalDate dueDate,
                              ArStatus status,
                              ZonedDateTime createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.customerId = customerId;
        this.salesOrderId = salesOrderId;
        this.amount = amount;
        this.paidAmount = paidAmount;
        this.dueDate = dueDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static CustomerReceivable create(Long id,
                                            Long organizationId,
                                            Long customerId,
                                            Long salesOrderId,
                                            Money amount,
                                            LocalDate dueDate) {
        if (id == null) {
            throw new InvalidReceivableOperationException("Customer receivable ID must not be null");
        }
        if (organizationId == null) {
            throw new InvalidReceivableOperationException("Organization ID must not be null");
        }
        if (customerId == null) {
            throw new InvalidReceivableOperationException("Customer ID must not be null");
        }
        if (salesOrderId == null) {
            throw new InvalidReceivableOperationException("Sales order ID must not be null");
        }
        if (amount == null || !amount.isPositive()) {
            throw new InvalidReceivableOperationException("Receivable amount must be positive");
        }
        if (dueDate == null) {
            throw new InvalidReceivableOperationException("Due date must not be null");
        }

        ZonedDateTime now = ZonedDateTime.now();
        Money initialPaid = Money.zero(amount.currency());

        CustomerReceivable receivable = new CustomerReceivable(
                id,
                organizationId,
                customerId,
                salesOrderId,
                amount,
                initialPaid,
                dueDate,
                ArStatus.OUTSTANDING,
                now
        );

        receivable.registerEvent(CustomerReceivableCreatedEvent.of(
                id,
                organizationId,
                customerId,
                salesOrderId,
                amount.amount(),
                amount.currency().name(),
                dueDate
        ));

        return receivable;
    }

    public void recordPayment(Money payment) {
        if (this.status == ArStatus.COLLECTED) {
            throw new ReceivableAlreadyCollectedException();
        }
        if (this.status == ArStatus.WRITTEN_OFF) {
            throw new ReceivableWrittenOffException();
        }
        if (payment == null || !payment.isPositive()) {
            throw new InvalidReceivableOperationException("Payment amount must be positive");
        }

        Money remainingBalance = getOutstandingBalance();
        if (payment.isGreaterThan(remainingBalance)) {
            throw new InvalidReceivableOperationException("Payment amount exceeds remaining balance");
        }

        this.paidAmount = this.paidAmount.add(payment);

        if (this.paidAmount.equals(this.amount)) {
            this.status = ArStatus.COLLECTED;
        } else {
            this.status = ArStatus.PARTIALLY_COLLECTED;
        }

        registerEvent(ReceivableCollectedEvent.of(
                this.id,
                this.organizationId,
                this.customerId,
                this.salesOrderId,
                payment.amount(),
                getOutstandingBalance().amount(),
                payment.currency().name(),
                ZonedDateTime.now()
        ));
    }

    public void writeOff(String reason) {
        if (this.status == ArStatus.COLLECTED) {
            throw new InvalidReceivableOperationException("Cannot write off an already collected receivable");
        }
        if (this.status == ArStatus.WRITTEN_OFF) {
            throw new ReceivableWrittenOffException("Receivable is already written off");
        }

        this.status = ArStatus.WRITTEN_OFF;

        registerEvent(ReceivableWrittenOffEvent.of(
                this.id,
                this.organizationId,
                this.customerId,
                this.salesOrderId,
                getOutstandingBalance().amount(),
                this.amount.currency().name(),
                reason,
                ZonedDateTime.now()
        ));
    }

    public Money getOutstandingBalance() {
        return this.amount.subtract(this.paidAmount);
    }

    @Override
    public Long getId() {
        return id;
    }
}
