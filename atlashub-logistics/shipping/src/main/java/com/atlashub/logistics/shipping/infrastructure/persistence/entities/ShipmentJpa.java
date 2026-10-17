package com.atlashub.logistics.shipping.infrastructure.persistence.entities;

import com.atlashub.logistics.shipping.domain.valueobject.CarrierChannel;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryFaultType;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentStatus;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
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
        name = "logistics_shipments",
        indexes = {
                @Index(name = "idx_shipment_tracking", columnList = "tracking_number", unique = true),
                @Index(name = "idx_shipment_org_status", columnList = "organization_id, status"),
                @Index(name = "idx_shipment_batch", columnList = "consolidation_batch_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ShipmentJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 64)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private ShipmentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_tier", nullable = false, length = 32)
    private DeliveryTier serviceTier;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_type", nullable = false, length = 32)
    private RouteType routeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_channel", length = 32)
    private CarrierChannel fulfillmentChannel;

    @Column(name = "origin_street")
    private String originStreet;

    @Column(name = "origin_city")
    private String originCity;

    @Column(name = "origin_state")
    private String originState;

    @Column(name = "origin_country")
    private String originCountry;

    @Column(name = "origin_postal_code")
    private String originPostalCode;

    @Column(name = "dest_street")
    private String destStreet;

    @Column(name = "dest_city")
    private String destCity;

    @Column(name = "dest_state")
    private String destState;

    @Column(name = "dest_country")
    private String destCountry;

    @Column(name = "dest_postal_code")
    private String destPostalCode;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "receiver_id")
    private Long receiverId;

    @Column(name = "weight_kg", precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "length_cm", precision = 10, scale = 2)
    private BigDecimal lengthCm;

    @Column(name = "width_cm", precision = 10, scale = 2)
    private BigDecimal widthCm;

    @Column(name = "height_cm", precision = 10, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "declared_amount", precision = 19, scale = 4)
    private BigDecimal declaredAmount;

    @Column(name = "declared_currency", length = 3)
    private String declaredCurrency;

    @Column(name = "customer_fee_amount", precision = 19, scale = 4)
    private BigDecimal customerFeeAmount;

    @Column(name = "customer_fee_currency", length = 3)
    private String customerFeeCurrency;

    @Column(name = "baseline_single_cost_amount", precision = 19, scale = 4)
    private BigDecimal baselineSingleCostAmount;

    @Column(name = "baseline_single_cost_currency", length = 3)
    private String baselineSingleCostCurrency;

    @Column(name = "allocated_actual_cost_amount", precision = 19, scale = 4)
    private BigDecimal allocatedActualCostAmount;

    @Column(name = "allocated_actual_cost_currency", length = 3)
    private String allocatedActualCostCurrency;

    @Column(name = "consolidation_batch_id")
    private Long consolidationBatchId;

    @Column(name = "delivery_code", length = 16)
    private String deliveryCode;

    @Column(name = "delivery_window_start")
    private ZonedDateTime deliveryWindowStart;

    @Column(name = "delivery_window_end")
    private ZonedDateTime deliveryWindowEnd;

    @Column(name = "latest_dispatch_at")
    private ZonedDateTime latestDispatchAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_milestone", length = 48)
    private TrackingMilestone currentMilestone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ShipmentStatus status;

    @Column(name = "delivery_attempts", nullable = false)
    private int deliveryAttempts;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_fault_type", length = 32)
    private DeliveryFaultType failureFaultType;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "rider_id")
    private Long riderId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "carrier_3pl_code", length = 32)
    private String carrier3PLCode;

    @Column(name = "carrier_3pl_reference", length = 128)
    private String carrier3PLReference;

    @Column(name = "proof_of_delivery_url", length = 1000)
    private String proofOfDeliveryUrl;

    @Column(name = "current_location", length = 255)
    private String currentLocation;

    @Column(name = "current_latitude", precision = 10, scale = 7)
    private BigDecimal currentLatitude;

    @Column(name = "current_longitude", precision = 10, scale = 7)
    private BigDecimal currentLongitude;

    @Column(name = "incentive_eligible", nullable = false)
    private boolean incentiveEligible;

    @Column(name = "dispatched_at")
    private ZonedDateTime dispatchedAt;

    @Column(name = "delivered_at")
    private ZonedDateTime deliveredAt;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Version
    private Long version;
}
