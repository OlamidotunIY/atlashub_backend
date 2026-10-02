package com.atlashub.pay.splits.domain.entities;

import com.atlashub.pay.splits.domain.valueobject.RecipientType;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SplitSubaccount {
    private final Long id;
    private final Long splitRuleId;
    private final RecipientType recipientType;
    private final String recipientId;
    private final BigDecimal share;
    private final String description;

    public SplitSubaccount(Long id, Long splitRuleId, RecipientType recipientType, String recipientId, BigDecimal share, String description) {
        this.id = id;
        this.splitRuleId = splitRuleId;
        this.recipientType = recipientType;
        this.recipientId = recipientId;
        this.share = share;
        this.description = description;
    }

}
