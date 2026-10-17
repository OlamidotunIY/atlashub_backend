package com.atlashub.analytics.infrastructure.persistence.entities;

import com.atlashub.analytics.domain.valueobject.DataQualityStatus;
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
@Table(
        name = "analytics_feature_snapshots",
        indexes = {
                @Index(name = "idx_feature_snapshot_unique", columnList = "organization_id, environment, schema_version, as_of", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BusinessFeatureSnapshotJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "environment", nullable = false)
    private String environment;

    @Column(name = "schema_version", nullable = false)
    private String schemaVersion;

    @Column(name = "window_start")
    private ZonedDateTime windowStart;

    @Column(name = "window_end")
    private ZonedDateTime windowEnd;

    @Column(name = "as_of", nullable = false)
    private ZonedDateTime asOf;

    @Enumerated(EnumType.STRING)
    @Column(name = "completeness", nullable = false)
    private DataQualityStatus completeness;

    @Column(name = "metric_values", columnDefinition = "json")
    private String metricValues;

    @Column(name = "source_projection_hashes", columnDefinition = "json")
    private String sourceProjectionHashes;

    @Column(name = "generated_at", nullable = false)
    private ZonedDateTime generatedAt;

    @Column(name = "expires_at")
    private ZonedDateTime expiresAt;

    @Version
    private Long version;
}
