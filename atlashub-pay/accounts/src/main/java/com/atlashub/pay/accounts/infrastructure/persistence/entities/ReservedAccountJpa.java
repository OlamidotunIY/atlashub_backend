package com.atlashub.pay.accounts.infrastructure.persistence.entities;

import com.atlashub.pay.accounts.domain.valueobject.BankingRestrictionType;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.pay.accounts.infrastructure.persistence.adapters.BankAccountNumberEncryptionConverter;
import java.util.Set;

@Entity
@Table(name = "pay_reserved_accounts", indexes = {
        @Index(name = "Idx_pay_reserved_org_env", columnList = "organization_id,api_environment"),
        @Index(name = "Idx_pay_reserved_owner_env", columnList = "organization_id,api_environment,owner_type,owner_reference_id"),
        @Index(name = "Idx_pay_reserved_request_env", columnList = "request_reference,api_environment", unique = true),
        @Index(name = "Idx_pay_reserved_anchor_env", columnList = "anchor_reserved_account_id,api_environment", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReservedAccountJpa implements BaseJpaEntity {
    @Id private Long id;
    @Column(name = "organization_id", nullable = false) private Long organizationId;
    @Enumerated(EnumType.STRING) @Column(name = "api_environment", nullable = false) private ApiEnvironment environment;
    @Enumerated(EnumType.STRING) @Column(name = "owner_type", nullable = false) private ReservedAccountOwnerType ownerType;
    @Column(name = "owner_reference_id", nullable = false) private String ownerReferenceId;
    @Column(name = "business_sub_account_id", nullable = false) private Long businessSubAccountId;
    @Column(name = "anchor_payout_sub_account_id", nullable = false) private String anchorPayoutSubAccountId;
    @Column(nullable = false) private String provider;
    @Column(name = "request_reference", nullable = false) private String requestReference;
    @Column(name = "anchor_reserved_account_id") private String anchorReservedAccountId;
    @Column(name = "anchor_customer_id") private String anchorCustomerId;
    @Column(name = "account_name") private String accountName;
    @Convert(converter = BankAccountNumberEncryptionConverter.class)
    @Column(name = "account_number", length = 512) private String accountNumber;
    @Column(name = "masked_account_number") private String maskedAccountNumber;
    @Column(name = "bank_name") private String bankName;
    @Column(name = "bank_code") private String bankCode;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private CurrencyCode currency;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ExternalAccountStatus status;
    @ElementCollection
    @CollectionTable(name = "pay_reserved_account_restrictions", joinColumns = @JoinColumn(name = "reserved_account_id"))
    @Enumerated(EnumType.STRING) @Column(name = "restriction", nullable = false)
    private Set<BankingRestrictionType> activeRestrictions;
    @Column(name = "failure_reason") private String failureReason;
    @Column(name = "created_at", nullable = false) private ZonedDateTime createdAt;
    @Column(name = "activated_at") private ZonedDateTime activatedAt;
    @Column(name = "updated_at", nullable = false) private ZonedDateTime updatedAt;
    @Version private Long version;
}
