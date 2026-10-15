package com.atlashub.accounting.assets.application.command.DisposeAsset;

import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.exceptions.AssetNotFoundException;
import com.atlashub.accounting.assets.domain.repositories.AssetRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DisposeAssetHandler extends Command<DisposeAssetCommand, Void> {

    private final AssetRepository assetRepository;

    public DisposeAssetHandler(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public Void execute(DisposeAssetCommand input) {
        Objects.requireNonNull(input, "input cannot be null");

        Asset asset = assetRepository.findById(input.assetId())
                .filter(a -> a.getOrganizationId().equals(input.organizationId()))
                .orElseThrow(() -> new AssetNotFoundException(input.assetId()));

        asset.dispose(input.saleValue(), input.disposalDate());
        assetRepository.save(asset);

        return null;
    }
}
