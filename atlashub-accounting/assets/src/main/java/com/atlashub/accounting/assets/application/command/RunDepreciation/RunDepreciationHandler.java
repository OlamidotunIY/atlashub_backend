package com.atlashub.accounting.assets.application.command.RunDepreciation;

import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.repositories.AssetRepository;
import com.atlashub.accounting.assets.domain.services.DepreciationService;
import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class RunDepreciationHandler extends Command<RunDepreciationCommand, Integer> {

    private final AssetRepository assetRepository;
    private final DepreciationService depreciationService;

    public RunDepreciationHandler(AssetRepository assetRepository, DepreciationService depreciationService) {
        this.assetRepository = assetRepository;
        this.depreciationService = depreciationService;
    }

    @Override
    public Integer execute(RunDepreciationCommand input) {
        Objects.requireNonNull(input, "input cannot be null");

        List<Asset> activeAssets = input.organizationId() != null
                ? assetRepository.findByOrganizationIdAndStatus(input.organizationId(), AssetStatus.ACTIVE)
                : assetRepository.findAll().stream().filter(a -> a.getStatus() == AssetStatus.ACTIVE).toList();

        int processed = 0;
        for (Asset asset : activeAssets) {
            Money monthlyDepreciation = depreciationService.calculateMonthlyDepreciation(asset);
            if (monthlyDepreciation.isPositive()) {
                asset.depreciate(monthlyDepreciation);
                assetRepository.save(asset);
                processed++;
            }
        }

        return processed;
    }
}
