package com.atlashub.accounting.assets.application.query.ListAssets;

import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.repositories.AssetRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ListAssetsHandler extends Query<ListAssetsQuery, List<AssetResult>> {

    private final AssetRepository assetRepository;

    public ListAssetsHandler(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public List<AssetResult> execute(ListAssetsQuery query) {
        Objects.requireNonNull(query, "query cannot be null");

        List<Asset> assets = query.status() != null
                ? assetRepository.findByOrganizationIdAndStatus(query.organizationId(), query.status())
                : assetRepository.findByOrganizationId(query.organizationId());

        return assets.stream()
                .map(this::toResult)
                .toList();
    }

    private AssetResult toResult(Asset asset) {
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
