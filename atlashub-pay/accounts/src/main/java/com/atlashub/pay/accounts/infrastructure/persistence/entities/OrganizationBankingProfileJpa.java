package com.atlashub.pay.accounts.infrastructure.persistence.entities;

import com.atlashub.pay.accounts.domain.valueobject.BankingProfileStatus;
import com.atlashub.pay.accounts.domain.valueobject.BankingRestrictionType;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
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
import java.util.Set;

@Entity
@Table(name = "pay_organization_banking_profiles", indexes = {
        @Index(name = "Idx_pay_banking_profile_org", columnList = "organization_id", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrganizationBankingProfileJpa implements BaseJpaEntity {
    @Id private Long id;
    @Column(name = "organization_id", nullable = false, unique = true) private Long organizationId;
    @Column(name = "anchor_business_customer_id", nullable = false) private String anchorBusinessCustomerId;
    @Column(name = "business_deposit_account_id") private Long businessDepositAccountId;
    @Column(name = "business_sub_account_id") private Long businessSubAccountId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private BankingProfileStatus status;
    @ElementCollection
    @CollectionTable(name = "pay_banking_profile_restrictions", joinColumns = @JoinColumn(name = "profile_id"))
    @Enumerated(EnumType.STRING) @Column(name = "restriction", nullable = false)
    private Set<BankingRestrictionType> activeRestrictions;
    @Column(name = "failure_code") private String failureCode;
    @Column(name = "failure_message") private String failureMessage;
    @Column(name = "created_at", nullable = false) private ZonedDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private ZonedDateTime updatedAt;
    @Version private Long version;
}
