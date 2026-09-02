package com.atlashub.catalog.domain.repository;

import com.atlashub.catalog.domain.model.HubProduct;

import com.atlashub.catalog.domain.valueobject.ProductStatus;

import java.util.List;
import java.util.Optional;

public interface HubProductRepository {
    Long nextIdentity();
    HubProduct save(HubProduct product);
    Optional<HubProduct> findById(Long id);
    List<HubProduct> findByStatus(ProductStatus status);
    List<HubProduct> findAll();
}
