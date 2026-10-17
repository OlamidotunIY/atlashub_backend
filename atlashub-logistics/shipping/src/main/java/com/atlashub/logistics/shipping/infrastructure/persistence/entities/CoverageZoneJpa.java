package com.atlashub.logistics.shipping.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "logistics_coverage_zones",
        indexes = {
                @Index(name = "idx_zone_code", columnList = "zone_code", unique = true),
                @Index(name = "idx_zone_state_city", columnList = "state, city")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CoverageZoneJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "state", nullable = false, length = 64)
    private String state;

    @Column(name = "city", nullable = false, length = 64)
    private String city;

    @Column(name = "zone_code", nullable = false, unique = true, length = 32)
    private String zoneCode;

    @Column(name = "internal_fleet_covered", nullable = false)
    private boolean internalFleetCovered;

    @Column(name = "partner_3pl_covered", nullable = false)
    private boolean partner3PLCovered;

    @Column(name = "preferred_3pl_carrier_code", length = 32)
    private String preferred3PLCarrierCode;

    @Column(name = "urgent_delivery_supported", nullable = false)
    private boolean urgentDeliverySupported;

    @Column(name = "base_standard_fee_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal baseStandardFeeAmount;

    @Column(name = "base_standard_fee_currency", length = 3, nullable = false)
    private String baseStandardFeeCurrency;

    @Column(name = "per_kg_standard_fee_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal perKgStandardFeeAmount;

    @Column(name = "per_kg_standard_fee_currency", length = 3, nullable = false)
    private String perKgStandardFeeCurrency;

    @Column(name = "urgent_multiplier", precision = 5, scale = 2, nullable = false)
    private BigDecimal urgentMultiplier;

    @Column(name = "intra_state_standard_min_hours", nullable = false)
    private int intraStateStandardMinHours;

    @Column(name = "intra_state_standard_max_hours", nullable = false)
    private int intraStateStandardMaxHours;

    @Column(name = "inter_state_standard_min_hours", nullable = false)
    private int interStateStandardMinHours;

    @Column(name = "inter_state_standard_max_hours", nullable = false)
    private int interStateStandardMaxHours;

    @Column(name = "urgent_min_hours", nullable = false)
    private int urgentMinHours;

    @Column(name = "urgent_max_hours", nullable = false)
    private int urgentMaxHours;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Version
    private Long version;
}
