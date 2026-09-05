package com.atlashub.billing.application.usecase;

import com.atlashub.billing.application.command.CheckoutSubscriptionCommand;
import com.atlashub.catalog.application.port.HubProductQueryService;
import com.atlashub.catalog.application.result.HubProductDetailsResult;
import com.atlashub.catalog.application.result.PricingResult;
import com.atlashub.identity.application.port.OrganizationQueryService;
import com.atlashub.identity.application.port.OrganizationQueryService.OrganizationSharedDto;
import com.atlashub.billing.domain.model.BillingInvoice;
import com.atlashub.billing.domain.repository.BillingInvoiceRepository;
import com.atlashub.billing.domain.valueobject.PaymentMethod;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.domain.event.DomainEventPublisher;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.exception.SharedErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutSubscriptionUseCase extends BaseUseCase<CheckoutSubscriptionCommand, Void> {

    private final OrganizationQueryService organizationQueryService;
    private final HubProductQueryService productQueryService;
    private final BillingInvoiceRepository invoiceRepository;
    private final DomainEventPublisher publisher;

    public CheckoutSubscriptionUseCase(OrganizationQueryService organizationQueryService, HubProductQueryService productQueryService,
                                       BillingInvoiceRepository invoiceRepository, DomainEventPublisher publisher) {
        this.organizationQueryService = organizationQueryService;
        this.productQueryService = productQueryService;
        this.invoiceRepository = invoiceRepository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Void execute(CheckoutSubscriptionCommand command) {
        OrganizationSharedDto organizationDto = organizationQueryService.getOrganizationById(Long.valueOf(command.organizationId()))
                .orElseThrow(() -> new BusinessRuleException(SharedErrorCode.NOT_FOUND, "Organization not found"));

        HubProductDetailsResult product = productQueryService.getHubProductDetails(command.productId());
        if (product == null) {
            throw new BusinessRuleException(SharedErrorCode.NOT_FOUND, "Product pricing not found");
        }

        PricingResult tier = product.pricing().get(0);

        Long invoiceId = invoiceRepository.nextIdentity();
        BillingInvoice invoice = BillingInvoice.initiate(
                invoiceId,
                Long.valueOf(command.organizationId()),
                organizationDto.currency(),
                tier.basePrice(),
                "Subscription for " + product.code(),
                command.paymentMethod()
        );

        invoiceRepository.save(invoice);
        publishEvents(invoice, publisher);
        return null;
    }
}

