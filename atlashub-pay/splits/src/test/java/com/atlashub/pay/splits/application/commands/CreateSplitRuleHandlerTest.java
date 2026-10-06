package com.atlashub.pay.splits.application.commands;

import com.atlashub.pay.splits.application.commands.CreateSplitRule.CreateSplitRuleCommand;
import com.atlashub.pay.splits.application.commands.CreateSplitRule.CreateSplitRuleHandler;
import com.atlashub.pay.splits.application.commands.CreateSplitRule.CreateSplitRuleResponse;
import com.atlashub.pay.splits.domain.entities.SplitRule;
import com.atlashub.pay.splits.domain.repositories.SplitRuleRepository;
import com.atlashub.pay.splits.domain.valueobject.RecipientType;
import com.atlashub.pay.splits.domain.valueobject.SplitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CreateSplitRuleHandlerTest {

    private SplitRuleRepository repository;
    private CreateSplitRuleHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(SplitRuleRepository.class);
        handler = new CreateSplitRuleHandler(repository);
    }

    @Test
    void execute_ShouldCreateAndSaveSplitRule() {
        // Arrange
        when(repository.nextIdentity()).thenReturn(100L);
        CreateSplitRuleCommand command = new CreateSplitRuleCommand(
                1L,
                "Test Rule",
                SplitType.PERCENTAGE,
                new BigDecimal("10"),
                List.of(new CreateSplitRuleCommand.SplitSubaccountItem(RecipientType.VENDOR, "v1", new BigDecimal("90"), "desc"))
        );

        // Act
        CreateSplitRuleResponse response = handler.execute(command);

        // Assert
        assertThat(response.splitRuleId()).isEqualTo(100L);
        
        ArgumentCaptor<SplitRule> captor = ArgumentCaptor.forClass(SplitRule.class);
        verify(repository).save(captor.capture());
        SplitRule saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(saved.getOrganizationId()).isEqualTo(1L);
        assertThat(saved.getName()).isEqualTo("Test Rule");
    }
}
