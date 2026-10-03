package com.atlashub.pay.accounts.infrastructure.persistence.entities;

import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest.RequestStatus;
import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest.RequestType;
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
@Table(name = "pay_banking_provider_requests", indexes = {
        @Index(name = "Idx_pay_provider_request_status", columnList = "status,created_at"),
        @Index(name = "Idx_pay_provider_request_reference_env", columnList = "request_reference,api_environment", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BankingProviderRequestJpa implements BaseJpaEntity {
    @Id private Long id;
    @Enumerated(EnumType.STRING) @Column(name = "request_type", nullable = false) private RequestType requestType;
    @Column(name = "aggregate_id", nullable = false) private Long aggregateId;
    @Column(name = "request_reference", nullable = false) private String requestReference;
    @Column(name = "api_environment", nullable = false) private String apiEnvironment;
    @Column(name = "anchor_customer_id") private String anchorCustomerId;
    @Column(name = "parent_or_payout_account_id") private String parentOrPayoutAccountId;
    private String provider;
    @Column(name = "customer_type") private String customerType;
    @Column(name = "customer_reference_id") private String customerReferenceId;
    @Column(name = "customer_full_name") private String customerFullName;
    @Column(name = "customer_email") private String customerEmail;
    @Column(name = "customer_bvn") private String customerBvn;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RequestStatus status;
    @Column(nullable = false) private int attempts;
    @Column(name = "failure_reason") private String failureReason;
    @Column(name = "created_at", nullable = false) private ZonedDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private ZonedDateTime updatedAt;
    @Version private Long version;
}
