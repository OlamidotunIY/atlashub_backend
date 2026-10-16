package com.atlashub.accounting.assets.application.query.GetAssetDetails;

import com.atlashub.accounting.assets.application.query.ListAssets.AssetResult;
import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.exceptions.AssetNotFoundException;
import com.atlashub.accounting.assets.domain.repositories.AssetRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GetAssetDetailsHandler extends Query<GetAssetDetailsQuery, AssetResult> {

    private final AssetRepository assetRepository;

    public GetAssetDetailsHandler(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public AssetResult execute(GetAssetDetailsQuery query) {
        Objects.requireNonNull(query, "query cannot be null");

        Asset asset = assetRepository.findById(query.assetId())
                .filter(a -> a.getOrganizationId().equals(query.organizationId()))
                .orElseThrow(() -> new AssetNotFoundException(query.assetId()));

        return new AssetResult(
                asset.getId(),
                asset.getOrganizationId(),
                asset.getName(),
                asset.getTagNumber(),
                asset.getCategory(),
                asset.getPurchaseDate(),
                asset.getPurchaseValue().amount(),
                asset.getSalvageValue().amount(),
                asset.getUsefulLifeMonths(),
                asset.getDepreciationMethod(),
                asset.getAccumulatedDepreciation().amount(),
                asset.getCurrentBookValue().amount(),
                asset.getStatus(),
                asset.getPurchaseValue().currency().name(),
                asset.getDisposalDate(),
                asset.getSaleValue() != null ? asset.getSaleValue().amount() : null
        );
    }
}
