package com.atlashub.accounts.application.query.GetUserProfile;

import com.atlashub.accounts.domain.entities.Organization;
import com.atlashub.accounts.domain.entities.User;
import com.atlashub.accounts.domain.repositories.OrganizationRepository;
import com.atlashub.accounts.domain.repositories.UserRepository;
import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetUserProfileHandlerTest {

    @Test
    void returns_the_active_environment_role_and_full_active_organization() {
        UserRepository users = mock(UserRepository.class);
        OrganizationRepository organizations = mock(OrganizationRepository.class);
        MembershipQueryPort memberships = mock(MembershipQueryPort.class);
        ZonedDateTime now = ZonedDateTime.now();
        User user = new User(1L, "Tolu", "Adebayo", new EmailAddress("tolu@example.com"), null,
                null, new Country("NG"), 2L, ApiEnvironment.LIVE, true, null, null, now, now);
        Organization organization = new Organization(2L, "Tolu's Store", AtlasHubRegistrationType.SOLE_PROPRIETORSHIP,
                SupportedIndustry.RETAIL, LocalDate.of(2026, 10, 7), "Retail store", CurrencyCode.NGN,
                "https://cdn.example/logo.png", "https://tolus.store", new Country("NG"), now, now);
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(memberships.listOrganizationIds(1L)).thenReturn(List.of(2L));
        when(organizations.findAllByIds(List.of(2L))).thenReturn(List.of(organization));
        when(organizations.findById(2L)).thenReturn(Optional.of(organization));
        when(memberships.getActiveRoleName(1L, 2L)).thenReturn(Optional.of("Owner"));

        UserProfileResult result = new GetUserProfileHandler(users, organizations, memberships)
                .execute(new GetUserProfileQuery(1L));

        assertEquals(ApiEnvironment.LIVE, result.activeEnvironment());
        assertEquals("Owner", result.activeOrganizationRole());
        assertNotNull(result.activeOrganization());
        assertEquals("Tolu's Store", result.activeOrganization().businessName());
        assertEquals("https://cdn.example/logo.png", result.activeOrganization().logoUrl());
        assertEquals(1, result.organizations().size());
    }
}
