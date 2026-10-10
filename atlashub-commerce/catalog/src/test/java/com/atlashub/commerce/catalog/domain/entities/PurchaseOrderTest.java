package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.events.PurchaseOrderReceivedEvent;
import com.atlashub.commerce.catalog.domain.events.PurchaseOrderSentEvent;
import com.atlashub.commerce.catalog.domain.exceptions.InvalidPurchaseOrderStateException;
import com.atlashub.commerce.catalog.domain.valueobject.PurchaseOrderStatus;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchaseOrderTest {

    @Test
    @DisplayName("create_draftPO_addsItemsAndRecalculatesTotal")
    void create_draftPO_addsItemsAndRecalculatesTotal() {
        PurchaseOrder po = PurchaseOrder.create(1L, 10L, 100L, 200L,
                LocalDate.now().plusDays(7), CurrencyCode.NGN);

        assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.DRAFT);
        assertThat(po.getTotalAmount().amount()).isEqualByComparingTo(BigDecimal.ZERO);

        po.addItem(11L, 1001L, 5, new Money(BigDecimal.valueOf(2000), CurrencyCode.NGN));
        po.addItem(12L, 1002L, 2, new Money(BigDecimal.valueOf(5000), CurrencyCode.NGN));

        assertThat(po.getItems()).hasSize(2);
        // (5 * 2000) + (2 * 5000) = 10000 + 10000 = 20000
        assertThat(po.getTotalAmount().amount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
    }

    @Test
    @DisplayName("send_whenDraftWithItems_transitionsToSentAndEmitsEvent")
    void send_whenDraftWithItems_transitionsToSentAndEmitsEvent() {
        PurchaseOrder po = PurchaseOrder.create(2L, 10L, 100L, 200L,
                LocalDate.now().plusDays(7), CurrencyCode.NGN);
        po.addItem(21L, 1001L, 3, new Money(BigDecimal.valueOf(1000), CurrencyCode.NGN));

        po.send();

        assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.SENT);
        List<DomainEvent<?>> events = po.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(PurchaseOrderSentEvent.class);
        PurchaseOrderSentEvent sentEvent = (PurchaseOrderSentEvent) events.get(0);
        assertThat(sentEvent.aggregateId()).isEqualTo(2L);
        assertThat(sentEvent.payload().totalAmount().amount()).isEqualByComparingTo(BigDecimal.valueOf(3000));
    }

    @Test
    @DisplayName("send_whenNoItems_throwsInvalidPurchaseOrderStateException")
    void send_whenNoItems_throwsInvalidPurchaseOrderStateException() {
        PurchaseOrder po = PurchaseOrder.create(3L, 10L, 100L, 200L,
                LocalDate.now().plusDays(7), CurrencyCode.NGN);

        assertThatThrownBy(po::send)
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("Cannot send a purchase order with no items");
    }

    @Test
    @DisplayName("receiveItems_partialThenFull_transitionsToReceivedAndEmitsEvent")
    void receiveItems_partialThenFull_transitionsToReceivedAndEmitsEvent() {
        PurchaseOrder po = PurchaseOrder.create(4L, 10L, 100L, 200L,
                LocalDate.now().plusDays(7), CurrencyCode.NGN);
        po.addItem(41L, 1001L, 10, new Money(BigDecimal.valueOf(500), CurrencyCode.NGN));
        po.send();
        po.pullDomainEvents();

        // Partial receipt
        po.receiveItems(Map.of(1001L, 4));
        assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        assertThat(po.getItems().get(0).getQuantityReceived()).isEqualTo(4);
        assertThat(po.pullDomainEvents()).isEmpty();

        // Remaining receipt
        po.receiveItems(Map.of(1001L, 6));
        assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);
        assertThat(po.getItems().get(0).getQuantityReceived()).isEqualTo(10);
        List<DomainEvent<?>> events = po.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(PurchaseOrderReceivedEvent.class);
    }

    @Test
    @DisplayName("cancel_whenDraftOrSent_transitionsToCancelled")
    void cancel_whenDraftOrSent_transitionsToCancelled() {
        PurchaseOrder po = PurchaseOrder.create(5L, 10L, 100L, 200L,
                LocalDate.now().plusDays(7), CurrencyCode.NGN);
        po.cancel();
        assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);

        PurchaseOrder sentPo = PurchaseOrder.create(6L, 10L, 100L, 200L,
                LocalDate.now().plusDays(7), CurrencyCode.NGN);
        sentPo.addItem(61L, 1001L, 1, new Money(BigDecimal.valueOf(100), CurrencyCode.NGN));
        sentPo.send();
        sentPo.cancel();
        assertThat(sentPo.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
    }
}
