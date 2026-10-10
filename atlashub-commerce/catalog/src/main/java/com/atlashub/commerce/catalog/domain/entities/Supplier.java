package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.exceptions.InvalidProductStateException;
import com.atlashub.commerce.catalog.domain.valueobject.SupplierStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class Supplier extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private String name;
    private EmailAddress email;
    private PhoneNumber phone;
    private String address;
    private SupplierStatus status;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Supplier(Long id, Long organizationId, String name, EmailAddress email,
                    PhoneNumber phone, String address, SupplierStatus status,
                    ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        validateInvariants();
    }

    public static Supplier create(Long id, Long organizationId, String name,
                                  EmailAddress email, PhoneNumber phone, String address) {
        ZonedDateTime now = ZonedDateTime.now();
        return new Supplier(id, organizationId, name, email, phone, address, SupplierStatus.ACTIVE, now, now);
    }

    public void deactivate() {
        if (this.status == SupplierStatus.INACTIVE) {
            return;
        }
        this.status = SupplierStatus.INACTIVE;
        touch();
    }

    public void reactivate() {
        if (this.status == SupplierStatus.ACTIVE) {
            return;
        }
        this.status = SupplierStatus.ACTIVE;
        touch();
    }

    public void updateDetails(String name, EmailAddress email, PhoneNumber phone, String address) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductStateException("Supplier name is required");
        }
        this.name = name.trim();
        this.email = email;
        this.phone = phone;
        this.address = address != null ? address.trim() : null;
        touch();
    }

    public boolean isActive() {
        return this.status == SupplierStatus.ACTIVE;
    }

    private void validateInvariants() {
        if (id == null) {
            throw new InvalidProductStateException("Supplier id cannot be null");
        }
        if (organizationId == null) {
            throw new InvalidProductStateException("Organization id cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidProductStateException("Supplier name is required");
        }
        if (status == null) {
            throw new InvalidProductStateException("Supplier status cannot be null");
        }
        if (createdAt == null || updatedAt == null) {
            throw new InvalidProductStateException("Timestamps cannot be null");
        }
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
