package com.atlashub.logistics.warehousing.infrastructure.persistence.entities;

import com.atlashub.logistics.warehousing.domain.valueobject.FacilityType;
import com.atlashub.logistics.warehousing.domain.valueobject.WarehouseStatus;
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
        name = "warehousing_warehouses",
        indexes = {
                @Index(name = "idx_warehouse_code", columnList = "code", unique = true),
                @Index(name = "idx_warehouse_org", columnList = "organization_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WarehouseJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "facility_type", nullable = false, length = 32)
    private FacilityType facilityType;

    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "manager_id")
    private Long managerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private WarehouseStatus status;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Version
    @Column(name = "version")
    private Long version;
}
