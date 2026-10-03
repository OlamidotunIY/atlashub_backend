package com.atlashub.pay.splits.infrastructure.persistence.entities;

import com.atlashub.pay.splits.domain.valueobject.RecipientType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "split_subaccounts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SplitSubaccountJpa {

    @Id
    private Long id;

    @Column(name = "split_rule_id")
    private Long splitRuleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type")
    private RecipientType recipientType;

    @Column(name = "recipient_id")
    private String recipientId;

    @Column(name = "share")
    private BigDecimal share;

    @Column(name = "description")
    private String description;
}
