package com.atlashub.accounts.domain.entities;

import com.atlashub.accounts.domain.events.OrganizationRegistered;
import com.atlashub.accounts.domain.events.OrganizationUpdated;
import com.atlashub.accounts.domain.exceptions.InvalidOrganizationException;
import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class Organization extends AggregateRoot<Long> {

    private final Long id;
    private String businessName;
    private final AtlasHubRegistrationType registrationType;
    private final SupportedIndustry industry;
    private final LocalDate registrationDate;
    private String description;
    private String logoUrl;
    private String websiteUrl;
    private final Country country;
    private final CurrencyCode baseCurrency;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    /**
     * Creation constructor — raises OrganizationRegistered event.
     */
    public static Organization create(Long id, String businessName, AtlasHubRegistrationType registrationType, String description, CurrencyCode currency, String logoUrl, Country country, SupportedIndustry industry, String websiteUrl, Long ownerUserId) {
        validateRequired(id, businessName, registrationType, currency, country, industry, ownerUserId);

        LocalDate registrationDate = LocalDate.now();

        Organization organization = new Organization(id, businessName, registrationType, industry, registrationDate, description, currency, logoUrl, websiteUrl, country, ZonedDateTime.now(), ZonedDateTime.now());

        organization.registerEvent(new OrganizationRegistered(UUID.randomUUID().toString(), organization.id, ZonedDateTime.now(), CorrelationId.getOrCreate(), new OrganizationRegistered.Payload(businessName, registrationType, industry, registrationDate, country, currency, ownerUserId)));

        return organization;
    }

    /**
     * Reconstitution constructor — used by mappers only. No events raised.
     */
    public Organization(Long id, String businessName, AtlasHubRegistrationType registrationType, SupportedIndustry industry, LocalDate registrationDate, String description, CurrencyCode baseCurrency, String logoUrl, String websiteUrl, Country country, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.businessName = businessName;
        this.registrationType = registrationType;
        this.industry = industry;
        this.registrationDate = registrationDate;
        this.description = description;
        this.baseCurrency = baseCurrency;
        this.logoUrl = logoUrl;
        this.websiteUrl = websiteUrl;
        this.country = country;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateOrganization(String businessName, String description, String logoUrl, SupportedIndustry industry, String websiteUrl) {
        if (businessName == null || businessName.isBlank()) {
            throw new InvalidOrganizationException("Business name is required");
        }
        if (industry != null && industry != this.industry) {
            throw new InvalidOrganizationException("Industry cannot be changed after registration");
        }
        this.businessName = businessName;
        this.description = description;
        this.logoUrl = logoUrl;
        this.websiteUrl = websiteUrl;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(new OrganizationUpdated(UUID.randomUUID().toString(), this.id, this.updatedAt, CorrelationId.getOrCreate(), new OrganizationUpdated.Payload(this.businessName, this.description, this.logoUrl, this.industry, this.websiteUrl)));
    }

    private static void validateRequired(Long id, String businessName, AtlasHubRegistrationType registrationType, CurrencyCode currency, Country country, SupportedIndustry industry, Long ownerUserId) {
        if (id == null || ownerUserId == null) {
            throw new InvalidOrganizationException("Organization and owner identifiers are required");
        }
        if (businessName == null || businessName.isBlank()) {
            throw new InvalidOrganizationException("Business name is required");
        }
        if (registrationType == null || industry == null || currency == null || country == null) {
            throw new InvalidOrganizationException("Registration type, industry, country, and currency are required");
        }
        if (currency != country.deriveCurrency()) {
            throw new InvalidOrganizationException("Currency must match the organization's country");
        }
    }

    @Override
    public Long getId() {
        return id;
    }
}
