package com.atlashub.pay.transfers.infrastructure.persistence.entities;

import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "payouts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PayoutJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "recipient_bank_code", nullable = false, length = 10)
    private String recipientBankCode;

    @Column(name = "recipient_account_number", nullable = false, length = 20)
    private String recipientAccountNumber;

    @Column(name = "recipient_account_name", length = 100)
    private String recipientAccountName;

    @Column(name = "narration", length = 255)
    private String narration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PayoutStatus status;

    @Column(name = "reference", nullable = false, unique = true, length = 100)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private PaymentProvider provider;

    @Column(name = "provider_reference", length = 100)
    private String providerReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_system", nullable = false, length = 30)
    private SourceSystem sourceSystem;

    @Column(name = "source_reference_id", length = 100)
    private String sourceReferenceId;

    @Column(name = "initiated_by", nullable = false)
    private Long initiatedBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private ZonedDateTime approvedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "completed_at")
    private ZonedDateTime completedAt;

    @Version
    private Long version;


}
