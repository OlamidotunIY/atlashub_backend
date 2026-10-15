package com.atlashub.accounting.budget.infrastructure.persistence;

import com.atlashub.accounting.budget.domain.entities.Budget;
import com.atlashub.accounting.budget.domain.valueobject.BudgetStatus;
import com.atlashub.accounting.budget.infrastructure.persistence.adapters.BudgetRepositoryAdapter;
import com.atlashub.accounting.budget.infrastructure.persistence.entities.BudgetJpaEntity;
import com.atlashub.accounting.budget.infrastructure.persistence.mappers.BudgetMapper;
import com.atlashub.accounting.budget.infrastructure.persistence.repositories.SpringDataBudgetRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BudgetRepositoryAdapterTest {

    private SpringDataBudgetRepository springDataRepo;
    private BudgetMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private BudgetRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataBudgetRepository.class);
        mapper = mock(BudgetMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new BudgetRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should find budgets by organization ID and status")
    void shouldFindByOrgAndStatus() {
        BudgetJpaEntity jpa = mock(BudgetJpaEntity.class);
        Budget domain = Budget.create(
                1L,
                10L,
                1001L,
                "Sales & Operations",
                "2026-Q1",
                Money.of(new BigDecimal("3000000.00"), CurrencyCode.NGN),
                20L
        );

        when(springDataRepo.findByOrganizationIdAndStatus(10L, BudgetStatus.DRAFT)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<Budget> result = adapter.findByOrganizationIdAndStatus(10L, BudgetStatus.DRAFT);

        assertEquals(1, result.size());
        assertEquals("Sales & Operations", result.getFirst().getName());
    }

    @Test
    @DisplayName("Should find budget by org, account and period")
    void shouldFindByOrgAccountAndPeriod() {
        BudgetJpaEntity jpa = mock(BudgetJpaEntity.class);
        Budget domain = Budget.create(
                1L,
                10L,
                1001L,
                "Sales & Operations",
                "2026-Q1",
                Money.of(new BigDecimal("3000000.00"), CurrencyCode.NGN),
                20L
        );

        when(springDataRepo.findByOrganizationIdAndAccountIdAndPeriod(10L, 1001L, "2026-Q1"))
                .thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Budget> result = adapter.findByOrganizationIdAndAccountIdAndPeriod(10L, 1001L, "2026-Q1");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }
}
