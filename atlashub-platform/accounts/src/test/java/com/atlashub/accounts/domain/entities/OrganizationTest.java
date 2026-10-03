package com.atlashub.accounts.domain.entities;

import com.atlashub.accounts.domain.events.OrganizationRegistered;
import com.atlashub.accounts.domain.exceptions.InvalidOrganizationException;
import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrganizationTest {

    @Test
    void creates_an_atlashub_organization_with_generated_registration_date() {
        Organization organization = Organization.create(
                1L, "Tolu Store", AtlasHubRegistrationType.SOLE_PROPRIETORSHIP,
                "Retail store", CurrencyCode.NGN, null,
                new Country("NG"), SupportedIndustry.RETAIL, null, 10L);

        assertEquals(LocalDate.now(), organization.getRegistrationDate());

        OrganizationRegistered event = (OrganizationRegistered)
                organization.pullDomainEvents().getFirst();
        assertEquals(LocalDate.now(), event.payload().registrationDate());
        assertEquals(AtlasHubRegistrationType.SOLE_PROPRIETORSHIP,
                event.payload().registrationType());
    }

    @Test
    void rejects_a_missing_currency() {
        assertThrows(InvalidOrganizationException.class, () -> Organization.create(
                1L, "Tolu Store", AtlasHubRegistrationType.SOLE_PROPRIETORSHIP,
                null, null, null, new Country("NG"),
                SupportedIndustry.RETAIL, null, 10L));
    }
}
