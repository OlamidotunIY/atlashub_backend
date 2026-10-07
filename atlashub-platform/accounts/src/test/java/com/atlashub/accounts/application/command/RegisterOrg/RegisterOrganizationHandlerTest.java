package com.atlashub.accounts.application.command.RegisterOrg;

import com.atlashub.accounts.domain.entities.Organization;
import com.atlashub.accounts.domain.entities.User;
import com.atlashub.accounts.domain.repositories.OrganizationRepository;
import com.atlashub.accounts.domain.repositories.UserRepository;
import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;
import com.atlashub.shared.application.port.OneTimeSecretStore;
import com.atlashub.shared.domain.valueobject.Country;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterOrganizationHandlerTest {

    @Test
    void registers_user_and_organization_with_a_one_time_credential_reference() {
        OrganizationRepository organizations = mock(OrganizationRepository.class);
        UserRepository users = mock(UserRepository.class);
        OneTimeSecretStore secrets = mock(OneTimeSecretStore.class);
        when(users.findByEmail("tolu@example.com")).thenReturn(Optional.empty());
        when(users.nextIdentity()).thenReturn(10L);
        when(organizations.nextIdentity()).thenReturn(20L);
        when(secrets.store("Strong1!", Duration.ofHours(24))).thenReturn("credential-reference");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(organizations.save(any(Organization.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterOrganizationHandler handler = new RegisterOrganizationHandler(organizations, users, secrets);
        handler.execute(new RegisterOrganizationCommand(
                "Tolu Store", AtlasHubRegistrationType.SOLE_PROPRIETORSHIP,
                SupportedIndustry.RETAIL, null, null, null, new Country("NG"),
                "Tolu", "Ade", "tolu@example.com", "Strong1!", false));

        verify(secrets).store("Strong1!", Duration.ofHours(24));

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(users, times(1)).save(user.capture());
        assertEquals(20L, user.getValue().getActiveOrganizationId());

        ArgumentCaptor<Organization> organization = ArgumentCaptor.forClass(Organization.class);
        verify(organizations).save(organization.capture());
        assertEquals(LocalDate.now(), organization.getValue().getRegistrationDate());
    }
}
