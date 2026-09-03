package com.atlashub.billing.adapter.out.persistence.entity;

import com.atlashub.billing.domain.valueobject.SubscriptionStatus;
import com.atlashub.catalog.domain.valueobject.BillingCycle;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "organization_products", indexes = {
    @Index(name = "idx_org_product_org_id", columnList = "organization_id")
})
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationProductJpaEntity {
    @Id
    @Column(nullable = false)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycle billingCycle;

    @Column(name = "current_period_start", nullable = false)
    private ZonedDateTime currentPeriodStart;

    @Setter
    @Column(name = "current_period_end", nullable = false)
    private ZonedDateTime currentPeriodEnd;

    @Setter
    @Column(name = "canceled_at")
    private ZonedDateTime canceledAt;

    @Version
    private Long version;
}
