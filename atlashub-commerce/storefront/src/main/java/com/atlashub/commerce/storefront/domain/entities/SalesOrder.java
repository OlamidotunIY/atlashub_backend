package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.events.LayawayCreatedEvent;
import com.atlashub.commerce.storefront.domain.events.OnlineOrderCreatedEvent;
import com.atlashub.commerce.storefront.domain.events.PosSaleCompletedEvent;
import com.atlashub.commerce.storefront.domain.events.PosSaleFailedEvent;
import com.atlashub.commerce.storefront.domain.events.PosSaleRefundedEvent;
import com.atlashub.commerce.storefront.domain.events.SalesOrderCreatedEvent;
import com.atlashub.commerce.storefront.domain.events.SalesOrderPaymentInitiatedEvent;
import com.atlashub.commerce.storefront.domain.exceptions.InvalidOrderStateException;
import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class SalesOrder extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long outletId;
    private final Long vendorId;
    private final Long customerId;
    private final Long cashierId;
    private final Long tillId;
    private final OrderType type;
    private OrderStatus status;
    private final List<SalesOrderItem> items;
    private Long discountId;
    private Money totalGross;
    private Money totalDiscount;
    private Money totalTax;
    private Money totalNet;
    private final PaymentMethod paymentMethod;
    private String chargeReference;
    private final ZonedDateTime saleDate;
    private final String deliveryAddress;

    public SalesOrder(
            Long id,
            Long organizationId,
            Long outletId,
            Long vendorId,
            Long customerId,
            Long cashierId,
            Long tillId,
            OrderType type,
            OrderStatus status,
            List<SalesOrderItem> items,
            Long discountId,
            Money totalGross,
            Money totalDiscount,
            Money totalTax,
            Money totalNet,
            PaymentMethod paymentMethod,
            String chargeReference,
            ZonedDateTime saleDate,
            String deliveryAddress
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.outletId = outletId;
        this.vendorId = vendorId;
        this.customerId = customerId;
        this.cashierId = cashierId;
        this.tillId = tillId;
        this.type = type;
        this.status = status;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.discountId = discountId;
        this.totalGross = totalGross;
        this.totalDiscount = totalDiscount;
        this.totalTax = totalTax;
        this.totalNet = totalNet;
        this.paymentMethod = paymentMethod;
        this.chargeReference = chargeReference;
        this.saleDate = saleDate;
        this.deliveryAddress = deliveryAddress;
    }

    public static SalesOrder create(
            Long id,
            Long organizationId,
            Long outletId,
            Long vendorId,
            Long customerId,
            Long cashierId,
            Long tillId,
            OrderType type,
            PaymentMethod paymentMethod,
            List<SalesOrderItem> initialItems,
            String deliveryAddress
    ) {
        Objects.requireNonNull(id, "Sales order ID must not be null");
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(outletId, "Outlet ID must not be null");
        Objects.requireNonNull(type, "Order type must not be null");
        Objects.requireNonNull(paymentMethod, "Payment method must not be null");

        CurrencyCode currency = (initialItems != null && !initialItems.isEmpty())
                ? initialItems.getFirst().getUnitPrice().currency()
                : CurrencyCode.NGN;

        SalesOrder order = new SalesOrder(
                id,
                organizationId,
                outletId,
                vendorId,
                customerId,
                cashierId,
                tillId,
                type,
                OrderStatus.PENDING,
                new ArrayList<>(),
                null,
                Money.zero(currency),
                Money.zero(currency),
                Money.zero(currency),
                Money.zero(currency),
                paymentMethod,
                null,
                ZonedDateTime.now(),
                deliveryAddress
        );

        if (initialItems != null) {
            for (SalesOrderItem item : initialItems) {
                order.addItem(item);
            }
        }

        if (!order.items.isEmpty()) {
            List<SalesOrderCreatedEvent.OrderItemPayload> eventItems = order.items.stream()
                    .map(i -> new SalesOrderCreatedEvent.OrderItemPayload(i.getProductId(), i.getVariantId(), i.getQuantity()))
                    .toList();
            order.registerEvent(SalesOrderCreatedEvent.of(order.id, order.organizationId, order.outletId, eventItems));
        }

        if (order.type == OrderType.ONLINE) {
            order.registerEvent(OnlineOrderCreatedEvent.of(
                    order.id,
                    order.organizationId,
                    order.customerId,
                    order.totalNet.amount(),
                    order.totalNet.currency().name(),
                    order.deliveryAddress
            ));
        }

        return order;
    }

    public void addItem(SalesOrderItem item) {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Cannot add items to an order in status: " + status);
        }
        Objects.requireNonNull(item, "Item must not be null");
        this.items.add(item);
        recalculateTotals();
    }

    public void applyDiscount(Long discountId, Money discountAmount) {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Cannot apply discount to an order in status: " + status);
        }
        this.discountId = discountId;
        this.totalDiscount = discountAmount != null ? discountAmount : Money.zero(totalGross.currency());
        recalculateTotals();
    }

    public void initiatePayment(String chargeReference) {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Cannot initiate payment for order in status: " + status);
        }
        this.chargeReference = chargeReference;
        this.status = OrderStatus.PAYMENT_PENDING;

        registerEvent(SalesOrderPaymentInitiatedEvent.of(
                id,
                organizationId,
                outletId,
                chargeReference,
                totalNet.amount(),
                totalNet.currency().name(),
                paymentMethod.name(),
                cashierId
        ));
    }

    public void completePayment() {
        if (status != OrderStatus.PAYMENT_PENDING && !(status == OrderStatus.PENDING && paymentMethod == PaymentMethod.CASH)) {
            throw new InvalidOrderStateException("Cannot complete payment for order in status: " + status);
        }
        this.status = OrderStatus.COMPLETED;

        registerEvent(PosSaleCompletedEvent.of(
                id,
                organizationId,
                outletId,
                totalNet.amount(),
                totalNet.currency().name(),
                paymentMethod.name(),
                cashierId
        ));
    }

    public void failPayment(String reason) {
        if (status != OrderStatus.PAYMENT_PENDING && status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Cannot fail payment for order in status: " + status);
        }
        this.status = OrderStatus.FAILED;

        registerEvent(PosSaleFailedEvent.of(id, organizationId, reason));
    }

    public void refund(String reason, String customerNuban, String customerBankCode) {
        if (status != OrderStatus.COMPLETED) {
            throw new InvalidOrderStateException("Cannot refund order in status: " + status);
        }
        this.status = OrderStatus.REFUNDED;

        registerEvent(PosSaleRefundedEvent.of(
                id,
                organizationId,
                chargeReference,
                totalNet.amount(),
                totalNet.currency().name(),
                reason,
                customerNuban,
                customerBankCode
        ));
    }

    public void convertToLayaway(Long depositId, Money depositAmount) {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Cannot convert order to layaway in status: " + status);
        }
        this.status = OrderStatus.LAYAWAY;
        Money balanceRemaining = totalNet.subtract(depositAmount);

        registerEvent(LayawayCreatedEvent.of(
                id,
                organizationId,
                depositId,
                depositAmount.amount(),
                balanceRemaining.amount(),
                totalNet.currency().name()
        ));
    }

    private void recalculateTotals() {
        CurrencyCode currency = items.isEmpty() ? CurrencyCode.NGN : items.getFirst().getUnitPrice().currency();
        Money gross = Money.zero(currency);
        Money itemDiscount = Money.zero(currency);
        Money tax = Money.zero(currency);

        for (SalesOrderItem item : items) {
            gross = gross.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            tax = tax.add(item.getTaxAmount());
            if (item.getDiscountAmount() != null) {
                itemDiscount = itemDiscount.add(item.getDiscountAmount());
            }
        }

        Money orderDiscount = this.totalDiscount != null ? this.totalDiscount : Money.zero(currency);
        Money effectiveDiscount = itemDiscount.add(orderDiscount);

        this.totalGross = gross;
        this.totalDiscount = effectiveDiscount;
        this.totalTax = tax;
        this.totalNet = gross.add(tax).subtract(effectiveDiscount);
    }

    @Override
    public Long getId() {
        return id;
    }

    public List<SalesOrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
