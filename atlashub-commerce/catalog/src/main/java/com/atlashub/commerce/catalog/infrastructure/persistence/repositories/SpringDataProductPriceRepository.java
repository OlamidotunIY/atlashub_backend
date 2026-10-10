package com.atlashub.commerce.catalog.infrastructure.persistence.repositories;

import com.atlashub.commerce.catalog.domain.valueobject.PriceLevel;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.ProductPriceJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataProductPriceRepository extends JpaRepository<ProductPriceJpa, Long> {

    @Query("SELECT pp FROM ProductPriceJpa pp WHERE pp.productId = :productId " +
            "AND ((:variantId IS NULL AND pp.variantId IS NULL) OR pp.variantId = :variantId) " +
            "AND pp.priceLevel = :priceLevel")
    Optional<ProductPriceJpa> findPrice(
            @Param("productId") Long productId,
            @Param("variantId") Long variantId,
            @Param("priceLevel") PriceLevel priceLevel
    );
}
