package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.events.MemberDeactivatedEvent;
import com.atlashub.iam.domain.exception.InvalidOrganizationMemberException;
import com.atlashub.iam.domain.exception.LastOwnerDeactivationException;
import com.atlashub.iam.domain.valueobject.MemberStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationMemberTest {
    @Test
    void deactivation_emits_event_and_last_owner_is_protected() {
        OrganizationMember member = OrganizationMember.create(1L, 2L, 3L, 4L, null);
        member.pullDomainEvents();
        assertThrows(LastOwnerDeactivationException.class, () -> member.deactivate(true));
        member.deactivate(false);
        assertEquals(MemberStatus.INACTIVE, member.getStatus());
        assertInstanceOf(MemberDeactivatedEvent.class, member.pullDomainEvents().getFirst());
    }

    @Test
    void rejects_missing_role() {
        assertThrows(InvalidOrganizationMemberException.class,
                () -> OrganizationMember.create(1L, 2L, 3L, null, null));
    }

    @Test
    void suspension_and_forced_ban_deactivation_emit_access_revocation_events() {
        OrganizationMember member = OrganizationMember.create(1L, 2L, 3L, 4L, null);
        member.pullDomainEvents();
        member.suspend("Subscription overdue");
        assertEquals(MemberStatus.SUSPENDED, member.getStatus());
        assertInstanceOf(MemberDeactivatedEvent.class, member.pullDomainEvents().getFirst());

        member.deactivateForOrganizationBan();
        assertEquals(MemberStatus.INACTIVE, member.getStatus());
        assertInstanceOf(MemberDeactivatedEvent.class, member.pullDomainEvents().getFirst());
    }
}
