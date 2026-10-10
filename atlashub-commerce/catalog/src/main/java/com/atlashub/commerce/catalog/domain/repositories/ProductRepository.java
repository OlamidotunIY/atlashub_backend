package com.atlashub.commerce.catalog.domain.repositories;

import com.atlashub.commerce.catalog.domain.entities.Product;
import com.atlashub.commerce.catalog.domain.valueobject.ProductStatus;
import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.shared.domain.valueobject.PageResult;

import java.util.Optional;

public interface ProductRepository extends Repository<Product> {

    Optional<Product> findByOrganizationIdAndCode(Long organizationId, String code);

    Optional<Product> findByOrganizationIdAndBarcode(Long organizationId, String barcode);

    PageResult<Product> findByOrganizationId(Long organizationId, Long vendorId, Long categoryId,
                                            ProductStatus status, String search, int page, int size);
}
