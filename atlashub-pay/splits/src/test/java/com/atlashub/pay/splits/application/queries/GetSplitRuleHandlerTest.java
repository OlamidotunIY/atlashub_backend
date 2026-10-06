package com.atlashub.pay.splits.application.queries;

import com.atlashub.pay.splits.application.queries.GetSplitRule.GetSplitRuleHandler;
import com.atlashub.pay.splits.application.queries.GetSplitRule.GetSplitRuleQuery;
import com.atlashub.pay.splits.application.queries.GetSplitRule.SplitRuleResult;
import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.entities.SplitSubaccount;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleNotFoundException;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import com.atlashub.pay.splits.domain.valueobject.RecipientType;
import com.atlashub.pay.splits.domain.valueobject.SplitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GetSplitRuleHandlerTest {

    private SplitRuleRepository repository;
    private GetSplitRuleHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(SplitRuleRepository.class);
        handler = new GetSplitRuleHandler(repository);
    }

    @Test
    void execute_ShouldReturnResult() {
        // Arrange
        SplitRule rule = new SplitRule(
                100L, 1L, "Test Rule", SplitType.PERCENTAGE, new BigDecimal("10"),
                List.of(new SplitSubaccount(10L, 100L, RecipientType.VENDOR, "v1", new BigDecimal("90"), "desc")),
                true, ZonedDateTime.now(), ZonedDateTime.now()
        );
        when(repository.findByIdAndOrganizationId(100L, 1L)).thenReturn(Optional.of(rule));

        // Act
        SplitRuleResult result = handler.execute(new GetSplitRuleQuery(100L, 1L));

        // Assert
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.name()).isEqualTo("Test Rule");
        assertThat(result.subaccounts()).hasSize(1);
    }

    @Test
    void execute_ShouldThrowNotFound() {
        when(repository.findByIdAndOrganizationId(100L, 1L)).thenReturn(Optional.empty());
        assertThrows(SplitRuleNotFoundException.class, () -> handler.execute(new GetSplitRuleQuery(100L, 1L)));
    }
}
