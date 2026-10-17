package com.atlashub.logistics.shipping.infrastructure.persistence.entities;

import com.atlashub.logistics.shipping.domain.valueobject.RiderStatus;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "logistics_dispatch_riders",
        indexes = {
                @Index(name = "idx_rider_user", columnList = "user_id", unique = true),
                @Index(name = "idx_rider_org_status", columnList = "organization_id, status"),
                @Index(name = "idx_rider_zone_status", columnList = "zone_code, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DispatchRiderJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "zone_code", nullable = false, length = 32)
    private String zoneCode;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "license_number", nullable = false, length = 64)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private RiderStatus status;

    @Column(name = "current_shipment_id")
    private Long currentShipmentId;

    @Column(name = "onboarded_at", nullable = false)
    private ZonedDateTime onboardedAt;

    @Version
    private Long version;
}
