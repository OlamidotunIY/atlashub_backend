package com.atlashub.accounting.gl.infrastructure.persistence.adapters;

import com.atlashub.accounting.gl.domain.entities.BalanceSnapshot;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.BalanceSnapshotJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.mappers.BalanceSnapshotMapper;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataBalanceSnapshotRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BalanceSnapshotRepositoryAdapterTest {

    private SpringDataBalanceSnapshotRepository springDataRepo;
    private BalanceSnapshotMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private BalanceSnapshotRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataBalanceSnapshotRepository.class);
        mapper = mock(BalanceSnapshotMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new BalanceSnapshotRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    void nextIdentity_delegatesToSequenceGenerator() {
        when(sequenceGenerator.nextIdentity("accounting_balance_snapshot_seq")).thenReturn(301L);

        Long nextId = adapter.nextIdentity();

        assertEquals(301L, nextId);
        verify(sequenceGenerator).nextIdentity("accounting_balance_snapshot_seq");
    }

    @Test
    void findLatestByAccountIdAndDate_mapsToDomain() {
        LocalDate date = LocalDate.of(2026, 10, 1);
        BalanceSnapshotJpa jpa = new BalanceSnapshotJpa(
                1L, 10L, date, new BigDecimal("1000.00"), "NGN", ZonedDateTime.now(), 0L);
        BalanceSnapshot domain = BalanceSnapshot.create(
                1L, 10L, date, Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN));

        when(springDataRepo.findTopByAccountIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(10L, date))
                .thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<BalanceSnapshot> result = adapter.findLatestByAccountIdAndDate(10L, date);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(springDataRepo).findTopByAccountIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(10L, date);
    }
}
