package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.events.TillClosedEvent;
import com.atlashub.commerce.storefront.domain.events.TillOpenedEvent;
import com.atlashub.commerce.storefront.domain.exceptions.InvalidOrderStateException;
import com.atlashub.commerce.storefront.domain.exceptions.TillNotOpenException;
import com.atlashub.commerce.storefront.domain.valueobject.TillStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Objects;

@Getter
public class Till extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long outletId;
    private final String name;
    private final Money openingFloat;
    private Money expectedClosingBalance;
    private Money actualClosingBalance;
    private TillStatus status;
    private final ZonedDateTime openedAt;
    private ZonedDateTime closedAt;
    private final Long openedBy;
    private Long closedBy;

    public Till(
            Long id,
            Long organizationId,
            Long outletId,
            String name,
            Money openingFloat,
            Money expectedClosingBalance,
            Money actualClosingBalance,
            TillStatus status,
            ZonedDateTime openedAt,
            ZonedDateTime closedAt,
            Long openedBy,
            Long closedBy
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.outletId = outletId;
        this.name = name;
        this.openingFloat = openingFloat;
        this.expectedClosingBalance = expectedClosingBalance;
        this.actualClosingBalance = actualClosingBalance;
        this.status = status;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.openedBy = openedBy;
        this.closedBy = closedBy;
    }

    public static Till create(
            Long id,
            Long organizationId,
            Long outletId,
            String name,
            Long openedBy,
            Money openingFloat
    ) {
        Objects.requireNonNull(id, "Till ID must not be null");
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(outletId, "Outlet ID must not be null");
        Objects.requireNonNull(name, "Till name must not be null");
        Objects.requireNonNull(openedBy, "OpenedBy user ID must not be null");
        Objects.requireNonNull(openingFloat, "Opening float must not be null");

        ZonedDateTime now = ZonedDateTime.now();
        Till till = new Till(
                id,
                organizationId,
                outletId,
                name,
                openingFloat,
                openingFloat,
                null,
                TillStatus.OPEN,
                now,
                null,
                openedBy,
                null
        );

        till.registerEvent(TillOpenedEvent.of(
                id,
                organizationId,
                outletId,
                openedBy,
                openingFloat.amount(),
                openingFloat.currency().name()
        ));

        return till;
    }

    public void recordCashSale(Money amount) {
        if (status != TillStatus.OPEN) {
            throw new TillNotOpenException("Cannot record sale on till with status: " + status);
        }
        Objects.requireNonNull(amount, "Amount must not be null");
        this.expectedClosingBalance = this.expectedClosingBalance.add(amount);
    }

    public void close(Long closedByUserId, Money actualBalance) {
        if (status != TillStatus.OPEN) {
            throw new TillNotOpenException("Cannot close till with status: " + status);
        }
        Objects.requireNonNull(closedByUserId, "ClosedBy user ID must not be null");
        Objects.requireNonNull(actualBalance, "Actual balance must not be null");

        this.status = TillStatus.CLOSED;
        this.closedBy = closedByUserId;
        this.actualClosingBalance = actualBalance;
        this.closedAt = ZonedDateTime.now();

        Money totalSales = expectedClosingBalance.subtract(openingFloat);
        registerEvent(TillClosedEvent.of(
                id,
                organizationId,
                outletId,
                closedByUserId,
                actualBalance.amount(),
                totalSales.amount(),
                actualBalance.currency().name()
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
