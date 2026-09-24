package com.atlashub.catalog.adapter.out.repository;

import com.atlashub.catalog.adapter.out.entity.ProductPricingJpaEntity;
import com.atlashub.catalog.adapter.out.mapper.ProductPricingMapper;
import com.atlashub.catalog.domain.model.ProductPricing;
import com.atlashub.catalog.domain.repository.ProductPricingRepository;
import com.atlashub.catalog.domain.valueobject.BillingCycle;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProductPricingRepositoryAdapter implements ProductPricingRepository {

    private final SpringDataProductPricingRepository repository;
    private final ProductPricingMapper mapper;
    private final DomainSequenceGenerator sequenceGenerator;

    public ProductPricingRepositoryAdapter(
            SpringDataProductPricingRepository repository,
            ProductPricingMapper mapper,
            DomainSequenceGenerator sequenceGenerator) {
        this.repository = repository;
        this.mapper = mapper;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("ProductPricing_seq");
    }

    @Override
    public ProductPricing save(ProductPricing productPricing) {
        ProductPricingJpaEntity entity = mapper.toEntity(productPricing);
        ProductPricingJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ProductPricing> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public boolean existsById(Long id) {
        return false;
    }

    @Override
    public Optional<ProductPricing> findByProductIdAndCycleAndCurrency(Long productId, BillingCycle cycle, CurrencyCode currency) {
        return repository.findByHubProductIdAndBillingCycleAndCurrencyCode(productId, cycle.name(), currency.name())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<ProductPricing> findByProductIdAndCurrency(Long productId, CurrencyCode currency) {
        return repository.findByHubProductIdAndCurrencyCode(productId, currency.name()).map(mapper::toDomain);
    }

    @Override
    public List<ProductPricing> findAllByProductId(Long productId) {
        return repository.findAllByHubProductId(productId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
