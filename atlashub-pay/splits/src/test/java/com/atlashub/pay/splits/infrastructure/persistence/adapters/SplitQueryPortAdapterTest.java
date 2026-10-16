package com.atlashub.pay.splits.infrastructure.persistence.adapters;

import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.entities.SplitSubaccount;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import com.atlashub.pay.splits.domain.valueobject.RecipientType;
import com.atlashub.pay.splits.domain.valueobject.SplitType;
import com.atlashub.shared.application.port.SplitQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SplitQueryPortAdapterTest {

    @Mock
    private SplitRuleRepository repository;

    @InjectMocks
    private SplitQueryPortAdapter adapter;

    @Test
    void shouldReturnActiveRule() {
        SplitRule rule = mock(SplitRule.class);
        when(rule.getId()).thenReturn(1L);
        when(rule.getOrganizationId()).thenReturn(10L);
        when(rule.getName()).thenReturn("Test Split");
        when(rule.getType()).thenReturn(SplitType.PERCENTAGE);
        when(rule.getPlatformFeePercentage()).thenReturn(new BigDecimal("1.50"));
        when(rule.getSubaccounts()).thenReturn(Collections.singletonList(
                new SplitSubaccount(2L, 1L, RecipientType.VENDOR, "ACCT_123", new BigDecimal("98.50"), "Partner")
        ));
        when(rule.isActive()).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.of(rule));

        Optional<SplitQueryPort.SplitRuleDto> result = adapter.findActiveSplitRule(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Split", result.get().name());
        assertEquals(1, result.get().subaccounts().size());
        assertEquals("ACCT_123", result.get().subaccounts().get(0).subaccountCode());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Optional<SplitQueryPort.SplitRuleDto> result = adapter.findActiveSplitRule(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenInactive() {
        SplitRule rule = mock(SplitRule.class);
        when(rule.isActive()).thenReturn(false);
        when(repository.findById(1L)).thenReturn(Optional.of(rule));

        Optional<SplitQueryPort.SplitRuleDto> result = adapter.findActiveSplitRule(1L);

        assertFalse(result.isPresent());
    }
}
