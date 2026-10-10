package com.atlashub.commerce.catalog.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogValueObjectsTest {

    @Test
    @DisplayName("productStatus_constantsAreDefined")
    void productStatus_constantsAreDefined() {
        assertThat(ProductStatus.values()).containsExactlyInAnyOrder(
                ProductStatus.ACTIVE,
                ProductStatus.INACTIVE,
                ProductStatus.PENDING_APPROVAL,
                ProductStatus.DELETED
        );
    }

    @Test
    @DisplayName("productType_constantsAreDefined")
    void productType_constantsAreDefined() {
        assertThat(ProductType.values()).containsExactlyInAnyOrder(
                ProductType.PHYSICAL,
                ProductType.DIGITAL,
                ProductType.SERVICE
        );
    }

    @Test
    @DisplayName("priceLevel_constantsAreDefined")
    void priceLevel_constantsAreDefined() {
        assertThat(PriceLevel.values()).containsExactlyInAnyOrder(
                PriceLevel.RETAIL,
                PriceLevel.WHOLESALE
        );
    }

    @Test
    @DisplayName("discountEnums_constantsAreDefined")
    void discountEnums_constantsAreDefined() {
        assertThat(DiscountType.values()).containsExactlyInAnyOrder(
                DiscountType.PERCENTAGE,
                DiscountType.FLAT_AMOUNT
        );
        assertThat(DiscountScope.values()).containsExactlyInAnyOrder(
                DiscountScope.ORDER_LEVEL,
                DiscountScope.ITEM_LEVEL
        );
    }

    @Test
    @DisplayName("supplierStatus_constantsAreDefined")
    void supplierStatus_constantsAreDefined() {
        assertThat(SupplierStatus.values()).containsExactlyInAnyOrder(
                SupplierStatus.ACTIVE,
                SupplierStatus.INACTIVE
        );
    }

    @Test
    @DisplayName("purchaseOrderStatus_constantsAreDefined")
    void purchaseOrderStatus_constantsAreDefined() {
        assertThat(PurchaseOrderStatus.values()).containsExactlyInAnyOrder(
                PurchaseOrderStatus.DRAFT,
                PurchaseOrderStatus.SENT,
                PurchaseOrderStatus.PARTIALLY_RECEIVED,
                PurchaseOrderStatus.RECEIVED,
                PurchaseOrderStatus.CANCELLED
        );
    }

    @Test
    @DisplayName("vendorEnums_constantsAreDefined")
    void vendorEnums_constantsAreDefined() {
        assertThat(VendorStatus.values()).containsExactlyInAnyOrder(
                VendorStatus.PENDING,
                VendorStatus.ACTIVE,
                VendorStatus.SUSPENDED,
                VendorStatus.TERMINATED
        );
        assertThat(DisbursementSchedule.values()).containsExactlyInAnyOrder(
                DisbursementSchedule.DAILY,
                DisbursementSchedule.WEEKLY,
                DisbursementSchedule.ON_DEMAND
        );
    }
}
