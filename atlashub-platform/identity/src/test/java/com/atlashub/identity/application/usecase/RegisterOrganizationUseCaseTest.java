package com.atlashub.identity.application.usecase;

import com.atlashub.identity.application.command.RegisterOrganizationCommand;
import com.atlashub.identity.application.result.RegisterOrganizationResult;
import com.atlashub.identity.domain.valueobject.BusinessType;
import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.model.OrganizationMember;
import com.atlashub.identity.domain.model.User;
import com.atlashub.identity.domain.repository.OrganizationRepository;
import com.atlashub.identity.domain.repository.OrganizationMemberRepository;
import com.atlashub.identity.domain.repository.UserRepository;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterOrganizationUseCase")
class RegisterOrganizationUseCaseTest {

    @Mock private OrganizationRepository organizationRepository;
    @Mock private OrganizationMemberRepository memberRepository;
    @Mock private UserRepository userRepository;
    @Mock private DomainEventPublisher eventPublisher;

    private RegisterOrganizationUseCase useCase;

    private static final RegisterOrganizationCommand VALID_COMMAND = new RegisterOrganizationCommand(
            1L,
            "Acme Corp",
            BusinessType.STARTER
    );

    @BeforeEach
    void setUp() {
        useCase = new RegisterOrganizationUseCase(
                organizationRepository,
                memberRepository,
                userRepository,
                eventPublisher
        );
    }

    @Test
    @DisplayName("should register a new Organization and set user active organization if null")
    void shouldRegisterOrganizationAndReturnId() {
        // Arrange
        when(organizationRepository.nextIdentity()).thenReturn(100L);
        User user = new User(1L, "John", "Doe", new EmailAddress("john@example.com"), null, Country.NIGERIA);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // Act
        RegisterOrganizationResult result = useCase.execute(VALID_COMMAND);

        // Assert
        assertThat(result.organizationId()).isEqualTo(100L);

        ArgumentCaptor<Organization> orgCaptor = ArgumentCaptor.forClass(Organization.class);
        verify(organizationRepository).save(orgCaptor.capture());
        assertThat(orgCaptor.getValue().getBusinessName()).isEqualTo("Acme Corp");

        ArgumentCaptor<OrganizationMember> memberCaptor = ArgumentCaptor.forClass(OrganizationMember.class);
        verify(memberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getUserId()).isEqualTo(1L);
        assertThat(memberCaptor.getValue().getOrganizationId()).isEqualTo(100L);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getActiveOrganizationId()).isEqualTo(100L);
        
        verify(eventPublisher, times(2)).publish(any());
    }

    @Test
    @DisplayName("should throw NotFoundException when user does not exist")
    void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        // Arrange
        when(organizationRepository.nextIdentity()).thenReturn(100L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(VALID_COMMAND))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
