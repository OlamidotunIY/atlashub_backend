package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.DailySalesProjection;
import com.atlashub.analytics.infrastructure.persistence.entities.DailySalesProjectionJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.DailySalesProjectionMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataDailySalesProjectionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DailySalesProjectionRepositoryAdapterTest {

    private SpringDataDailySalesProjectionRepository springDataRepo;
    private DailySalesProjectionMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private DailySalesProjectionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataDailySalesProjectionRepository.class);
        mapper = mock(DailySalesProjectionMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);

        adapter = new DailySalesProjectionRepositoryAdapter(
                springDataRepo,
                mapper,
                sequenceGenerator,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Should find DailySalesProjection by org, env, outlet and date")
    void shouldFindByOrgEnvOutletAndDate() {
        LocalDate date = LocalDate.of(2026, 10, 17);
        DailySalesProjectionJpa jpa = new DailySalesProjectionJpa(
                1L, 100L, "live", 200L, date, 1,
                BigDecimal.valueOf(1000), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(1000), BigDecimal.ZERO,
                BigDecimal.ZERO, ZonedDateTime.now(), 0L
        );
        DailySalesProjection domain = DailySalesProjection.create(1L, 100L, "live", 200L, date);

        when(springDataRepo.findByOrganizationIdAndEnvironmentAndOutletIdAndMetricDate(100L, "live", 200L, date))
                .thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<DailySalesProjection> result = adapter.findByOrgEnvOutletAndDate(100L, "live", 200L, date);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getOrganizationId());
    }

    @Test
    @DisplayName("Should find DailySalesProjection list by date range")
    void shouldFindByDateRange() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 17);
        DailySalesProjectionJpa jpa = new DailySalesProjectionJpa(
                1L, 100L, "live", 200L, from, 1,
                BigDecimal.valueOf(1000), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(1000), BigDecimal.ZERO,
                BigDecimal.ZERO, ZonedDateTime.now(), 0L
        );
        DailySalesProjection domain = DailySalesProjection.create(1L, 100L, "live", 200L, from);

        when(springDataRepo.findByDateRange(100L, "live", 200L, from, to))
                .thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<DailySalesProjection> results = adapter.findByOrgEnvAndDateRange(100L, "live", 200L, from, to);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }
}
