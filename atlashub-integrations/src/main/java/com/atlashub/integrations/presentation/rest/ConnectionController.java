package com.atlashub.integrations.presentation.rest;

import com.atlashub.integrations.application.command.CompleteStripeAuthorization.*;
import com.atlashub.integrations.application.command.ConnectProvider.*;
import com.atlashub.integrations.application.command.DisconnectProvider.*;
import com.atlashub.integrations.application.command.StartStripeAuthorization.*;
import com.atlashub.integrations.application.command.VerifyProviderAuthorization.*;
import com.atlashub.integrations.application.query.ListAvailableProviders.*;
import com.atlashub.integrations.application.query.ListEligibleAuthorizations.*;
import com.atlashub.integrations.application.query.ListProviderConnections.*;
import com.atlashub.integrations.application.services.ConnectionService;
import com.atlashub.integrations.domain.entities.ProviderAuthorization;
import com.atlashub.integrations.domain.entities.ProviderConnection;
import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.integrations.domain.valueobject.ProviderOffering;
import com.atlashub.integrations.presentation.dto.*;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/v1/integrations")
public class ConnectionController {
    private final ConnectProviderHandler connectProvider;
    private final VerifyProviderAuthorizationHandler verifyAuthorization;
    private final DisconnectProviderHandler disconnectProvider;
    private final StartStripeAuthorizationHandler startStripeAuthorization;
    private final CompleteStripeAuthorizationHandler completeStripeAuthorization;
    private final ListAvailableProvidersHandler listAvailableProviders;
    private final ListProviderConnectionsHandler listConnections;
    private final ListEligibleAuthorizationsHandler listEligibleAuthorizations;

    public ConnectionController(ConnectProviderHandler connectProvider,
                                VerifyProviderAuthorizationHandler verifyAuthorization,
                                DisconnectProviderHandler disconnectProvider,
                                StartStripeAuthorizationHandler startStripeAuthorization,
                                CompleteStripeAuthorizationHandler completeStripeAuthorization,
                                ListAvailableProvidersHandler listAvailableProviders,
                                ListProviderConnectionsHandler listConnections,
                                ListEligibleAuthorizationsHandler listEligibleAuthorizations) {
        this.connectProvider = connectProvider;
        this.verifyAuthorization = verifyAuthorization;
        this.disconnectProvider = disconnectProvider;
        this.startStripeAuthorization = startStripeAuthorization;
        this.completeStripeAuthorization = completeStripeAuthorization;
        this.listAvailableProviders = listAvailableProviders;
        this.listConnections = listConnections;
        this.listEligibleAuthorizations = listEligibleAuthorizations;
    }

    @GetMapping("/providers")
    @PreAuthorize("hasAuthority('integrations:read')")
    public List<ProviderOffering> providers(@AuthenticationPrincipal AuthenticatedPrincipal principal,
                                            @RequestParam String merchantCountry) {
        requireOrganization(principal);
        return listAvailableProviders.execute(new ListAvailableProvidersQuery(
                merchantCountry, principal.apiEnvironment()));
    }

    @PostMapping("/connections/credentials")
    @PreAuthorize("hasAuthority('integrations:manage')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProviderConnectionResponse connectCredential(@AuthenticationPrincipal AuthenticatedPrincipal principal,
                                                        @Valid @RequestBody CredentialConnectionRequest request) {
        requireOrganization(principal);
        if (request.provider() == Provider.STRIPE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use Stripe hosted authorization");
        }
        var created = connectProvider.execute(new ConnectProviderCommand(principal.activeOrganizationId(),
                request.provider(), request.merchantCountry(), request.credential()));
        return response(created.connection(), List.of(created.authorization()));
    }

    @PostMapping("/providers/stripe/authorization-sessions")
    @PreAuthorize("hasAuthority('integrations:manage')")
    public AuthorizationSessionResponse startStripe(@AuthenticationPrincipal AuthenticatedPrincipal principal,
                                                    @Valid @RequestBody AuthorizationSessionRequest request) {
        requireOrganization(principal);
        var result = startStripeAuthorization.execute(new StartStripeAuthorizationCommand(
                principal.activeOrganizationId(), principal.apiEnvironment(), request.merchantCountry()));
        return new AuthorizationSessionResponse(result.authorizationUrl(), result.expiresAt());
    }

    @GetMapping("/stripe/callback")
    public ProviderConnectionResponse stripeCallback(@RequestParam String state,
                                                     @RequestParam(required = false) String code,
                                                     @RequestParam(required = false) String error) {
        var connected = completeStripeAuthorization.execute(
                new CompleteStripeAuthorizationCommand(state, code, error));
        return response(connected.connection(), authorizationsFor(connected.connection()));
    }

    @GetMapping("/connections")
    @PreAuthorize("hasAuthority('integrations:read')")
    public List<ProviderConnectionResponse> connections(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        requireOrganization(principal);
        return groupedConnections(principal.activeOrganizationId());
    }

    @PostMapping("/authorizations/{authorizationId}/verify")
    @PreAuthorize("hasAuthority('integrations:manage')")
    public ProviderConnectionResponse verify(@AuthenticationPrincipal AuthenticatedPrincipal principal,
                                             @PathVariable Long authorizationId) {
        requireOrganization(principal);
        var verified = verifyAuthorization.execute(new VerifyProviderAuthorizationCommand(
                principal.activeOrganizationId(), authorizationId));
        return response(verified.connection(), authorizationsFor(verified.connection()));
    }

    @DeleteMapping("/connections/{connectionId}")
    @PreAuthorize("hasAuthority('integrations:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnect(@AuthenticationPrincipal AuthenticatedPrincipal principal,
                           @PathVariable Long connectionId) {
        requireOrganization(principal);
        disconnectProvider.execute(new DisconnectProviderCommand(principal.activeOrganizationId(), connectionId));
    }

    @GetMapping("/connections/eligible")
    @PreAuthorize("hasAuthority('integrations:read')")
    public List<ProviderAuthorizationResponse> eligible(@AuthenticationPrincipal AuthenticatedPrincipal principal,
                                                       @RequestParam String currency,
                                                       @RequestParam Capability capability) {
        requireOrganization(principal);
        return listEligibleAuthorizations.execute(new ListEligibleAuthorizationsQuery(
                        principal.activeOrganizationId(), principal.apiEnvironment(),
                        currency.toUpperCase(Locale.ROOT), capability)).stream()
                .map(ConnectionService.ConnectedAuthorization::authorization)
                .map(this::response).toList();
    }

    private List<ProviderConnectionResponse> groupedConnections(Long organizationId) {
        Map<Long, List<ConnectionService.ConnectedAuthorization>> grouped = new LinkedHashMap<>();
        listConnections.execute(new ListProviderConnectionsQuery(organizationId)).forEach(value ->
                grouped.computeIfAbsent(value.connection().id(), ignored -> new ArrayList<>()).add(value));
        return grouped.values().stream().map(values -> response(values.getFirst().connection(),
                values.stream().map(ConnectionService.ConnectedAuthorization::authorization).toList())).toList();
    }

    private List<ProviderAuthorization> authorizationsFor(ProviderConnection connection) {
        return listConnections.execute(new ListProviderConnectionsQuery(connection.organizationId())).stream()
                .filter(value -> value.connection().id().equals(connection.id()))
                .map(ConnectionService.ConnectedAuthorization::authorization).toList();
    }

    private ProviderConnectionResponse response(ProviderConnection connection,
                                                List<ProviderAuthorization> authorizations) {
        return new ProviderConnectionResponse(connection.id(), connection.provider(), connection.merchantCountry(),
                connection.status().name(), authorizations.stream().map(this::response).toList());
    }

    private ProviderAuthorizationResponse response(ProviderAuthorization authorization) {
        return new ProviderAuthorizationResponse(authorization.id(), authorization.environment().name(),
                authorization.externalMerchantId(), authorization.status().name(), authorization.health().name(),
                authorization.activeCapabilities());
    }

    private void requireOrganization(AuthenticatedPrincipal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (principal.activeOrganizationId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Select an organization first");
        }
    }
}
