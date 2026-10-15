package com.atlashub.accounting.assets.infrastructure.persistence.entities;

import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.accounting.assets.domain.valueobject.DepreciationMethod;
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
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "accounting_assets",
        indexes = {
                @Index(name = "idx_asset_org_tag", columnList = "organization_id, tag_number", unique = true),
                @Index(name = "idx_asset_org_status", columnList = "organization_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AssetJpaEntity implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "tag_number", nullable = false)
    private String tagNumber;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "purchase_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal purchaseValue;

    @Column(name = "salvage_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal salvageValue;

    @Column(name = "useful_life_months", nullable = false)
    private Integer usefulLifeMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "depreciation_method", nullable = false)
    private DepreciationMethod depreciationMethod;

    @Column(name = "accumulated_depreciation", nullable = false, precision = 19, scale = 4)
    private BigDecimal accumulatedDepreciation;

    @Column(name = "current_book_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBookValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssetStatus status;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "disposal_date")
    private LocalDate disposalDate;

    @Column(name = "sale_value", precision = 19, scale = 4)
    private BigDecimal saleValue;

    @Version
    private Long version;
}
