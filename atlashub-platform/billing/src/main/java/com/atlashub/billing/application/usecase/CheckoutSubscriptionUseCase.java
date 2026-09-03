package com.atlashub.billing.application.usecase;

import com.atlashub.admin.domain.model.Admin;
import com.atlashub.admin.domain.repository.AdminRepository;
import com.atlashub.admin.domain.valueobject.AdminRole;
import com.atlashub.billing.application.command.CheckoutSubscriptionCommand;
import com.atlashub.billing.application.port.out.ProductPricingDto;
import com.atlashub.billing.application.port.out.ProductQueryPort;
import com.atlashub.billing.domain.model.BillingInvoice;
import com.atlashub.billing.domain.model.OrganizationProduct;
import com.atlashub.billing.domain.repository.BillingInvoiceRepository;
import com.atlashub.billing.domain.repository.OrganizationProductRepository;
import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import com.atlashub.billing.domain.valueobject.PaymentMethod;
import com.atlashub.billing.domain.valueobject.SubscriptionStatus;
import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.shared.application.api.OrganizationQueryApi;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.exception.SharedErrorCode;
import com.atlashub.shared.domain.money.CurrencyCode;
import com.atlashub.shared.domain.money.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class CheckoutSubscriptionUseCase extends BaseUseCase<CheckoutSubscriptionCommand, Void> {

    private final OrganizationProductRepository orgProductRepository;
    private final BillingInvoiceRepository invoiceRepository;
    private final ProductQueryPort productQueryPort;
    private final OrganizationQueryApi organizationQueryApi;
    private final DomainEventPublisher publisher;
    private final AdminRepository adminRepository;
    private final String frontendUrl;

    public CheckoutSubscriptionUseCase(
            OrganizationProductRepository orgProductRepository,
            BillingInvoiceRepository invoiceRepository,
            ProductQueryPort productQueryPort, OrganizationQueryApi organizationQueryApi,
            DomainEventPublisher publisher,
            AdminRepository adminRepository,
            @Value("${atlashub.frontend.url}") String frontendUrl) {
        this.orgProductRepository = orgProductRepository;
        this.invoiceRepository = invoiceRepository;
        this.productQueryPort = productQueryPort;
        this.organizationQueryApi = organizationQueryApi;
        this.publisher = publisher;
        this.adminRepository = adminRepository;
        this.frontendUrl = frontendUrl;
    }

    @Override
    @Transactional
    public Void execute(CheckoutSubscriptionCommand command) {

        OrganizationQueryApi.OrganizationSharedDto organization = organizationQueryApi.getOrganizationById(command.organizationId()).orElseThrow(() -> new NotFoundException(IdentityErrorCode.Organization_NOT_FOUND, "Organization not found"));

        ProductPricingDto pricing = productQueryPort.getProductPricing(command.productId(), organization.currency())
                .orElseThrow(() -> new NotFoundException(SharedErrorCode.INVALID_ID, "Product not found"));

        ZonedDateTime now = ZonedDateTime.now();
        BillingCycle cycle = BillingCycle.valueOf(pricing.billingCycle());
        ZonedDateTime periodEnd = now.plusMonths(1); // default logic

        OrganizationProduct orgProduct = new OrganizationProduct(
                orgProductRepository.nextIdentity(),
                command.organizationId(),
                command.productId(),
                SubscriptionStatus.PENDING,
                cycle,
                now,
                periodEnd,
                null
        );
        OrganizationProduct savedOrgProduct = orgProductRepository.save(orgProduct);

        BillingInvoice invoice = new BillingInvoice(
                invoiceRepository.nextIdentity(),
                savedOrgProduct.getId(),
                Money.of(pricing.priceAmount(), CurrencyCode.valueOf(pricing.currency())),
                InvoiceStatus.DRAFT,
                now.plusDays(7),
                null
        );

        if (command.paymentMethod() == PaymentMethod.WALLET) {
            invoice.requestWalletCharge(command.organizationId());
        } else if (command.paymentMethod() == PaymentMethod.CHARGE) {
            Admin masterAdmin = adminRepository.findByRole(AdminRole.MASTER)
                    .orElseThrow(() -> new NotFoundException(SharedErrorCode.INVALID_ID, "Master admin not found"));
            String redirectUrl = frontendUrl + "/checkout/success";
            invoice.requestExternalCharge(command.organizationId(), masterAdmin.getEmail().value(), redirectUrl);
        } else {
            throw new BusinessRuleException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Invalid payment method");
        }

        BillingInvoice savedInvoice = invoiceRepository.save(invoice);

        publishEvents(savedOrgProduct, publisher);
        publishEvents(savedInvoice, publisher);

        return null;
    }
}