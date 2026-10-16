package com.atlashub.hr.staff.domain.entities;

import com.atlashub.hr.staff.domain.events.InfractionLoggedEvent;
import com.atlashub.hr.staff.domain.events.InfractionResolvedEvent;
import com.atlashub.hr.staff.domain.exceptions.InvalidStaffOperationException;
import com.atlashub.hr.staff.domain.valueobject.InfractionSeverity;
import com.atlashub.hr.staff.domain.valueobject.InfractionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InfractionTest {

    @Test
    @DisplayName("Should create Infraction in OPEN status and resolve it")
    void shouldCreateAndResolveInfraction() {
        Infraction infraction = Infraction.create(
                1L,
                100L,
                10L,
                "Unauthorized absence on critical deployment day",
                InfractionSeverity.MEDIUM,
                5L
        );

        assertNotNull(infraction);
        assertEquals(1L, infraction.getId());
        assertEquals(100L, infraction.getEmployeeId());
        assertEquals(10L, infraction.getOrganizationId());
        assertEquals(InfractionSeverity.MEDIUM, infraction.getSeverity());
        assertEquals(InfractionStatus.OPEN, infraction.getStatus());
        assertNull(infraction.getResolutionNotes());
        assertEquals(1, infraction.peekDomainEvents().size());
        assertThat(infraction.peekDomainEvents().getFirst()).isInstanceOf(InfractionLoggedEvent.class);

        infraction.pullDomainEvents();

        infraction.resolve("Employee provided medical documentation; formal warning issued");
        assertEquals(InfractionStatus.RESOLVED, infraction.getStatus());
        assertEquals("Employee provided medical documentation; formal warning issued", infraction.getResolutionNotes());
        assertNotNull(infraction.getResolvedAt());
        assertEquals(1, infraction.peekDomainEvents().size());
        assertThat(infraction.peekDomainEvents().getFirst()).isInstanceOf(InfractionResolvedEvent.class);

        assertThrows(InvalidStaffOperationException.class, () ->
                infraction.resolve("Cannot resolve twice"));
    }

    @Test
    @DisplayName("Should fail Infraction creation on invalid inputs")
    void shouldFailOnInvalidInputs() {
        assertThrows(InvalidStaffOperationException.class, () -> Infraction.create(
                null,
                100L,
                10L,
                "Description",
                InfractionSeverity.LOW,
                5L
        ));

        assertThrows(InvalidStaffOperationException.class, () -> Infraction.create(
                1L,
                100L,
                10L,
                "",
                InfractionSeverity.LOW,
                5L
        ));
    }
}
