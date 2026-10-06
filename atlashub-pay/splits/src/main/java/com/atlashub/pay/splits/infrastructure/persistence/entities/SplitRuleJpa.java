package com.atlashub.pay.splits.infrastructure.persistence.entities;

import com.atlashub.pay.splits.domain.valueobject.SplitType;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "split_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SplitRuleJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private SplitType type;

    @Column(name = "platform_fee_percentage")
    private BigDecimal platformFeePercentage;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "splitRuleId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SplitSubaccountJpa> subaccounts;

    @Version
    private Long version;
}
