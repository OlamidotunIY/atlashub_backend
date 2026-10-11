package com.atlashub.audit.infrastructure.persistence.adapters;

import com.atlashub.audit.domain.entities.AuditLog;
import com.atlashub.audit.infrastructure.persistence.entities.AuditLogJpa;
import com.atlashub.audit.infrastructure.persistence.mappers.AuditLogMapper;
import com.atlashub.audit.infrastructure.persistence.repositories.SpringDataAuditLogRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogRepositoryAdapterTest {

    @Mock
    private SpringDataAuditLogRepository springDataRepo;

    @Mock
    private AuditLogMapper mapper;

    @Mock
    private DomainSequenceGenerator sequenceGenerator;

    @Mock
    private DomainEventPublisher eventPublisher;

    private AuditLogRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AuditLogRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should generate next identity from sequence")
    void nextIdentity_delegatesToSequenceGenerator() {
        when(sequenceGenerator.nextIdentity("audit_log_seq")).thenReturn(42L);

        Long nextId = adapter.nextIdentity();

        assertEquals(42L, nextId);
        verify(sequenceGenerator).nextIdentity("audit_log_seq");
    }

    @Test
    @DisplayName("Should find by organization ID and map to domain")
    void findByOrganizationId_mapsList() {
        AuditLogJpa jpa = new AuditLogJpa(1L, 10L, 20L, "ACT", "RES", "1", null, null, null, ZonedDateTime.now(), 0L);
        AuditLog domain = AuditLog.create(1L, 10L, 20L, "ACT", "RES", "1", null, null, null, ZonedDateTime.now());

        when(springDataRepo.findByOrganizationIdOrderByTimestampDesc(eq(20L), any(Pageable.class)))
                .thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<AuditLog> results = adapter.findByOrganizationId(20L, 1, 10);

        assertEquals(1, results.size());
        assertEquals(domain, results.get(0));
        verify(springDataRepo).findByOrganizationIdOrderByTimestampDesc(eq(20L), any(Pageable.class));
    }

    @Test
    @DisplayName("Should find by organization ID and resource type")
    void findByOrganizationIdAndResourceType_mapsList() {
        AuditLogJpa jpa = new AuditLogJpa(1L, 10L, 20L, "ACT", "RES", "1", null, null, null, ZonedDateTime.now(), 0L);
        AuditLog domain = AuditLog.create(1L, 10L, 20L, "ACT", "RES", "1", null, null, null, ZonedDateTime.now());

        when(springDataRepo.findByOrganizationIdAndResourceTypeOrderByTimestampDesc(eq(20L), eq("RES"), any(Pageable.class)))
                .thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<AuditLog> results = adapter.findByOrganizationIdAndResourceType(20L, "RES", 1, 10);

        assertEquals(1, results.size());
        assertEquals(domain, results.get(0));
        verify(springDataRepo).findByOrganizationIdAndResourceTypeOrderByTimestampDesc(eq(20L), eq("RES"), any(Pageable.class));
    }

    @Test
    @DisplayName("Should save audit log and return domain")
    void save_persistsAndReturnsDomain() {
        AuditLog domain = AuditLog.create(1L, 10L, 20L, "ACT", "RES", "1", null, null, null, ZonedDateTime.now());
        AuditLogJpa jpa = new AuditLogJpa(1L, 10L, 20L, "ACT", "RES", "1", null, null, null, ZonedDateTime.now(), 0L);

        when(springDataRepo.findById(1L)).thenReturn(Optional.empty());
        when(mapper.toPersistence(domain)).thenReturn(jpa);
        when(springDataRepo.save(jpa)).thenReturn(jpa);
        when(mapper.toDomain(jpa)).thenReturn(domain);

        AuditLog saved = adapter.save(domain);

        assertNotNull(saved);
        assertEquals(domain, saved);
        verify(springDataRepo).save(jpa);
    }
}
