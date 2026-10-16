package com.atlashub.accounting.assets.application.query.GetAssetDetails;

public record GetAssetDetailsQuery(
        Long assetId,
        Long organizationId
) {}
