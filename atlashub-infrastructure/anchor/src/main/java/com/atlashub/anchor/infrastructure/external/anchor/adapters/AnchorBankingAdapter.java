package com.atlashub.anchor.infrastructure.external.anchor.adapters;

import com.atlashub.anchor.infrastructure.external.anchor.client.AnchorClientRegistry;
import com.atlashub.anchor.infrastructure.external.anchor.client.AnchorClients;
import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorEnvironment;
import com.atlashub.anchor.infrastructure.external.anchor.configuration.AnchorProperties;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorBank;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorRelationship;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorRequest;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorResourceIdentifier;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorToManyRelationship;
import com.atlashub.anchor.infrastructure.external.anchor.dto.deposit.CreateDepositAccountData;
import com.atlashub.anchor.infrastructure.external.anchor.dto.deposit.DepositAccountResource;
import com.atlashub.anchor.infrastructure.external.anchor.dto.deposit.FreezeDepositAccountData;
import com.atlashub.anchor.infrastructure.external.anchor.dto.deposit.UnfreezeDepositAccountData;
import com.atlashub.anchor.infrastructure.external.anchor.dto.reservedaccount.CreateReservedAccountData;
import com.atlashub.anchor.infrastructure.external.anchor.dto.reservedaccount.ReservedAccountResource;
import com.atlashub.anchor.infrastructure.external.anchor.dto.subaccount.CreateSubAccountData;
import com.atlashub.anchor.infrastructure.external.anchor.dto.subaccount.SubAccountResource;
import com.atlashub.shared.application.port.AnchorBankingPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Pay-accounts mapping for its banking port. Transport, credentials, timeouts, and endpoint
 * declarations are owned exclusively by the shared Anchor module.
 *
 */
@Component
public class AnchorBankingAdapter implements AnchorBankingPort {
    private final ObjectProvider<AnchorClientRegistry> clientRegistryProvider;
    private final ObjectProvider<AnchorProperties> propertiesProvider;

    public AnchorBankingAdapter(ObjectProvider<AnchorClientRegistry> clientRegistryProvider,
                                ObjectProvider<AnchorProperties> propertiesProvider) {
        this.clientRegistryProvider = clientRegistryProvider;
        this.propertiesProvider = propertiesProvider;
    }

    @Override
    public boolean supports(String capability, String apiEnvironment) {
        AnchorProperties.ProgrammeCapabilities capabilities = capabilities(apiEnvironment);
        return switch (capability.toUpperCase()) {
            case "DEPOSIT_ACCOUNT" -> capabilities.depositAccounts();
            case "SUB_ACCOUNT" -> capabilities.subAccounts();
            case "RESERVED_ACCOUNT" -> capabilities.reservedAccounts();
            case "TRANSFER" -> capabilities.transfers();
            default -> false;
        };
    }

    @Override
    public String requireFboAccountId(String apiEnvironment) {
        String accountId = capabilities(apiEnvironment).fboAccountId();
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalStateException("Anchor FBO account is unavailable in " + apiEnvironment.toUpperCase());
        }
        return accountId;
    }

    @Override
    public DepositAccountResult createBusinessDepositAccount(String customerId, String productName, String requestReference, String apiEnvironment) {
        requireCapability("DEPOSIT_ACCOUNT", apiEnvironment);
        DepositAccountResource resource = requireData(clients(apiEnvironment).depositAccounts().createDepositAccount(requestReference, new AnchorRequest<>(
                new CreateDepositAccountData(
                        new CreateDepositAccountData.Attributes(productName),
                        new CreateDepositAccountData.Relationships(relationship(customerId, "BusinessCustomer"))
                )
        )).data(), "Anchor did not return a deposit account");
        return new DepositAccountResult(resource.id(), status(resource.attributes() == null ? null : resource.attributes().status()));
    }

    @Override
    public SubAccountResult createBusinessSubAccount(
            String customerId,
            String parentFboAccountId,
            boolean createVirtualNuban,
            String requestReference,
            String apiEnvironment
    ) {
        requireCapability("SUB_ACCOUNT", apiEnvironment);
        SubAccountResource resource = requireData(clients(apiEnvironment).subAccounts().createSubAccount(requestReference, new AnchorRequest<>(
                new CreateSubAccountData(
                        new CreateSubAccountData.Attributes(createVirtualNuban),
                        new CreateSubAccountData.Relationships(
                                relationship(customerId, "BusinessCustomer"),
                                relationship(parentFboAccountId, "DepositAccount")
                        )
                )
        )).data(), "Anchor did not return a subaccount");
        return new SubAccountResult(resource.id(), virtualNubanId(resource.relationships()), "PENDING");
    }

    @Override
    public ReservedAccountResult createReservedAccount(
            ReservedAccountCustomer customer,
            String provider,
            String payoutSubAccountId,
            String requestReference,
            String apiEnvironment
    ) {
        requireCapability("RESERVED_ACCOUNT", apiEnvironment);
        boolean businessCustomer = "BUSINESS".equalsIgnoreCase(customer.type());
        if (businessCustomer && (customer.providerCustomerId() == null || customer.providerCustomerId().isBlank())) {
            throw new IllegalArgumentException("Anchor business-customer ID is required for a business reserved account");
        }
        CreateReservedAccountData.Customer inlineCustomer = businessCustomer ? null
                : new CreateReservedAccountData.Customer(new CreateReservedAccountData.IndividualCustomer(
                        splitName(customer.fullName()), customer.email(), customer.bvn()));
        AnchorRelationship customerRelationship = businessCustomer
                ? relationship(customer.providerCustomerId(), "BusinessCustomer") : null;
        ReservedAccountResource resource = requireData(clients(apiEnvironment).reservedAccounts()
                .createReservedAccount(requestReference, new AnchorRequest<>(new CreateReservedAccountData(
                        new CreateReservedAccountData.Attributes(provider, inlineCustomer),
                        new CreateReservedAccountData.Relationships(
                                relationship(payoutSubAccountId, "SubAccount"), customerRelationship))))
                .data(), "Anchor did not return a reserved account");
        String anchorCustomerId = resource.relationships() == null || resource.relationships().customer() == null
                || resource.relationships().customer().data() == null
                ? null : resource.relationships().customer().data().id();
        return new ReservedAccountResult(resource.id(), anchorCustomerId, "PENDING");
    }

    @Override
    public AccountDetails fetchDepositAccount(String anchorAccountId, String apiEnvironment) {
        DepositAccountResource resource = requireData(clients(apiEnvironment).depositAccounts().getDepositAccount(anchorAccountId).data(), "Anchor deposit account was not found");
        DepositAccountResource.Attributes attributes = resource.attributes();
        return details(resource.id(), attributes == null ? null : attributes.accountName(),
                attributes == null ? null : attributes.accountNumber(), attributes == null ? null : attributes.bank(),
                attributes == null ? null : attributes.currency(), attributes == null ? null : attributes.status());
    }

    @Override
    public AccountDetails fetchSubAccount(String anchorSubAccountId, String apiEnvironment) {
        SubAccountResource resource = requireData(clients(apiEnvironment).subAccounts().getSubAccount(anchorSubAccountId).data(), "Anchor subaccount was not found");
        return new AccountDetails(resource.id(), null, null, null, null, null, "NGN", "ACTIVE");
    }

    @Override
    public AccountDetails fetchReservedAccount(String anchorReservedAccountId, String apiEnvironment) {
        ReservedAccountResource resource = requireData(clients(apiEnvironment).reservedAccounts().getReservedAccount(anchorReservedAccountId).data(), "Anchor reserved account was not found");
        ReservedAccountResource.Attributes attributes = resource.attributes();
        return details(resource.id(), attributes == null ? null : attributes.accountName(),
                attributes == null ? null : attributes.accountNumber(), attributes == null ? null : attributes.bank(), "NGN", "ACTIVE");
    }

    @Override
    public void freezeDepositAccount(String anchorAccountId, String reason, String apiEnvironment) {
        clients(apiEnvironment).depositAccounts().freezeDepositAccount(anchorAccountId, new AnchorRequest<>(
                new FreezeDepositAccountData(new FreezeDepositAccountData.Attributes(reason, reason))
        ));
    }

    @Override
    public void unfreezeDepositAccount(String anchorAccountId, String apiEnvironment) {
        clients(apiEnvironment).depositAccounts().unfreezeDepositAccount(new AnchorRequest<>(new UnfreezeDepositAccountData(anchorAccountId)));
    }

    private AnchorClients clients(String apiEnvironment) {
        AnchorClientRegistry registry = clientRegistryProvider.getIfAvailable();
        if (registry == null) {
            throw new IllegalStateException("Anchor integration is disabled or not configured");
        }
        return registry.forEnvironment(toAnchorEnvironment(apiEnvironment));
    }

    private AnchorProperties.ProgrammeCapabilities capabilities(String apiEnvironment) {
        AnchorProperties properties = propertiesProvider.getIfAvailable();
        if (properties == null) {
            throw new IllegalStateException("Anchor integration is disabled or not configured");
        }
        return properties.forEnvironment(toAnchorEnvironment(apiEnvironment)).capabilities();
    }

    private void requireCapability(String capability, String apiEnvironment) {
        if (!supports(capability, apiEnvironment)) {
            throw new IllegalStateException("Anchor " + capability + " is unavailable in " + apiEnvironment.toUpperCase());
        }
    }

    private AnchorEnvironment toAnchorEnvironment(String apiEnvironment) {
        if ("TEST".equalsIgnoreCase(apiEnvironment)) return AnchorEnvironment.SANDBOX;
        if ("LIVE".equalsIgnoreCase(apiEnvironment)) return AnchorEnvironment.LIVE;
        throw new IllegalArgumentException("API environment must be TEST or LIVE");
    }

    private AnchorRelationship relationship(String id, String type) {
        return new AnchorRelationship(new AnchorResourceIdentifier(id, type));
    }

    private String virtualNubanId(SubAccountResource.Relationships relationships) {
        if (relationships == null) return null;
        AnchorToManyRelationship virtualNubans = relationships.virtualNubans();
        List<AnchorResourceIdentifier> resources = virtualNubans == null ? null : virtualNubans.data();
        return resources == null || resources.isEmpty() ? null : resources.getFirst().id();
    }

    private CreateReservedAccountData.FullName splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Reserved-account customer full name is required");
        }
        String[] names = fullName.trim().split("\\s+", 2);
        return new CreateReservedAccountData.FullName(names[0], names.length == 1 ? names[0] : names[1]);
    }

    private AccountDetails details(String resourceId, String accountName, String accountNumber, AnchorBank bank, String currency, String status) {
        return new AccountDetails(resourceId, accountName, accountNumber, mask(accountNumber),
                bank == null ? null : bank.name(), bank == null ? null : bank.nipCode(),
                currency == null || currency.isBlank() ? "NGN" : currency, status(status));
    }

    private String mask(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) return null;
        int visible = Math.min(4, accountNumber.length());
        return "*".repeat(accountNumber.length() - visible) + accountNumber.substring(accountNumber.length() - visible);
    }

    private String status(String status) {
        return status == null || status.isBlank() ? "PENDING" : status;
    }

    private <T> T requireData(T data, String message) {
        if (data == null) throw new IllegalStateException(message);
        return data;
    }
}
