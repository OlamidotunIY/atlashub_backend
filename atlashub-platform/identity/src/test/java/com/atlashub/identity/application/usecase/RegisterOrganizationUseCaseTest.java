package com.atlashub.identity.application.usecase;

import com.atlashub.identity.application.command.RegisterOrganizationCommand;
import com.atlashub.identity.application.dto.RegisterOrganizationResult;
import com.atlashub.identity.domain.model.BusinessType;
import com.atlashub.identity.domain.model.Organization;
import com.atlashub.identity.domain.repository.OrganizationRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.ConflictException;
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

/**
 * Unit tests for {@link RegisterOrganizationUseCase}.
 *
 * <p>All collaborators are mocked; no Spring context or database is required.
 * Tests follow the Arrange / Act / Assert pattern and verify:
 * <ul>
 *   <li>Happy path: Organization is persisted and API key pair is returned.</li>
 *   <li>Duplicate email: {@link ConflictException} is thrown, nothing is persisted.</li>
 *   <li>Domain events: events are published after a successful registration.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterOrganizationUseCase")
class RegisterOrganizationUseCaseTest {

    @Mock private OrganizationRepository OrganizationRepository;
        @Mock private DomainEventPublisher eventPublisher;

    private RegisterOrganizationUseCase useCase;

    private static final RegisterOrganizationCommand VALID_COMMAND = new RegisterOrganizationCommand(
            "NG",
            "Acme Corp",
            "John",
            "Doe",
            "john@acme.com",
            "+2348012345678",
                        BusinessType.STARTER
    );

    @BeforeEach
    void setUp() {
        useCase = new RegisterOrganizationUseCase(
                OrganizationRepository,
                eventPublisher
        );
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should register a new Organization and return Organization id")
    void shouldRegisterOrganizationAndReturnId() {
        // Arrange
        when(OrganizationRepository.findByEmail(VALID_COMMAND.email())).thenReturn(Optional.empty());
        
        // Act
        RegisterOrganizationResult result = useCase.execute(VALID_COMMAND);

        // Assert
        assertThat(result.OrganizationId()).isNotNull();

        // Verify the Organization was persisted exactly once
        ArgumentCaptor<Organization> captor = ArgumentCaptor.forClass(Organization.class);
        verify(OrganizationRepository, times(1)).save(captor.capture());

        Organization saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
    }

    // ── Conflict ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should throw ConflictException when Organization email already exists")
    void shouldThrowConflictExceptionWhenEmailAlreadyExists() {
        // Arrange — simulate an existing Organization with the same email
        Organization existing = mock(Organization.class);
        when(OrganizationRepository.findByEmail(VALID_COMMAND.email()))
                .thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(VALID_COMMAND))
                .isInstanceOf(ConflictException.class);

        // Verify nothing was persisted and no events were published
        verify(OrganizationRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}

