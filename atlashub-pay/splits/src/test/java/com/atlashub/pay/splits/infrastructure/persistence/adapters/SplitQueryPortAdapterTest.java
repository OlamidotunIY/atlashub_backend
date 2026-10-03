package com.atlashub.pay.splits.infrastructure.persistence.adapters;

import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleInactiveException;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleNotFoundException;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SplitQueryPortAdapterTest {

    @Mock
    private SplitRuleRepository repository;

    @InjectMocks
    private SplitQueryPortAdapter adapter;

    @Test
    void shouldReturnActiveRule() {
        SplitRule rule = mock(SplitRule.class);
        when(rule.isActive()).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.of(rule));

        SplitRule result = adapter.findActiveSplitRule(1L);

        assertEquals(rule, result);
    }

    @Test
    void shouldThrowWhenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(SplitRuleNotFoundException.class, () -> adapter.findActiveSplitRule(1L));
    }

    @Test
    void shouldThrowWhenInactive() {
        SplitRule rule = mock(SplitRule.class);
        when(rule.isActive()).thenReturn(false);
        when(repository.findById(1L)).thenReturn(Optional.of(rule));

        assertThrows(SplitRuleInactiveException.class, () -> adapter.findActiveSplitRule(1L));
    }
}
