package com.atlashub.catalog.adapter.out;

import com.atlashub.catalog.adapter.out.entity.HubProductJpaEntity;
import com.atlashub.catalog.adapter.out.mapper.HubProductMapper;
import com.atlashub.catalog.adapter.out.repository.SpringDataHubProductRepository;
import com.atlashub.catalog.domain.model.HubProduct;
import com.atlashub.catalog.domain.repository.HubProductRepository;
import com.atlashub.catalog.domain.valueobject.ProductStatus;
import com.atlashub.shared.adapter.out.external.DomainSequenceGenerator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class HubProductRepositoryAdapter implements HubProductRepository {

    private final SpringDataHubProductRepository repository;
    private final HubProductMapper mapper;
    private final DomainSequenceGenerator sequenceGenerator;

    public HubProductRepositoryAdapter(
            SpringDataHubProductRepository repository,
            HubProductMapper mapper,
            DomainSequenceGenerator sequenceGenerator) {
        this.repository = repository;
        this.mapper = mapper;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("HubProduct_seq");
    }

    @Override
    public HubProduct save(HubProduct product) {
        HubProductJpaEntity entity = mapper.toEntity(product);
        HubProductJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<HubProduct> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<HubProduct> findByStatus(ProductStatus status) {
        return repository.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<HubProduct> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
