package com.atlashub.accounting.ap.infrastructure.messaging.listeners;

import com.atlashub.accounting.ap.application.command.RecordSupplierInvoice.RecordSupplierInvoiceCommand;
import com.atlashub.accounting.ap.application.command.RecordSupplierInvoice.RecordSupplierInvoiceHandler;
import com.atlashub.accounting.ap.infrastructure.messaging.events.ApInboundEvents.PurchaseOrderReceivedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.TimeoutException;

@Component
public class PurchaseOrderReceivedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(PurchaseOrderReceivedListener.class);
    private static final String GROUP_ID = "accounting-ap-po-received";

    private final RecordSupplierInvoiceHandler handler;

    public PurchaseOrderReceivedListener(ObjectMapper objectMapper, RecordSupplierInvoiceHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("PurchaseOrderReceivedEvent", GROUP_ID);
        registerSubscription("com.atlashub.commerce.inventory.domain.events.PurchaseOrderReceivedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void onPurchaseOrderReceived(String message) {
        processEventIfMatches(message, "PurchaseOrderReceivedEvent", PurchaseOrderReceivedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> {
                    var payload = event.payload();
                    if (payload != null && payload.amount() != null && payload.amount().signum() > 0) {
                        CurrencyCode currency = (payload.currency() != null && !payload.currency().isBlank())
                                ? CurrencyCode.valueOf(payload.currency())
                                : CurrencyCode.NGN;
                        Money amount = Money.of(payload.amount(), currency);
                        LocalDate dueDate = payload.dueDate() != null ? payload.dueDate() : LocalDate.now().plusDays(30);

                        handler.execute(new RecordSupplierInvoiceCommand(
                                payload.organizationId(),
                                payload.supplierId(),
                                payload.purchaseOrderId(),
                                payload.invoiceNumber(),
                                amount,
                                dueDate
                        ));
                    }
                });
    }
}
