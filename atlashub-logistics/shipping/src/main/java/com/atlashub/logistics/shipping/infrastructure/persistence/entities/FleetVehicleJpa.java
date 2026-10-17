package com.atlashub.logistics.shipping.infrastructure.persistence.entities;

import com.atlashub.logistics.shipping.domain.valueobject.VehicleStatus;
import com.atlashub.logistics.shipping.domain.valueobject.VehicleType;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "logistics_fleet_vehicles",
        indexes = {
                @Index(name = "idx_vehicle_plate", columnList = "license_plate", unique = true),
                @Index(name = "idx_vehicle_org_status", columnList = "organization_id, status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FleetVehicleJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "zone_code", nullable = false, length = 32)
    private String zoneCode;

    @Column(name = "license_plate", nullable = false, unique = true, length = 32)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 32)
    private VehicleType vehicleType;

    @Column(name = "max_weight_kg", precision = 10, scale = 2, nullable = false)
    private BigDecimal maxWeightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private VehicleStatus status;

    @Column(name = "current_rider_id")
    private Long currentRiderId;

    @Column(name = "registered_at", nullable = false)
    private ZonedDateTime registeredAt;

    @Version
    private Long version;
}
