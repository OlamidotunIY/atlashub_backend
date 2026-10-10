package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.exceptions.InvalidProductStateException;
import com.atlashub.commerce.catalog.domain.valueobject.SupplierStatus;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierTest {

    @Test
    @DisplayName("create_validSupplier_initializesAsActive")
    void create_validSupplier_initializesAsActive() {
        Supplier supplier = Supplier.create(1L, 10L, "Acme Supplies",
                new EmailAddress("sales@acme.com"), new PhoneNumber("+2348011223344"), "123 Market St, Lagos");

        assertThat(supplier.getId()).isEqualTo(1L);
        assertThat(supplier.getOrganizationId()).isEqualTo(10L);
        assertThat(supplier.getName()).isEqualTo("Acme Supplies");
        assertThat(supplier.getStatus()).isEqualTo(SupplierStatus.ACTIVE);
        assertThat(supplier.isActive()).isTrue();
    }

    @Test
    @DisplayName("deactivateAndReactivate_transitionsCorrectly")
    void deactivateAndReactivate_transitionsCorrectly() {
        Supplier supplier = Supplier.create(2L, 10L, "Supplier Two",
                new EmailAddress("two@supplier.com"), new PhoneNumber("+2348011223344"), "Lagos");

        supplier.deactivate();
        assertThat(supplier.getStatus()).isEqualTo(SupplierStatus.INACTIVE);
        assertThat(supplier.isActive()).isFalse();

        // idempotent deactivate
        supplier.deactivate();
        assertThat(supplier.getStatus()).isEqualTo(SupplierStatus.INACTIVE);

        supplier.reactivate();
        assertThat(supplier.getStatus()).isEqualTo(SupplierStatus.ACTIVE);
        assertThat(supplier.isActive()).isTrue();

        // idempotent reactivate
        supplier.reactivate();
        assertThat(supplier.getStatus()).isEqualTo(SupplierStatus.ACTIVE);
    }

    @Test
    @DisplayName("updateDetails_updatesFieldsCorrectly")
    void updateDetails_updatesFieldsCorrectly() {
        Supplier supplier = Supplier.create(3L, 10L, "Old Name",
                new EmailAddress("old@supplier.com"), new PhoneNumber("+2348011223344"), "Old Address");

        supplier.updateDetails("New Name", new EmailAddress("new@supplier.com"),
                new PhoneNumber("+2348099887766"), "New Address");

        assertThat(supplier.getName()).isEqualTo("New Name");
        assertThat(supplier.getEmail().value()).isEqualTo("new@supplier.com");
        assertThat(supplier.getAddress()).isEqualTo("New Address");
    }

    @Test
    @DisplayName("create_nullRequiredFields_throwsInvalidProductStateException")
    void create_nullRequiredFields_throwsInvalidProductStateException() {
        assertThatThrownBy(() -> Supplier.create(null, 10L, "Name", null, null, null))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("Supplier id cannot be null");

        assertThatThrownBy(() -> Supplier.create(1L, null, "Name", null, null, null))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("Organization id cannot be null");

        assertThatThrownBy(() -> Supplier.create(1L, 10L, "  ", null, null, null))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("Supplier name is required");
    }
}
