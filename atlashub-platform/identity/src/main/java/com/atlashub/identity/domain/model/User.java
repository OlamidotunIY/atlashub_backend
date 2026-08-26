package com.atlashub.identity.domain.model;

import com.atlashub.identity.domain.event.UserCreated;
import com.atlashub.identity.domain.event.UserProfileUpdated;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.exception.SharedErrorCode;
import com.atlashub.shared.exception.ValidationException;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class User extends AggregateRoot<Long> {

    private final Long id;
    private final String code;
    private final Long integration;
    private String firstName;
    private String lastName;
    private final EmailAddress email;
    private PhoneNumber phone;
    private Map<String, String> metadata;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public User(Long id, String code, Long integration, String firstName, String lastName, EmailAddress email, PhoneNumber phone, Map<String, String> metadata) {
        if (integration == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Integration ID is required");
        if (firstName == null || firstName.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "First name is required");
        if (lastName == null || lastName.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Last name is required");
        if (email == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Email is required");

        this.id = id;
        this.code = code != null ? code : "CUS_" + UUID.randomUUID().toString().replace("-", "");
        this.integration = integration;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = this.createdAt;

        // If this is a new User (id is null), raise the created event
        if (id == null) {
            registerEvent(new UserCreated(
                UUID.randomUUID().toString(),
                this.code, // Assuming event schema accepts String for Aggregate ID or we just pass the code
                this.createdAt,
                new UserCreated.Payload(
                    this.integration,
                    this.email.value(),
                    this.firstName,
                    this.lastName
                )
            ));
        }
    }

    public void updateProfile(String firstName, String lastName, PhoneNumber phone) {
        if (firstName == null || firstName.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "First name is required");
        if (lastName == null || lastName.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Last name is required");

        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(new UserProfileUpdated(
            UUID.randomUUID().toString(),
            this.id != null ? this.id.toString() : this.code,
            this.updatedAt,
            new UserProfileUpdated.Payload(
                this.integration,
                this.firstName,
                this.lastName,
                this.phone != null ? this.phone.value() : null
            )
        ));
    }

    public void updateMetadata(Map<String, String> metadata) {
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
