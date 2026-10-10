package com.atlashub.pay.accounts.application.commands.ConfigurePaystackSettlementRoute;

import com.atlashub.shared.application.security.ApiEnvironment;

public record ConfigurePaystackSettlementRouteCommand(Long organizationId, ApiEnvironment environment) {}
