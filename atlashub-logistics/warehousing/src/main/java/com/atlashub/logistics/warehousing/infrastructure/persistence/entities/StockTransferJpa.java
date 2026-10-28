package com.atlashub.logistics.warehousing.infrastructure.persistence.entities;

import com.atlashub.logistics.warehousing.domain.valueobject.LocationType;
import com.atlashub.logistics.warehousing.domain.valueobject.TransferStatus;
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

@Entity(name = "WarehousingStockTransfer")
@Table(
        name = "warehousing_stock_transfers",
        indexes = {
                @Index(name = "idx_transfer_org_status", columnList = "organization_id, status"),
                @Index(name = "idx_transfer_shipment", columnList = "shipment_id"),
                @Index(name = "idx_transfer_source_dest", columnList = "source_id, destination_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StockTransferJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private LocationType sourceType;

    @Column(name = "source_street")
    private String sourceStreet;

    @Column(name = "source_city")
    private String sourceCity;

    @Column(name = "source_state")
    private String sourceState;

    @Column(name = "source_country")
    private String sourceCountry;

    @Column(name = "source_postal_code")
    private String sourcePostalCode;

    @Column(name = "destination_id", nullable = false)
    private Long destinationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "destination_type", nullable = false, length = 32)
    private LocationType destinationType;

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

    @Column(name = "total_weight_kg", precision = 10, scale = 2)
    private BigDecimal totalWeightKg;

    @Column(name = "transfer_shipping_fee_amount", precision = 19, scale = 4)
    private BigDecimal transferShippingFeeAmount;

    @Column(name = "transfer_shipping_fee_currency", length = 3)
    private String transferShippingFeeCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TransferStatus status;

    @Column(name = "shipment_id")
    private Long shipmentId;

    @Column(name = "items_json", columnDefinition = "json")
    private String itemsJson;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "requested_at", nullable = false)
    private ZonedDateTime requestedAt;

    @Column(name = "received_at")
    private ZonedDateTime receivedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
