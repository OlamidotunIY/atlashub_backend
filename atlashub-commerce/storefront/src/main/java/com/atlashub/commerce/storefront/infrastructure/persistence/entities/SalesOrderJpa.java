package com.atlashub.commerce.storefront.infrastructure.persistence.entities;

import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "commerce_sales_orders",
        indexes = {
                @Index(name = "Idx_comm_so_org_outlet", columnList = "organization_id, outlet_id"),
                @Index(name = "Idx_comm_so_charge_ref", columnList = "charge_reference"),
                @Index(name = "Idx_comm_so_status_date", columnList = "status, sale_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SalesOrderJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "cashier_id")
    private Long cashierId;

    @Column(name = "till_id")
    private Long tillId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OrderType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "discount_id")
    private Long discountId;

    @Column(name = "total_gross", nullable = false)
    private BigDecimal totalGross;

    @Column(name = "total_discount", nullable = false)
    private BigDecimal totalDiscount;

    @Column(name = "total_tax", nullable = false)
    private BigDecimal totalTax;

    @Column(name = "total_net", nullable = false)
    private BigDecimal totalNet;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "charge_reference")
    private String chargeReference;

    @Column(name = "sale_date", nullable = false)
    private ZonedDateTime saleDate;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Version
    private Long version;
}
