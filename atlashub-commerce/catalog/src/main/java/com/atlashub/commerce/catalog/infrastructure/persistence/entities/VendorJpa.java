package com.atlashub.commerce.catalog.infrastructure.persistence.entities;

import com.atlashub.commerce.catalog.domain.valueobject.DisbursementSchedule;
import com.atlashub.commerce.catalog.domain.valueobject.VendorStatus;
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
        name = "commerce_vendors",
        indexes = {
                @Index(name = "Idx_comm_vendor_org_id", columnList = "organization_id"),
                @Index(name = "Idx_comm_vendor_org_user", columnList = "organization_id, user_id", unique = true),
                @Index(name = "Idx_comm_vendor_org_status", columnList = "organization_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VendorJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "settlement_bank_code")
    private String settlementBankCode;

    @Column(name = "settlement_account_number")
    private String settlementAccountNumber;

    @Column(name = "settlement_account_name")
    private String settlementAccountName;

    @Column(name = "commission_rate")
    private BigDecimal commissionRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "disbursement_schedule")
    private DisbursementSchedule disbursementSchedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VendorStatus status;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
