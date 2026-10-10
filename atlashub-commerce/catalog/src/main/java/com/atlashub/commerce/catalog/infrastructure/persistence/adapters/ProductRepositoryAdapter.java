package com.atlashub.commerce.catalog.infrastructure.persistence.adapters;

import com.atlashub.commerce.catalog.domain.entities.Product;
import com.atlashub.commerce.catalog.domain.repositories.ProductRepository;
import com.atlashub.commerce.catalog.domain.valueobject.ProductStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.ProductJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.mappers.ProductMapper;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataProductRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductRepositoryAdapter
        extends JpaBaseRepository<Product, ProductJpa>
        implements ProductRepository {

    private final SpringDataProductRepository springDataRepo;

    public ProductRepositoryAdapter(SpringDataProductRepository springDataRepo,
                                  ProductMapper mapper,
                                  DomainSequenceGenerator sequenceGenerator,
                                  DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "commerce_product_seq";
    }

    @Override
    public Optional<Product> findByOrganizationIdAndCode(Long organizationId, String code) {
        return springDataRepo.findByOrganizationIdAndCode(organizationId, code)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findByOrganizationIdAndBarcode(Long organizationId, String barcode) {
        return springDataRepo.findByOrganizationIdAndCode(organizationId, barcode)
                .map(mapper::toDomain);
    }

    @Override
    public PageResult<Product> findByOrganizationId(Long organizationId, Long vendorId, Long categoryId,
                                                    ProductStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductJpa> jpaPage = springDataRepo.findByFilters(organizationId, vendorId, categoryId, status, search, pageable);

        List<Product> products = jpaPage.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new PageResult<>(
                products,
                jpaPage.getNumber(),
                jpaPage.getSize(),
                jpaPage.getTotalElements(),
                jpaPage.getTotalPages()
        );
    }
}
