package com.atlashub.accounting.assets.infrastructure.persistence.adapters;

import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.repositories.AssetRepository;
import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.accounting.assets.infrastructure.persistence.entities.AssetJpaEntity;
import com.atlashub.accounting.assets.infrastructure.persistence.mappers.AssetMapper;
import com.atlashub.accounting.assets.infrastructure.persistence.repositories.SpringDataAssetRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AssetRepositoryAdapter
        extends JpaBaseRepository<Asset, AssetJpaEntity>
        implements AssetRepository {

    private final SpringDataAssetRepository springDataRepo;

    public AssetRepositoryAdapter(
            SpringDataAssetRepository springDataRepo,
            AssetMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "accounting_asset_seq";
    }

    @Override
    public List<Asset> findByOrganizationIdAndStatus(Long organizationId, AssetStatus status) {
        return springDataRepo.findByOrganizationIdAndStatus(organizationId, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Asset> findByOrganizationIdAndTagNumber(Long organizationId, String tagNumber) {
        return springDataRepo.findByOrganizationIdAndTagNumber(organizationId, tagNumber)
                .map(mapper::toDomain);
    }

    @Override
    public List<Asset> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
