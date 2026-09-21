package com.atlashub.accounts.domain.model;

import com.atlashub.accounts.domain.exception.WeakPasswordException;
import com.atlashub.shared.domain.exception.MissingRequiredFieldException;

import com.atlashub.accounts.domain.event.*;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
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

    /**
     * Creation constructor — raises UserCreated event. id must be non-null (assign nextIdentity() before calling).
     */
    public static User create(Long id, String firstName, String lastName, EmailAddress email, Country country, Boolean isInvited, String passwordHash) {
        if (id == null) throw new MissingRequiredFieldException("User id is required");
        if (firstName == null || firstName.isBlank()) throw new MissingRequiredFieldException("First name is required");
        if (lastName == null || lastName.isBlank()) throw new MissingRequiredFieldException("Last name is required");
        if (email == null) throw new MissingRequiredFieldException("Email is required");
        if (country == null) throw new MissingRequiredFieldException("Country is required");

        _validatePassword(passwordHash);

        User user = new User(id, firstName, lastName, email, null, null, country, null, ZonedDateTime.now(), ZonedDateTime.now());

        user.registerEvent(new UserCreated(
                UUID.randomUUID().toString(),
                String.valueOf(user.id),
                user.createdAt,
                CorrelationId.getOrCreate(),
                new UserCreated.Payload(
                        user.email.value(),
                        isInvited,
                        passwordHash
                )
        ));

        return user;
    }

    /**
     * Reconstitution constructor — used by mappers only. No events raised.
     */
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
        if (firstName == null || firstName.isBlank()) throw new MissingRequiredFieldException("First name is required");
        if (lastName == null || lastName.isBlank()) throw new MissingRequiredFieldException("Last name is required");

        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(new UserProfileUpdated(
                UUID.randomUUID().toString(),
                String.valueOf(this.id),
                this.updatedAt,
                CorrelationId.getOrCreate(),
                new UserProfileUpdated.Payload(
                        this.firstName,
                        this.lastName,
                        this.phone != null ? this.phone.value() : null
                )
        ));
    }

    public void switchActiveOrganization(Long organizationId) {
        if (organizationId == null) throw new MissingRequiredFieldException("Organization id is required");
        this.activeOrganizationId = organizationId;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(new UserActiveOrganizationChanged(
                UUID.randomUUID().toString(),
                String.valueOf(this.id),
                this.updatedAt,
                CorrelationId.getOrCreate(),
                new UserActiveOrganizationChanged.Payload(this.id, organizationId)
        ));
    }

    private static void _validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new WeakPasswordException("Password cannot be empty.");
        }

        if (password.length() < 8) {
            throw new WeakPasswordException("Password must be at least 8 characters long.");
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isWhitespace(c)) hasSpecial = true; // Anything not a letter, number, or space
        }

        if (!hasUpper) {
            throw new WeakPasswordException("Password must contain at least one uppercase letter.");
        }

        if (!hasLower) {
            throw new WeakPasswordException("Password must contain at least one lowercase letter.");
        }

        if (!hasDigit) {
            throw new WeakPasswordException("Password must contain at least one number.");
        }

        if (!hasSpecial) {
            throw new WeakPasswordException("Password must contain at least one special character.");
        }
    }

    @Override
    public Long getId() {
        return id;
    }
}










