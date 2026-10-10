package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.events.VendorApprovedEvent;
import com.atlashub.commerce.catalog.domain.events.VendorSuspendedEvent;
import com.atlashub.commerce.catalog.domain.events.VendorTerminatedEvent;
import com.atlashub.commerce.catalog.domain.exceptions.InvalidProductStateException;
import com.atlashub.commerce.catalog.domain.valueobject.DisbursementSchedule;
import com.atlashub.commerce.catalog.domain.valueobject.VendorStatus;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VendorTest {

    @Test
    @DisplayName("create_initializesAsPending")
    void create_initializesAsPending() {
        Vendor vendor = Vendor.create(1L, 10L, 501L, "Top Store",
                new EmailAddress("store@top.com"), new PhoneNumber("+2348011223344"),
                "044", "0123456789", "Top Store Ltd",
                BigDecimal.valueOf(10.5), DisbursementSchedule.WEEKLY);

        assertThat(vendor.getId()).isEqualTo(1L);
        assertThat(vendor.getOrganizationId()).isEqualTo(10L);
        assertThat(vendor.getUserId()).isEqualTo(501L);
        assertThat(vendor.getBusinessName()).isEqualTo("Top Store");
        assertThat(vendor.getStatus()).isEqualTo(VendorStatus.PENDING);
        assertThat(vendor.getCommissionRate()).isEqualByComparingTo(BigDecimal.valueOf(10.5));
        assertThat(vendor.getDisbursementSchedule()).isEqualTo(DisbursementSchedule.WEEKLY);
    }

    @Test
    @DisplayName("approve_whenPending_transitionsToActiveAndRegistersEvent")
    void approve_whenPending_transitionsToActiveAndRegistersEvent() {
        Vendor vendor = Vendor.create(2L, 10L, 502L, "Approved Vendor",
                null, null, null, null, null, null, null);

        vendor.approve();

        assertThat(vendor.getStatus()).isEqualTo(VendorStatus.ACTIVE);
        assertThat(vendor.isActive()).isTrue();
        List<DomainEvent<?>> events = vendor.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(VendorApprovedEvent.class);
    }

    @Test
    @DisplayName("approve_whenAlreadyActive_isIdempotent")
    void approve_whenAlreadyActive_isIdempotent() {
        Vendor vendor = Vendor.create(3L, 10L, 503L, "Active Vendor",
                null, null, null, null, null, null, null);
        vendor.approve();
        vendor.pullDomainEvents();

        vendor.approve();
        assertThat(vendor.getStatus()).isEqualTo(VendorStatus.ACTIVE);
        assertThat(vendor.pullDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("suspend_whenActive_transitionsToSuspendedAndRegistersEvent")
    void suspend_whenActive_transitionsToSuspendedAndRegistersEvent() {
        Vendor vendor = Vendor.create(4L, 10L, 504L, "Suspended Vendor",
                null, null, null, null, null, null, null);
        vendor.approve();
        vendor.pullDomainEvents();

        vendor.suspend("Policy violation");

        assertThat(vendor.getStatus()).isEqualTo(VendorStatus.SUSPENDED);
        assertThat(vendor.isSuspended()).isTrue();
        List<DomainEvent<?>> events = vendor.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(VendorSuspendedEvent.class);
    }

    @Test
    @DisplayName("terminate_whenActiveOrSuspended_transitionsToTerminatedAndRegistersEvent")
    void terminate_whenActiveOrSuspended_transitionsToTerminatedAndRegistersEvent() {
        Vendor vendor = Vendor.create(5L, 10L, 505L, "Terminated Vendor",
                null, null, null, null, null, null, null);
        vendor.approve();
        vendor.pullDomainEvents();

        vendor.terminate();

        assertThat(vendor.getStatus()).isEqualTo(VendorStatus.TERMINATED);
        assertThat(vendor.isTerminated()).isTrue();
        List<DomainEvent<?>> events = vendor.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(VendorTerminatedEvent.class);
    }

    @Test
    @DisplayName("suspend_whenTerminated_throwsInvalidProductStateException")
    void suspend_whenTerminated_throwsInvalidProductStateException() {
        Vendor vendor = Vendor.create(6L, 10L, 506L, "Vendor",
                null, null, null, null, null, null, null);
        vendor.terminate();

        assertThatThrownBy(() -> vendor.suspend("Reason"))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("Cannot suspend a terminated vendor");
    }

    @Test
    @DisplayName("updateCommissionRate_validatesRateRange")
    void updateCommissionRate_validatesRateRange() {
        Vendor vendor = Vendor.create(7L, 10L, 507L, "Vendor",
                null, null, null, null, null, null, null);

        vendor.updateCommissionRate(BigDecimal.valueOf(15));
        assertThat(vendor.getCommissionRate()).isEqualByComparingTo(BigDecimal.valueOf(15));

        assertThatThrownBy(() -> vendor.updateCommissionRate(BigDecimal.valueOf(-1)))
                .isInstanceOf(InvalidProductStateException.class);

        assertThatThrownBy(() -> vendor.updateCommissionRate(BigDecimal.valueOf(101)))
                .isInstanceOf(InvalidProductStateException.class);
    }
}
