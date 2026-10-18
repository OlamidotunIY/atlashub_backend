package com.atlashub.hr.payroll.infrastructure.persistence.entities;

import com.atlashub.hr.payroll.domain.valueobject.LineItemType;
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

@Entity
@Table(
        name = "hr_payslip_line_items",
        indexes = {
                @Index(name = "idx_line_item_payslip", columnList = "payslip_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PayslipLineItemJpaEntity implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "payslip_id", nullable = false)
    private Long payslipId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LineItemType type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Version
    private Long version;

    @Override
    public Long getId() {
        return id;
    }
}
