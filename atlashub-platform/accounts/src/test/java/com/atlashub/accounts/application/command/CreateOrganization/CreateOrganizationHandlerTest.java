package com.atlashub.accounts.application.command.CreateOrganization;

import com.atlashub.accounts.domain.entities.Organization;
import com.atlashub.accounts.domain.entities.User;
import com.atlashub.accounts.domain.events.OrganizationRegistered;
import com.atlashub.accounts.domain.repositories.OrganizationRepository;
import com.atlashub.accounts.domain.repositories.UserRepository;
import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateOrganizationHandlerTest {
    @Test
    void verified_user_can_create_multiple_organizations_with_owner_events() {
        UserRepository users = mock(UserRepository.class);
        OrganizationRepository organizations = mock(OrganizationRepository.class);
        User owner = User.create(1L, "Tolu", "Ade", new EmailAddress("tolu@example.com"),
                new Country("NG"), true, "Strong1!", "secret-ref");
        owner.markEmailVerified();
        when(users.findById(1L)).thenReturn(Optional.of(owner));
        when(organizations.nextIdentity()).thenReturn(21L, 22L);
        CreateOrganizationHandler handler = new CreateOrganizationHandler(organizations, users);
        var command = new CreateOrganizationCommand(1L, "Merchant", AtlasHubRegistrationType.PRIVATE_LIMITED_COMPANY,
                SupportedIndustry.RETAIL, null, null, null, new Country("NG"), CurrencyCode.NGN);

        assertEquals(21L, handler.execute(command));
        assertEquals(22L, handler.execute(command));
        ArgumentCaptor<Organization> saved = ArgumentCaptor.forClass(Organization.class);
        verify(organizations, times(2)).save(saved.capture());
        assertEquals(2, saved.getAllValues().size());
        for (Organization organization : saved.getAllValues()) {
            OrganizationRegistered event = (OrganizationRegistered) organization.pullDomainEvents().getFirst();
            assertEquals(1L, event.payload().ownerUserId());
        }
    }
}
