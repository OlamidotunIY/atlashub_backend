package com.atlashub.accounts.domain.model;

import com.atlashub.accounts.domain.event.OrganizationRegistered;
import com.atlashub.accounts.domain.event.OrganizationUpdated;
import com.atlashub.accounts.domain.valueobject.BusinessSize;
import com.atlashub.accounts.domain.valueobject.BusinessType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Country;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class Organization extends AggregateRoot<Long> {

    private final Long id;
    private String businessName;
    private final BusinessType businessType;
    private final BusinessSize businessSize;
    private String industry;
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
    public static Organization create(Long id, String businessName, BusinessType businessType, BusinessSize businessSize, String description, CurrencyCode currency, String logoUrl, Country country, String industry, String websiteUrl) {

        Organization organization = new Organization(id, businessName, businessType, businessSize, industry, description, currency, logoUrl, websiteUrl, country, ZonedDateTime.now(), ZonedDateTime.now());

        organization.registerEvent(new OrganizationRegistered(
                UUID.randomUUID().toString(),
                String.valueOf(id),
                ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new OrganizationRegistered.Payload(
                        businessName,
                        businessType,
                        businessSize,
                        currency
                )
        ));

        return organization;
    }

    /**
     * Reconstitution constructor — used by mappers only. No events raised.
     */
    public Organization(Long id, String businessName, BusinessType businessType, BusinessSize businessSize, String industry,
                        String description, CurrencyCode baseCurrency, String logoUrl, String websiteUrl, Country country,
                        ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.businessName = businessName;
        this.businessType = businessType;
        this.businessSize = businessSize;
        this.industry = industry;
        this.description = description;
        this.baseCurrency = baseCurrency;
        this.logoUrl = logoUrl;
        this.websiteUrl = websiteUrl;
        this.country = country;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateOrganization(String businessName, String description, String logoUrl, String industry, String websiteUrl) {
        this.businessName = businessName;
        this.description = description;
        this.logoUrl = logoUrl;
        this.industry = industry;
        this.websiteUrl = websiteUrl;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(new OrganizationUpdated(
                UUID.randomUUID().toString(),
                String.valueOf(id),
                this.updatedAt,
                CorrelationId.getOrCreate(),
                new OrganizationUpdated.Payload(
                        this.businessName,
                        this.description,
                        this.logoUrl,
                        this.industry,
                        this.websiteUrl
                )
        ));
    }

    @Override
    public Long getId() {
        return id;
    }
}
