package com.atlashub.identity.domain.model;

import com.atlashub.identity.domain.event.UserActiveOrganizationChanged;
import com.atlashub.identity.domain.event.UserCreated;
import com.atlashub.identity.domain.event.UserProfileUpdated;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.domain.exception.SharedErrorCode;
import com.atlashub.shared.domain.exception.ValidationException;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class User extends AggregateRoot<Long> {

    private final Long id;
    private String firstName;
    private String lastName;
    private final EmailAddress email;
    private String imageUrl;
    private PhoneNumber phone;
    private final Country country;
    private Long activeOrganizationId;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    /** Creation constructor — raises UserCreated event. id must be non-null (assign nextIdentity() before calling). */
    public User(Long id, String firstName, String lastName, EmailAddress email,
                PhoneNumber phone, Country country, Boolean isInvited) {
        if (id == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "User id is required");
        if (firstName == null || firstName.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "First name is required");
        if (lastName == null || lastName.isBlank()) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Last name is required");
        if (email == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Email is required");
        if (country == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Country is required");

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.country = country;
        this.activeOrganizationId = null;
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = this.createdAt;

        registerEvent(new UserCreated(
            UUID.randomUUID().toString(),
            String.valueOf(this.id),
            this.createdAt,
            new UserCreated.Payload(
                this.email.value(),
                this.firstName,
                this.lastName,
                this.country.name(),
                isInvited
            )
        ));
    }

    /** Reconstitution constructor — used by mappers only. No events raised. */
    public User(Long id, String firstName, String lastName, EmailAddress email, String imageUrl,
                PhoneNumber phone, Country country, Long activeOrganizationId,
                ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.imageUrl = imageUrl;
        this.phone = phone;
        this.country = country;
        this.activeOrganizationId = activeOrganizationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        this.updatedAt = ZonedDateTime.now();
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
            String.valueOf(this.id),
            this.updatedAt,
            new UserProfileUpdated.Payload(
                this.firstName,
                this.lastName,
                this.phone != null ? this.phone.value() : null
            )
        ));
    }

    public void switchActiveOrganization(Long organizationId) {
        if (organizationId == null) throw new ValidationException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Organization id is required");
        this.activeOrganizationId = organizationId;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(new UserActiveOrganizationChanged(
            UUID.randomUUID().toString(),
            String.valueOf(this.id),
            this.updatedAt,
            new UserActiveOrganizationChanged.Payload(this.id, organizationId)
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
