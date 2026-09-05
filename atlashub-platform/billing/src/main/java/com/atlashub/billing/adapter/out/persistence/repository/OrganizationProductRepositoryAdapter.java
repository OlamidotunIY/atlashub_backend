package com.atlashub.billing.adapter.out.persistence.repository;

import com.atlashub.billing.adapter.out.persistence.mapper.OrganizationProductMapper;
import com.atlashub.billing.domain.model.OrganizationProduct;
import com.atlashub.billing.domain.repository.OrganizationProductRepository;
import com.atlashub.shared.adapter.out.external.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrganizationProductRepositoryAdapter implements OrganizationProductRepository {

    private final SpringDataOrganizationProductRepository jpaRepository;
    private final OrganizationProductMapper mapper;
    private final DomainSequenceGenerator sequenceGenerator;

    public OrganizationProductRepositoryAdapter(
            SpringDataOrganizationProductRepository jpaRepository,
            OrganizationProductMapper mapper,
            DomainSequenceGenerator sequenceGenerator) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("org_product_seq");
    }

    @Override
    public OrganizationProduct save(OrganizationProduct orgProduct) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(orgProduct)));
    }

    @Override
    public Optional<OrganizationProduct> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}
