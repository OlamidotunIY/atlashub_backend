package com.atlashub.logistics.returns.infrastructure.persistence.entities;

import com.atlashub.logistics.returns.domain.valueobject.ReturnFaultType;
import com.atlashub.logistics.returns.domain.valueobject.ReturnReasonType;
import com.atlashub.logistics.returns.domain.valueobject.ReturnStatus;
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
        name = "returns_return_shipments",
        indexes = {
                @Index(name = "idx_return_tracking", columnList = "return_tracking_number", unique = true),
                @Index(name = "idx_return_org_status", columnList = "organization_id, status"),
                @Index(name = "idx_return_original_shipment", columnList = "original_shipment_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReturnShipmentJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "original_sales_order_id", nullable = false)
    private Long originalSalesOrderId;

    @Column(name = "original_shipment_id")
    private Long originalShipmentId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

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

    @Column(name = "destination_outlet_id")
    private Long destinationOutletId;

    @Column(name = "return_tracking_number", nullable = false, unique = true, length = 64)
    private String returnTrackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false, length = 32)
    private ReturnReasonType reasonType;

    @Enumerated(EnumType.STRING)
    @Column(name = "fault_type", nullable = false, length = 32)
    private ReturnFaultType faultType;

    @Column(name = "delivery_attempts_confirmed", nullable = false)
    private int deliveryAttemptsConfirmed;

    @Column(name = "original_item_amount", precision = 19, scale = 4)
    private BigDecimal originalItemAmount;

    @Column(name = "original_item_currency", length = 3)
    private String originalItemCurrency;

    @Column(name = "original_shipping_fee_amount", precision = 19, scale = 4)
    private BigDecimal originalShippingFeeAmount;

    @Column(name = "original_shipping_fee_currency", length = 3)
    private String originalShippingFeeCurrency;

    @Column(name = "return_shipping_fee_amount", precision = 19, scale = 4)
    private BigDecimal returnShippingFeeAmount;

    @Column(name = "return_shipping_fee_currency", length = 3)
    private String returnShippingFeeCurrency;

    @Column(name = "declared_value_amount", precision = 19, scale = 4)
    private BigDecimal declaredValueAmount;

    @Column(name = "declared_value_currency", length = 3)
    private String declaredValueCurrency;

    @Column(name = "customer_refund_amount", precision = 19, scale = 4)
    private BigDecimal customerRefundAmount;

    @Column(name = "customer_refund_currency", length = 3)
    private String customerRefundCurrency;

    @Column(name = "merchant_penalty_amount", precision = 19, scale = 4)
    private BigDecimal merchantPenaltyAmount;

    @Column(name = "merchant_penalty_currency", length = 3)
    private String merchantPenaltyCurrency;

    @Column(name = "merchant_compensation_amount", precision = 19, scale = 4)
    private BigDecimal merchantCompensationAmount;

    @Column(name = "merchant_compensation_currency", length = 3)
    private String merchantCompensationCurrency;

    @Column(name = "refund_customer_shipping", nullable = false)
    private boolean refundCustomerShipping;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ReturnStatus status;

    @Column(name = "inspection_notes", length = 512)
    private String inspectionNotes;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "received_at")
    private ZonedDateTime receivedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
