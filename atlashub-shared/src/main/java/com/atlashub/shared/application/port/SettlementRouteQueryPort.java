package com.atlashub.shared.application.port;

import com.atlashub.shared.application.security.ApiEnvironment;

import java.util.List;

public interface SettlementRouteQueryPort {
    List<SettlementRoute> findActiveRoutes(ApiEnvironment environment);

    record SettlementRoute(Long organizationId, ApiEnvironment environment, String provider,
                           String providerSubaccountCode, Long anchorDepositAccountId) {}
}
