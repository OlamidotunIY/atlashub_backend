package com.atlashub.logistics.shipping.infrastructure.persistence.entities;

import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentStatus;
import com.atlashub.logistics.shipping.domain.valueobject.TrackingMilestone;
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
        name = "logistics_shipment_milestone_logs",
        indexes = {
                @Index(name = "idx_milestone_shipment", columnList = "shipment_id, occurred_at"),
                @Index(name = "idx_milestone_tracking", columnList = "tracking_number, occurred_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ShipmentMilestoneLogJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(name = "tracking_number", nullable = false, length = 64)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_type", nullable = false, length = 32)
    private RouteType routeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "milestone", nullable = false, length = 48)
    private TrackingMilestone milestone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ShipmentStatus status;

    @Column(name = "location_name", length = 255)
    private String locationName;

    @Column(name = "state", length = 64)
    private String state;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "occurred_at", nullable = false)
    private ZonedDateTime occurredAt;

    @Version
    private Long version;
}
