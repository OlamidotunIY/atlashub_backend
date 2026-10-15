package com.atlashub.accounting.assets.application.command.RegisterAsset;

import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.exceptions.InvalidAssetOperationException;
import com.atlashub.accounting.assets.domain.repositories.AssetRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RegisterAssetHandler extends Command<RegisterAssetCommand, Long> {

    private final AssetRepository assetRepository;

    public RegisterAssetHandler(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public Long execute(RegisterAssetCommand input) {
        Objects.requireNonNull(input, "input cannot be null");

        if (assetRepository.findByOrganizationIdAndTagNumber(input.organizationId(), input.tagNumber()).isPresent()) {
            throw new InvalidAssetOperationException("Asset with tag number already exists: " + input.tagNumber());
        }

        Long assetId = assetRepository.nextIdentity();
        Asset asset = Asset.create(
                assetId,
                input.organizationId(),
                input.name(),
                input.tagNumber(),
                input.category(),
                input.purchaseDate(),
                input.purchaseValue(),
                input.salvageValue(),
                input.usefulLifeMonths(),
                input.depreciationMethod()
        );

        Asset saved = assetRepository.save(asset);
        return saved.getId();
    }
}
