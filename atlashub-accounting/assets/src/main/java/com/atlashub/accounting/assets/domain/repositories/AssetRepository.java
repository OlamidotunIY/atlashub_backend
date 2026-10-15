package com.atlashub.accounting.assets.domain.repositories;

import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends Repository<Asset> {

    List<Asset> findByOrganizationIdAndStatus(Long organizationId, AssetStatus status);

    Optional<Asset> findByOrganizationIdAndTagNumber(Long organizationId, String tagNumber);

    List<Asset> findByOrganizationId(Long organizationId);
}
