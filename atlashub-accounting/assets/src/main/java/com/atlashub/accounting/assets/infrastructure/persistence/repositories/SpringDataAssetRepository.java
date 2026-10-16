package com.atlashub.accounting.assets.infrastructure.persistence.repositories;

import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.accounting.assets.infrastructure.persistence.entities.AssetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataAssetRepository extends JpaRepository<AssetJpaEntity, Long> {

    List<AssetJpaEntity> findByOrganizationIdAndStatus(Long organizationId, AssetStatus status);

    Optional<AssetJpaEntity> findByOrganizationIdAndTagNumber(Long organizationId, String tagNumber);

    List<AssetJpaEntity> findByOrganizationId(Long organizationId);
}
