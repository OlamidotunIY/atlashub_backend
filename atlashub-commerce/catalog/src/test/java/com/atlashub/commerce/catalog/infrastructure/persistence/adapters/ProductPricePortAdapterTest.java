package com.atlashub.commerce.catalog.infrastructure.persistence.adapters;

import com.atlashub.commerce.catalog.domain.valueobject.PriceLevel;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.ProductPriceJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataProductPriceRepository;
import com.atlashub.shared.application.port.ProductPricePort;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductPricePortAdapterTest {

    private SpringDataProductPriceRepository priceRepo;
    private DomainSequenceGenerator sequenceGenerator;
    private ProductPricePortAdapter adapter;

    @BeforeEach
    void setUp() {
        priceRepo = mock(SpringDataProductPriceRepository.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        adapter = new ProductPricePortAdapter(priceRepo, sequenceGenerator);
    }

    @Test
    @DisplayName("Should return nextIdentity using sequence")
    void shouldReturnNextIdentity() {
        when(sequenceGenerator.nextIdentity("commerce_product_price_seq")).thenReturn(101L);
        assertEquals(101L, adapter.nextIdentity());
    }

    @Test
    @DisplayName("Should save price via repository")
    void shouldSavePrice() {
        adapter.savePrice(
                1L,
                10L,
                20L,
                "RETAIL",
                Money.of(new BigDecimal("100.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("150.00"), CurrencyCode.NGN),
                new BigDecimal("0.50")
        );

        verify(priceRepo).save(any(ProductPriceJpa.class));
    }

    @Test
    @DisplayName("Should find price and map to ProductPriceDto")
    void shouldFindPrice() {
        ProductPriceJpa jpa = new ProductPriceJpa(
                1L,
                10L,
                20L,
                PriceLevel.RETAIL,
                new BigDecimal("100.00"),
                new BigDecimal("150.00"),
                new BigDecimal("0.50")
        );

        when(priceRepo.findPrice(10L, 20L, PriceLevel.RETAIL)).thenReturn(Optional.of(jpa));

        Optional<ProductPricePort.ProductPriceDto> dto = adapter.findPrice(10L, 20L, "RETAIL");

        assertTrue(dto.isPresent());
        assertEquals(1L, dto.get().id());
        assertEquals(10L, dto.get().productId());
        assertEquals(20L, dto.get().variantId());
        assertEquals("RETAIL", dto.get().priceLevel());
        assertNotNull(dto.get().costPrice());
        assertEquals(new BigDecimal("100.0000"), dto.get().costPrice().amount());
        assertNotNull(dto.get().sellingPrice());
        assertEquals(new BigDecimal("150.0000"), dto.get().sellingPrice().amount());
        assertEquals(new BigDecimal("0.50"), dto.get().markup());
    }
}
