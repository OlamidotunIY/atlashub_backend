package com.atlashub.commerce.catalog.infrastructure.persistence.mappers;

import com.atlashub.commerce.catalog.domain.entities.PurchaseOrder;
import com.atlashub.commerce.catalog.domain.entities.PurchaseOrderItem;
import com.atlashub.commerce.catalog.domain.valueobject.PurchaseOrderStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.PurchaseOrderItemJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.PurchaseOrderJpa;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PurchaseOrderMapperTest {

    private PurchaseOrderMapperImpl poMapper;
    private PurchaseOrderItemMapperImpl itemMapper;

    @BeforeEach
    void setUp() {
        poMapper = new PurchaseOrderMapperImpl();
        itemMapper = new PurchaseOrderItemMapperImpl();

        ValueObjectMapper vom = new ValueObjectMapper();
        ReflectionTestUtils.setField(poMapper, "valueObjectMapper", vom);
        ReflectionTestUtils.setField(itemMapper, "valueObjectMapper", vom);
    }

    @Test
    @DisplayName("Should map PurchaseOrder domain to PurchaseOrderJpa correctly")
    void shouldMapDomainToPersistence() {
        ZonedDateTime now = ZonedDateTime.now();
        LocalDate delivery = LocalDate.now().plusDays(7);
        PurchaseOrder po = new PurchaseOrder(
                1L,
                10L,
                20L,
                30L,
                PurchaseOrderStatus.DRAFT,
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                delivery,
                List.of(),
                now,
                now
        );

        PurchaseOrderJpa jpa = poMapper.toPersistence(po);

        assertNotNull(jpa);
        assertEquals(1L, jpa.getId());
        assertEquals(10L, jpa.getOrganizationId());
        assertEquals(20L, jpa.getOutletId());
        assertEquals(30L, jpa.getSupplierId());
        assertEquals(PurchaseOrderStatus.DRAFT, jpa.getStatus());
        assertEquals(new BigDecimal("5000.0000"), jpa.getTotalAmount());
        assertEquals(delivery, jpa.getExpectedDeliveryDate());
    }

    @Test
    @DisplayName("Should map PurchaseOrderJpa and items to PurchaseOrder domain correctly")
    void shouldMapPersistenceToDomainWithItems() {
        ZonedDateTime now = ZonedDateTime.now();
        LocalDate delivery = LocalDate.now().plusDays(7);
        PurchaseOrderJpa poJpa = new PurchaseOrderJpa(
                1L,
                10L,
                20L,
                30L,
                PurchaseOrderStatus.SENT,
                new BigDecimal("5000.00"),
                delivery,
                now,
                now,
                1L
        );

        PurchaseOrderItem item = new PurchaseOrderItem(
                101L,
                1L,
                555L,
                10,
                0,
                Money.of(new BigDecimal("500.00"), CurrencyCode.NGN)
        );

        PurchaseOrder domain = poMapper.toDomain(poJpa, List.of(item));

        assertNotNull(domain);
        assertEquals(1L, domain.getId());
        assertEquals(10L, domain.getOrganizationId());
        assertEquals(PurchaseOrderStatus.SENT, domain.getStatus());
        assertEquals(1, domain.getItems().size());
        assertEquals(101L, domain.getItems().get(0).getId());
        assertEquals(555L, domain.getItems().get(0).getProductId());
        assertEquals(10, domain.getItems().get(0).getQuantityOrdered());
    }

    @Test
    @DisplayName("Should map PurchaseOrderItem domain to JPA and vice-versa")
    void shouldMapPurchaseOrderItem() {
        PurchaseOrderItem item = new PurchaseOrderItem(
                101L,
                1L,
                555L,
                10,
                2,
                Money.of(new BigDecimal("500.00"), CurrencyCode.NGN)
        );

        PurchaseOrderItemJpa itemJpa = itemMapper.toPersistence(item);
        assertNotNull(itemJpa);
        assertEquals(101L, itemJpa.getId());
        assertEquals(1L, itemJpa.getPurchaseOrderId());
        assertEquals(555L, itemJpa.getProductId());
        assertEquals(10, itemJpa.getQuantityOrdered());
        assertEquals(2, itemJpa.getQuantityReceived());
        assertEquals(new BigDecimal("500.0000"), itemJpa.getUnitCost());

        PurchaseOrderItem mappedBack = itemMapper.toDomain(itemJpa);
        assertNotNull(mappedBack);
        assertEquals(101L, mappedBack.getId());
        assertEquals(555L, mappedBack.getProductId());
        assertEquals(10, mappedBack.getQuantityOrdered());
        assertEquals(2, mappedBack.getQuantityReceived());
        assertEquals(new BigDecimal("500.0000"), mappedBack.getUnitCost().amount());
    }
}
