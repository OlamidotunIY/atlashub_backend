package com.atlashub.identity.application.command;

import com.atlashub.identity.domain.model.BusinessType;

public record RegisterMerchantCommand(
    String country,
    String businessName,
    String firstName,
    String lastName,
    String email,
    String phone,
    BusinessType businessType
) {}

