package com.atlashub.commerce.inventory.infrastructure.persistence.entities;

import com.atlashub.commerce.inventory.domain.valueobject.RefundMethod;
import com.atlashub.commerce.inventory.domain.valueobject.ReturnStatus;
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
@Table(name = "commerce_customer_returns", indexes = {@Index(name = "Idx_comm_cr_org_sales", columnList = "organization_id, sales_order_id"), @Index(name = "Idx_comm_cr_org_outlet", columnList = "organization_id, outlet_id, status")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CustomerReturnJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "outlet_id", nullable = false)
    private Long outletId;

    @Column(name = "sales_order_id", nullable = false)
    private Long salesOrderId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "refund_amount", nullable = false)
    private BigDecimal refundAmount;

    @Column(name = "reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_method", nullable = false)
    private RefundMethod refundMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReturnStatus status;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Version
    private Long version;
}
