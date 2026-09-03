package com.atlashub.billing.application.usecase;

import com.atlashub.billing.application.command.CheckoutSubscriptionCommand;
import com.atlashub.billing.application.port.out.ProductPricingDto;
import com.atlashub.billing.application.port.out.ProductQueryPort;
import com.atlashub.billing.domain.events.ExternalChargeRequestedEvent;
import com.atlashub.billing.domain.events.WalletChargeRequestedEvent;
import com.atlashub.billing.domain.model.BillingInvoice;
import com.atlashub.billing.domain.model.OrganizationProduct;
import com.atlashub.billing.domain.repository.BillingInvoiceRepository;
import com.atlashub.billing.domain.repository.OrganizationProductRepository;
import com.atlashub.billing.domain.valueobject.InvoiceStatus;
import com.atlashub.billing.domain.valueobject.SubscriptionStatus;
import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.exception.SharedErrorCode;
import com.atlashub.shared.domain.money.CurrencyCode;
import com.atlashub.shared.domain.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class CheckoutSubscriptionUseCase extends BaseUseCase<CheckoutSubscriptionCommand, Void> {

    private final OrganizationProductRepository orgProductRepository;
    private final BillingInvoiceRepository invoiceRepository;
    private final ProductQueryPort productQueryPort;
    private final DomainEventPublisher publisher;
    private final com.atlashub.shared.adapter.out.external.DomainSequenceGenerator idGenerator;

    public CheckoutSubscriptionUseCase(
            OrganizationProductRepository orgProductRepository,
            BillingInvoiceRepository invoiceRepository,
            ProductQueryPort productQueryPort,
            DomainEventPublisher publisher,
            com.atlashub.shared.adapter.out.external.DomainSequenceGenerator idGenerator) {
        this.orgProductRepository = orgProductRepository;
        this.invoiceRepository = invoiceRepository;
        this.productQueryPort = productQueryPort;
        this.publisher = publisher;
        this.idGenerator = idGenerator;
    }

    @Override
    @Transactional
    public Void execute(CheckoutSubscriptionCommand command) {
        ProductPricingDto pricing = productQueryPort.getProductPricing(command.productId())
                .orElseThrow(() -> new NotFoundException(SharedErrorCode.INVALID_ID, "Product not found"));

        ZonedDateTime now = ZonedDateTime.now();
        BillingCycle cycle = BillingCycle.valueOf(pricing.billingCycle());
        ZonedDateTime periodEnd = now.plusMonths(1); // default logic

        OrganizationProduct orgProduct = new OrganizationProduct(
                idGenerator.nextIdentity("org_product_seq"),
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
                idGenerator.nextIdentity("billing_invoice_seq"),
                savedOrgProduct.getId(),
                Money.of(pricing.priceAmount(), CurrencyCode.valueOf(pricing.currency())),
                InvoiceStatus.DRAFT,
                now.plusDays(7),
                null
        );
        BillingInvoice savedInvoice = invoiceRepository.save(invoice);

        publishEvents(savedOrgProduct, publisher);
        publishEvents(savedInvoice, publisher);

        String eventId = UUID.randomUUID().toString();
        ZonedDateTime occurredAt = ZonedDateTime.now();

        if ("WALLET".equalsIgnoreCase(command.paymentMethod())) {
            WalletChargeRequestedEvent event = new WalletChargeRequestedEvent(
                    eventId,
                    String.valueOf(savedInvoice.getId()),
                    occurredAt,
                    new WalletChargeRequestedEvent.Payload(
                            savedInvoice.getId(),
                            command.organizationId(),
                            pricing.priceAmount(),
                            pricing.currency()
                    )
            );
            publisher.publish(EnvelopedDomainEvent.wrap(event));
        } else if ("CHARGE".equalsIgnoreCase(command.paymentMethod())) {
            ExternalChargeRequestedEvent event = new ExternalChargeRequestedEvent(
                    eventId,
                    String.valueOf(savedInvoice.getId()),
                    occurredAt,
                    new ExternalChargeRequestedEvent.Payload(
                            savedInvoice.getId(),
                            command.organizationId(),
                            pricing.priceAmount(),
                            pricing.currency(),
                            "admin@organization.com", // In a real app, query org admin email
                            "https://frontend.atlashub.com/checkout/success"
                    )
            );
            publisher.publish(EnvelopedDomainEvent.wrap(event));
        } else {
            throw new BusinessRuleException(SharedErrorCode.MISSING_REQUIRED_FIELD, "Invalid payment method");
        }

        return null;
    }
}