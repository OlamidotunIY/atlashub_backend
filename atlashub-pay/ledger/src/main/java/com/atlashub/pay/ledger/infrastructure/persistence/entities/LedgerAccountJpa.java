package com.atlashub.pay.ledger.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;
import com.atlashub.pay.ledger.domain.valueobject.LedgerRestrictionType;
import com.atlashub.shared.application.security.ApiEnvironment;

@Entity
@Table(
        name = "ledger_accounts",
        indexes = {
                @Index(name = "Idx_ledger_account_org_id", columnList = "organization_id"),
                @Index(name = "Idx_ledger_account_org_env", columnList = "organization_id,api_environment"),
                @Index(name = "Idx_ledger_account_outlet_id", columnList = "outlet_id")
                ,@Index(name = "Idx_ledger_account_party", columnList = "organization_id,party_type,party_reference_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LedgerAccountJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "api_environment", nullable = false)
    private ApiEnvironment environment;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "outlet_id")
    private Long outletId;

    @Column(name = "party_type")
    private String partyType;

    @Column(name = "party_reference_id")
    private String partyReferenceId;

    @Column(nullable = false)
    private String currency;

    @Column(name = "normal_balance", nullable = false)
    private String normalBalance;

    @Column(nullable = false)
    private String status;

    @ElementCollection
    @CollectionTable(name = "ledger_account_restrictions", joinColumns = @JoinColumn(name = "account_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_type", nullable = false)
    private Set<LedgerRestrictionType> activeRestrictions;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
