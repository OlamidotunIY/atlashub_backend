package com.atlashub.pay.accounts.infrastructure.persistence.entities;

import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
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

import java.time.ZonedDateTime;

@Entity
@Table(name = "pay_business_sub_accounts", indexes = {
        @Index(name = "Idx_pay_sub_org", columnList = "organization_id"),
        @Index(name = "Idx_pay_sub_anchor", columnList = "anchor_sub_account_id", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BusinessSubAccountJpa implements BaseJpaEntity {
    @Id private Long id;
    @Column(name = "organization_id", nullable = false) private Long organizationId;
    @Column(name = "banking_profile_id", nullable = false) private Long bankingProfileId;
    @Column(name = "anchor_business_customer_id", nullable = false) private String anchorBusinessCustomerId;
    @Column(name = "anchor_parent_fbo_account_id", nullable = false) private String anchorParentFboAccountId;
    @Column(name = "anchor_sub_account_id", unique = true) private String anchorSubAccountId;
    @Column(name = "anchor_virtual_nuban_id") private String anchorVirtualNubanId;
    @Column(name = "account_name") private String accountName;
    @Column(name = "masked_account_number") private String maskedAccountNumber;
    @Column(name = "bank_name") private String bankName;
    @Column(name = "bank_code") private String bankCode;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private CurrencyCode currency;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ExternalAccountStatus status;
    @Column(name = "failure_reason") private String failureReason;
    @Column(name = "created_at", nullable = false) private ZonedDateTime createdAt;
    @Column(name = "activated_at") private ZonedDateTime activatedAt;
    @Column(name = "updated_at", nullable = false) private ZonedDateTime updatedAt;
    @Version private Long version;
}
