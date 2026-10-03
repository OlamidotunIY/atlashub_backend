package com.atlashub.accounts.domain.entities;

import com.atlashub.accounts.domain.events.OutletClosedEvent;
import com.atlashub.accounts.domain.events.OutletCreatedEvent;
import com.atlashub.accounts.domain.events.OutletSuspendedEvent;
import com.atlashub.accounts.domain.exceptions.InvalidOutletException;
import com.atlashub.accounts.domain.exceptions.InvalidOutletStateException;
import com.atlashub.accounts.domain.valueobject.OutletStatus;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OutletTest {

    @Test
    void follows_the_outlet_lifecycle_and_publishes_events() {
        Outlet outlet = Outlet.create(
                1L, 2L, "Ikeja", "1 Allen Avenue", "Ikeja", "Lagos",
                new Country("NG"), CurrencyCode.NGN, null);

        assertInstanceOf(OutletCreatedEvent.class, outlet.pullDomainEvents().getFirst());
        outlet.suspend();
        assertEquals(OutletStatus.SUSPENDED, outlet.getStatus());
        assertInstanceOf(OutletSuspendedEvent.class, outlet.pullDomainEvents().getFirst());
        outlet.close();
        assertEquals(OutletStatus.CLOSED, outlet.getStatus());
        assertInstanceOf(OutletClosedEvent.class, outlet.pullDomainEvents().getFirst());
    }

    @Test
    void rejects_invalid_identity_with_a_module_exception() {
        assertThrows(InvalidOutletException.class, () -> Outlet.create(
                null, 2L, "Ikeja", null, null, null,
                new Country("NG"), CurrencyCode.NGN, null));
    }

    @Test
    void cannot_suspend_an_already_suspended_outlet() {
        Outlet outlet = Outlet.create(
                1L, 2L, "Ikeja", null, null, null,
                new Country("NG"), CurrencyCode.NGN, null);
        outlet.suspend();

        assertThrows(InvalidOutletStateException.class, outlet::suspend);
    }
}
