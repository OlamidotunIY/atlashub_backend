package com.atlashub.pay.settlement.presentation.dto;
import jakarta.validation.constraints.NotBlank;
public record DisputeSettlementRequest(@NotBlank String reason){}
