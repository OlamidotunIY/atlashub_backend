package com.atlashub.pay.settlement.presentation.dto;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
public record SettlementResponse(Long id,String provider,String providerSettlementId,BigDecimal amount,String currency,
                                 ZonedDateTime settledAt,String status,String description){}
