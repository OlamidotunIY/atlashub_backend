package com.atlashub.commerce.catalog.infrastructure.persistence.repositories;

import com.atlashub.commerce.catalog.domain.valueobject.ProductStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.ProductJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataProductRepository extends JpaRepository<ProductJpa, Long> {

    Optional<ProductJpa> findByOrganizationIdAndCode(Long organizationId, String code);

    @Query("SELECT p FROM ProductJpa p WHERE p.organizationId = :organizationId " +
            "AND (:vendorId IS NULL OR p.vendorId = :vendorId) " +
            "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ProductJpa> findByFilters(
            @Param("organizationId") Long organizationId,
            @Param("vendorId") Long vendorId,
            @Param("categoryId") Long categoryId,
            @Param("status") ProductStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
