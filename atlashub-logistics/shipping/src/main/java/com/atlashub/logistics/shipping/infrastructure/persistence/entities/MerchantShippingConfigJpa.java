package com.atlashub.logistics.shipping.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "logistics_merchant_configs",
        indexes = {
                @Index(name = "idx_merchant_config_org", columnList = "organization_id", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MerchantShippingConfigJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false, unique = true)
    private Long organizationId;

    @Column(name = "urgent_delivery_enabled", nullable = false)
    private boolean urgentDeliveryEnabled;

    @Column(name = "default_street")
    private String defaultStreet;

    @Column(name = "default_city")
    private String defaultCity;

    @Column(name = "default_state")
    private String defaultState;

    @Column(name = "default_country")
    private String defaultCountry;

    @Column(name = "default_postal_code")
    private String defaultPostalCode;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
