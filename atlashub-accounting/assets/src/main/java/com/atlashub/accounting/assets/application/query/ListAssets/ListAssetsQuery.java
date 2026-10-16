package com.atlashub.accounting.assets.application.query.ListAssets;

import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;

public record ListAssetsQuery(
        Long organizationId,
        AssetStatus status
) {}
