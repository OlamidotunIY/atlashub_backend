package com.atlashub.iam.infrastructure.persistence.repositories;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringDataOrganizationMemberRepositoryTest {
    @Test
    void scoped_update_query_uses_pessimistic_write_lock() throws Exception {
        var method = SpringDataOrganizationMemberRepository.class.getMethod(
                "findByIdAndOrganizationIdForUpdate", Long.class, Long.class);
        assertEquals(LockModeType.PESSIMISTIC_WRITE, method.getAnnotation(Lock.class).value());
    }
}
