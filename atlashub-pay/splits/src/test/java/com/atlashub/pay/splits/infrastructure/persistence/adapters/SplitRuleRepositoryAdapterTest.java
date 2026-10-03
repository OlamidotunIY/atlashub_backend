package com.atlashub.pay.splits.infrastructure.persistence.adapters;

import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.infrastructure.persistence.entities.SplitRuleJpa;
import com.atlashub.pay.splits.infrastructure.persistence.mappers.SplitRuleMapper;
import com.atlashub.pay.splits.infrastructure.persistence.repositories.SpringDataSplitRuleRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SplitRuleRepositoryAdapterTest {

    @Mock
    private SpringDataSplitRuleRepository springDataRepo;

    @Mock
    private SplitRuleMapper mapper;

    @Mock
    private DomainSequenceGenerator sequenceGenerator;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private SplitRuleRepositoryAdapter adapter;

    @Test
    void shouldFindAllByOrganizationId() {
        SplitRuleJpa jpa = mock(SplitRuleJpa.class);
        SplitRule rule = mock(SplitRule.class);
        
        when(springDataRepo.findAllByOrganizationId(1L)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(rule);

        List<SplitRule> result = adapter.findAllByOrganizationId(1L);

        assertEquals(1, result.size());
        assertEquals(rule, result.get(0));
    }

    @Test
    void shouldFindByIdAndOrganizationId() {
        SplitRuleJpa jpa = mock(SplitRuleJpa.class);
        SplitRule rule = mock(SplitRule.class);
        
        when(springDataRepo.findByIdAndOrganizationId(1L, 2L)).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(rule);

        Optional<SplitRule> result = adapter.findByIdAndOrganizationId(1L, 2L);

        assertTrue(result.isPresent());
        assertEquals(rule, result.get());
    }
}
