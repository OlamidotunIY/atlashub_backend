package com.atlashub.accounts.domain.entities;

import com.atlashub.accounts.domain.events.OutletClosedEvent;
import com.atlashub.accounts.domain.events.OutletCreatedEvent;
import com.atlashub.accounts.domain.events.OutletSuspendedEvent;
import com.atlashub.accounts.domain.events.OutletUpdatedEvent;
import com.atlashub.accounts.domain.valueobject.OutletStatus;
import com.atlashub.accounts.domain.exceptions.InvalidOutletStateException;
import com.atlashub.accounts.domain.exceptions.InvalidOutletException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class Outlet extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private String name;
    private String address;
    private String city;
    private String state;
    private final Country country;
    private final CurrencyCode currency;
    private Long managerId;
    private OutletStatus status;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    /** Reconstitution constructor — used by MapStruct only. No events raised. */
    public Outlet(Long id, Long organizationId, String name, String address, String city, String state,
                  Country country, CurrencyCode currency, Long managerId, OutletStatus status,
                  ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.currency = currency;
        this.managerId = managerId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Static factory — enforces invariants and raises OutletCreatedEvent. */
    public static Outlet create(Long id, Long organizationId, String name, String address,
                                String city, String state, Country country, CurrencyCode currency,
                                Long managerId) {
        if (id == null) throw new InvalidOutletException("Outlet id is required");
        if (organizationId == null) throw new InvalidOutletException("Organization id is required");
        if (name == null || name.isBlank()) throw new InvalidOutletException("Outlet name is required");
        if (country == null) throw new InvalidOutletException("Country is required");
        if (currency == null) throw new InvalidOutletException("Currency is required");

        ZonedDateTime now = ZonedDateTime.now();
        Outlet outlet = new Outlet(id, organizationId, name, address, city, state, country, currency,
                managerId, OutletStatus.ACTIVE, now, now);

        outlet.registerEvent(new OutletCreatedEvent(
                UUID.randomUUID().toString(),
                outlet.id,
                now,
                CorrelationId.getOrCreate(),
                new OutletCreatedEvent.Payload(
                        id, organizationId, name, address, city, state,
                        country.code(), currency.name()
                )
        ));

        return outlet;
    }

    public void updateDetails(String name, String address, String city, String state, Long managerId) {
        if (name == null || name.isBlank()) throw new InvalidOutletException("Outlet name is required");
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.managerId = managerId;
        touch();

        registerEvent(new OutletUpdatedEvent(
                UUID.randomUUID().toString(),
                this.id,
                this.updatedAt,
                CorrelationId.getOrCreate(),
                new OutletUpdatedEvent.Payload(name, address, city, state)
        ));
    }

    public void suspend() {
        if (this.status != OutletStatus.ACTIVE) {
            throw new InvalidOutletStateException("Only ACTIVE outlets can be suspended");
        }
        this.status = OutletStatus.SUSPENDED;
        touch();

        registerEvent(new OutletSuspendedEvent(
                UUID.randomUUID().toString(),
                this.id,
                this.updatedAt,
                CorrelationId.getOrCreate(),
                new OutletSuspendedEvent.Payload(this.organizationId)
        ));
    }

    public void close() {
        if (this.status == OutletStatus.CLOSED) {
            throw new InvalidOutletStateException("Outlet is already closed");
        }
        this.status = OutletStatus.CLOSED;
        touch();

        registerEvent(new OutletClosedEvent(
                UUID.randomUUID().toString(),
                this.id,
                this.updatedAt,
                CorrelationId.getOrCreate(),
                new OutletClosedEvent.Payload(this.organizationId)
        ));
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
