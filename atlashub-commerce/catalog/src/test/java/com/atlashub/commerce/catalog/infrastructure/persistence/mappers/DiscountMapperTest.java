package com.atlashub.commerce.catalog.infrastructure.persistence.mappers;

import com.atlashub.commerce.catalog.domain.entities.Discount;
import com.atlashub.commerce.catalog.domain.valueobject.DiscountScope;
import com.atlashub.commerce.catalog.domain.valueobject.DiscountType;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.DiscountJpa;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiscountMapperTest {

    private DiscountMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new DiscountMapperImpl();
        ReflectionTestUtils.setField(mapper, "valueObjectMapper", new ValueObjectMapper());
    }

    @Test
    @DisplayName("Should map Discount domain to DiscountJpa correctly")
    void shouldMapDomainToPersistence() {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(30);
        Discount discount = new Discount(
                1L,
                10L,
                "Summer Sale",
                DiscountType.PERCENTAGE,
                new BigDecimal("15.00"),
                DiscountScope.ORDER_LEVEL,
                Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN),
                100,
                5,
                from,
                to,
                true
        );

        DiscountJpa jpa = mapper.toPersistence(discount);

        assertNotNull(jpa);
        assertEquals(1L, jpa.getId());
        assertEquals(10L, jpa.getOrganizationId());
        assertEquals("Summer Sale", jpa.getName());
        assertEquals(DiscountType.PERCENTAGE, jpa.getType());
        assertEquals(new BigDecimal("15.00"), jpa.getValue());
        assertEquals(DiscountScope.ORDER_LEVEL, jpa.getScope());
        assertEquals(new BigDecimal("1000.0000"), jpa.getMinOrderAmount());
        assertEquals(100, jpa.getMaxUses());
        assertEquals(5, jpa.getUsedCount());
        assertEquals(from, jpa.getValidFrom());
        assertEquals(to, jpa.getValidTo());
        assertTrue(jpa.isActive());
    }

    @Test
    @DisplayName("Should map DiscountJpa to Discount domain correctly")
    void shouldMapPersistenceToDomain() {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(30);
        DiscountJpa jpa = new DiscountJpa(
                1L,
                10L,
                "Summer Sale",
                DiscountType.PERCENTAGE,
                new BigDecimal("15.00"),
                DiscountScope.ORDER_LEVEL,
                new BigDecimal("1000.00"),
                100,
                5,
                from,
                to,
                true,
                1L
        );

        Discount domain = mapper.toDomain(jpa);

        assertNotNull(domain);
        assertEquals(1L, domain.getId());
        assertEquals(10L, domain.getOrganizationId());
        assertEquals("Summer Sale", domain.getName());
        assertEquals(DiscountType.PERCENTAGE, domain.getType());
        assertEquals(new BigDecimal("15.00"), domain.getValue());
        assertEquals(DiscountScope.ORDER_LEVEL, domain.getScope());
        assertNotNull(domain.getMinOrderAmount());
        assertEquals(new BigDecimal("1000.0000"), domain.getMinOrderAmount().amount());
        assertTrue(domain.isActive());
    }
}
