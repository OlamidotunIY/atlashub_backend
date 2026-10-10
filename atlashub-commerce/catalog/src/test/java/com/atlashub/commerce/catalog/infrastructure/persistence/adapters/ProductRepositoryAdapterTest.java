package com.atlashub.commerce.catalog.infrastructure.persistence.adapters;

import com.atlashub.commerce.catalog.domain.entities.Product;
import com.atlashub.commerce.catalog.domain.valueobject.ProductStatus;
import com.atlashub.commerce.catalog.domain.valueobject.ProductType;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.ProductJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.mappers.ProductMapper;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataProductRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductRepositoryAdapterTest {

    private SpringDataProductRepository springDataRepo;
    private ProductMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private ProductRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataProductRepository.class);
        mapper = mock(ProductMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new ProductRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should return nextIdentity using sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_product_seq")).thenReturn(42L);
        assertEquals(42L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should find product by code")
    void shouldFindByCode() {
        ProductJpa jpa = mock(ProductJpa.class);
        Product domain = mock(Product.class);

        when(springDataRepo.findByOrganizationIdAndCode(10L, "CODE1")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Product> result = adapter.findByOrganizationIdAndCode(10L, "CODE1");
        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should find product by barcode")
    void shouldFindByBarcode() {
        ProductJpa jpa = mock(ProductJpa.class);
        Product domain = mock(Product.class);

        when(springDataRepo.findByOrganizationIdAndCode(10L, "BARCODE123")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Product> result = adapter.findByOrganizationIdAndBarcode(10L, "BARCODE123");
        assertTrue(result.isPresent());
        assertEquals(domain, result.get());
    }

    @Test
    @DisplayName("Should return paginated products")
    void shouldReturnPaginatedProducts() {
        ProductJpa jpa = mock(ProductJpa.class);
        Product domain = mock(Product.class);
        Page<ProductJpa> jpaPage = new PageImpl<>(List.of(jpa), PageRequest.of(0, 10), 1);

        when(springDataRepo.findByFilters(eq(10L), eq(null), eq(null), eq(ProductStatus.ACTIVE), eq("search"), any()))
                .thenReturn(jpaPage);
        when(mapper.toDomain(jpa)).thenReturn(domain);

        PageResult<Product> page = adapter.findByOrganizationId(10L, null, null, ProductStatus.ACTIVE, "search", 0, 10);

        assertNotNull(page);
        assertEquals(1, page.content().size());
        assertEquals(1, page.totalElements());
    }
}
