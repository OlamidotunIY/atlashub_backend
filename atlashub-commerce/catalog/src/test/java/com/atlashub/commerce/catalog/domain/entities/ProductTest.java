package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.events.ProductApprovedEvent;
import com.atlashub.commerce.catalog.domain.events.ProductCreatedEvent;
import com.atlashub.commerce.catalog.domain.exceptions.InvalidProductStateException;
import com.atlashub.commerce.catalog.domain.valueobject.ProductStatus;
import com.atlashub.commerce.catalog.domain.valueobject.ProductType;
import com.atlashub.shared.domain.event.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    @DisplayName("create_standardProduct_initializesAsActiveAndEmitsProductCreatedEvent")
    void create_standardProduct_initializesAsActiveAndEmitsProductCreatedEvent() {
        Product product = Product.create(1L, 10L, null, "SKU-001", "Espresso",
                "Dark roast coffee", 100L, 200L, 300L, true, false, false, ProductType.PHYSICAL);

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getOrganizationId()).isEqualTo(10L);
        assertThat(product.getVendorId()).isNull();
        assertThat(product.getCode()).isEqualTo("SKU-001");
        assertThat(product.getName()).isEqualTo("Espresso");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.isTaxable()).isTrue();
        assertThat(product.isService()).isFalse();
        assertThat(product.isHasVariants()).isFalse();
        assertThat(product.getType()).isEqualTo(ProductType.PHYSICAL);

        List<DomainEvent<?>> events = product.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ProductCreatedEvent.class);
        ProductCreatedEvent event = (ProductCreatedEvent) events.get(0);
        assertThat(event.aggregateId()).isEqualTo(1L);
        assertThat(event.payload().code()).isEqualTo("SKU-001");
        assertThat(event.payload().organizationId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("create_vendorProduct_initializesAsPendingApproval")
    void create_vendorProduct_initializesAsPendingApproval() {
        Product product = Product.create(2L, 10L, 55L, "V-001", "Vendor Mug",
                "Ceramic mug", 100L, null, null, true, false, false, ProductType.PHYSICAL);

        assertThat(product.getStatus()).isEqualTo(ProductStatus.PENDING_APPROVAL);
        assertThat(product.isPendingApproval()).isTrue();
    }

    @Test
    @DisplayName("create_nullRequiredFields_throwsInvalidProductStateException")
    void create_nullRequiredFields_throwsInvalidProductStateException() {
        assertThatThrownBy(() -> Product.create(null, 10L, null, "SKU-001", "Item",
                null, null, null, null, false, false, false, ProductType.PHYSICAL))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("Product id cannot be null");

        assertThatThrownBy(() -> Product.create(1L, null, null, "SKU-001", "Item",
                null, null, null, null, false, false, false, ProductType.PHYSICAL))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("Organization id cannot be null");

        assertThatThrownBy(() -> Product.create(1L, 10L, null, "  ", "Item",
                null, null, null, null, false, false, false, ProductType.PHYSICAL))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("Product code is required");

        assertThatThrownBy(() -> Product.create(1L, 10L, null, "SKU-001", "",
                null, null, null, null, false, false, false, ProductType.PHYSICAL))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("Product name is required");
    }

    @Test
    @DisplayName("approve_whenPendingApproval_transitionsToActiveAndRegistersEvent")
    void approve_whenPendingApproval_transitionsToActiveAndRegistersEvent() {
        Product product = Product.create(3L, 10L, 55L, "V-002", "Vendor Shirt",
                null, null, null, null, true, false, false, ProductType.PHYSICAL);
        product.pullDomainEvents();

        product.approve();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.isActive()).isTrue();
        List<DomainEvent<?>> events = product.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ProductApprovedEvent.class);
        ProductApprovedEvent approvedEvent = (ProductApprovedEvent) events.get(0);
        assertThat(approvedEvent.payload().vendorId()).isEqualTo(55L);
        assertThat(approvedEvent.payload().code()).isEqualTo("V-002");
    }

    @Test
    @DisplayName("approve_whenAlreadyActive_isIdempotent")
    void approve_whenAlreadyActive_isIdempotent() {
        Product product = Product.create(4L, 10L, null, "SKU-004", "Active Item",
                null, null, null, null, false, false, false, ProductType.PHYSICAL);
        product.pullDomainEvents();

        product.approve();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.pullDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("approve_whenInactive_throwsInvalidProductStateException")
    void approve_whenInactive_throwsInvalidProductStateException() {
        Product product = Product.create(5L, 10L, null, "SKU-005", "Item",
                null, null, null, null, false, false, false, ProductType.PHYSICAL);
        product.deactivate();

        assertThatThrownBy(product::approve)
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("Only pending approval products can be approved");
    }

    @Test
    @DisplayName("reject_whenPendingApproval_transitionsToInactive")
    void reject_whenPendingApproval_transitionsToInactive() {
        Product product = Product.create(6L, 10L, 55L, "V-003", "Vendor Hat",
                "Initial desc", null, null, null, true, false, false, ProductType.PHYSICAL);

        product.reject("Poor image quality");

        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        assertThat(product.getDescription()).contains("Poor image quality");
    }

    @Test
    @DisplayName("reject_whenNotPendingApproval_throwsInvalidProductStateException")
    void reject_whenNotPendingApproval_throwsInvalidProductStateException() {
        Product product = Product.create(7L, 10L, null, "SKU-007", "Item",
                null, null, null, null, false, false, false, ProductType.PHYSICAL);

        assertThatThrownBy(() -> product.reject("Reason"))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("Only pending approval products can be rejected");
    }

    @Test
    @DisplayName("deactivateAndActivate_stateTransitionsWorkCorrectly")
    void deactivateAndActivate_stateTransitionsWorkCorrectly() {
        Product product = Product.create(8L, 10L, null, "SKU-008", "Item",
                null, null, null, null, false, false, false, ProductType.PHYSICAL);

        product.deactivate();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);

        // idempotent deactivate
        product.deactivate();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);

        product.activate();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);

        // idempotent activate
        product.activate();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("markAsDeleted_setsStatusToDeleted")
    void markAsDeleted_setsStatusToDeleted() {
        Product product = Product.create(9L, 10L, null, "SKU-009", "Item",
                null, null, null, null, false, false, false, ProductType.PHYSICAL);

        product.markAsDeleted();
        assertThat(product.isDeleted()).isTrue();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DELETED);
    }

    @Test
    @DisplayName("updateDetails_whenDeleted_throwsInvalidProductStateException")
    void updateDetails_whenDeleted_throwsInvalidProductStateException() {
        Product product = Product.create(10L, 10L, null, "SKU-010", "Item",
                null, null, null, null, false, false, false, ProductType.PHYSICAL);
        product.markAsDeleted();

        assertThatThrownBy(() -> product.updateDetails("New Name", "New Desc", null, null, null, true, true))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("Cannot update a deleted product");
    }
}
