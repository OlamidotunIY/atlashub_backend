package com.atlashub.pay.mandates.infrastructure.persistence.entities;

import com.atlashub.pay.mandates.domain.valueobject.MandateFrequency;
import com.atlashub.pay.mandates.domain.valueobject.MandateStatus;
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
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "payment_mandates",
        indexes = {
                @Index(name = "idx_mandates_org_status", columnList = "organization_id, status"),
                @Index(name = "idx_mandates_org_customer", columnList = "organization_id, customer_id"),
                @Index(name = "idx_mandates_charge_due", columnList = "status, next_charge_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PaymentMandateJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 30)
    private MandateFrequency frequency;

    @Column(name = "authorization_code", nullable = false, length = 255)
    private String authorizationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MandateStatus status;

    @Column(name = "next_charge_date", nullable = false)
    private LocalDate nextChargeDate;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
