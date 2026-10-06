package com.atlashub.pay.accounts.infrastructure.persistence.entities;

import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.pay.accounts.infrastructure.persistence.adapters.BankAccountNumberEncryptionConverter;

@Entity
@Table(name = "pay_business_sub_accounts", indexes = {
        @Index(name = "Idx_pay_sub_org_env", columnList = "organization_id,api_environment", unique = true),
        @Index(name = "Idx_pay_sub_anchor_env", columnList = "anchor_sub_account_id,api_environment", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BusinessSubAccountJpa implements BaseJpaEntity {
    @Id private Long id;
    @Column(name = "organization_id", nullable = false) private Long organizationId;
    @Enumerated(EnumType.STRING) @Column(name = "api_environment", nullable = false) private ApiEnvironment environment;
    @Column(name = "banking_profile_id", nullable = false) private Long bankingProfileId;
    @Column(name = "anchor_business_customer_id", nullable = false) private String anchorBusinessCustomerId;
    @Column(name = "anchor_parent_fbo_account_id", nullable = false) private String anchorParentFboAccountId;
    @Column(name = "anchor_sub_account_id") private String anchorSubAccountId;
    @Column(name = "anchor_virtual_nuban_id") private String anchorVirtualNubanId;
    @Column(name = "account_name") private String accountName;
    @Convert(converter = BankAccountNumberEncryptionConverter.class)
    @Column(name = "account_number", length = 512) private String accountNumber;
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
