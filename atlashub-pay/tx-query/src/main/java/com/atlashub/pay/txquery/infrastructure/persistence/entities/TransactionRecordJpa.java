package com.atlashub.pay.txquery.infrastructure.persistence.entities;

import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Table(name = "pay_transaction_records",
        uniqueConstraints = @UniqueConstraint(name = "Uk_pay_tx_org_env_ref",
                columnNames = {"organization_id", "api_environment", "reference"}),
        indexes = {
                @Index(name = "Idx_pay_tx_org_env_time", columnList = "organization_id,api_environment,created_at"),
                @Index(name = "Idx_pay_tx_org_env_status", columnList = "organization_id,api_environment,status"),
                @Index(name = "Idx_pay_tx_party", columnList = "organization_id,api_environment,party_type,party_reference_id"),
                @Index(name = "Idx_pay_tx_source", columnList = "organization_id,api_environment,source_system,source_reference_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TransactionRecordJpa implements BaseJpaEntity {
    @Id private Long id;
    @Column(name = "organization_id", nullable = false) private Long organizationId;
    @Enumerated(EnumType.STRING) @Column(name = "api_environment", nullable = false) private ApiEnvironment environment;
    @Column(nullable = false) private String type;
    @Column(nullable = false) private String status;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal amount;
    @Column(precision = 19, scale = 4) private BigDecimal fee;
    @Column(name = "net_amount", precision = 19, scale = 4) private BigDecimal netAmount;
    @Column(nullable = false) private String currency;
    private String channel;
    private String provider;
    @Column(nullable = false) private String reference;
    @Column(name = "source_system") private String sourceSystem;
    @Column(name = "source_reference_id") private String sourceReferenceId;
    @Column(name = "party_type") private String partyType;
    @Column(name = "party_reference_id") private String partyReferenceId;
    @Column(name = "outlet_id") private Long outletId;
    @Column(name = "recipient_name") private String recipientName;
    @Column(name = "recipient_account_number") private String recipientAccountNumber;
    private String description;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "json") private Map<String, String> metadata;
    @Column(name = "created_at", nullable = false) private ZonedDateTime createdAt;
    @Column(name = "completed_at") private ZonedDateTime completedAt;
    @Column(name = "updated_at", nullable = false) private ZonedDateTime updatedAt;
    @Version private Long version;
}
