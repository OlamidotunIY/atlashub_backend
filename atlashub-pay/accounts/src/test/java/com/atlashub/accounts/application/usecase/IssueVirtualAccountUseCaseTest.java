package com.atlashub.accounts.application.usecase;

import com.atlashub.accounts.application.command.IssueVirtualAccountCommand;
import com.atlashub.accounts.application.port.VirtualAccountQueryService;
import com.atlashub.accounts.domain.model.VirtualAccount;
import com.atlashub.accounts.domain.repository.VirtualAccountDomainRepository;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IssueVirtualAccountUseCaseTest {

    private VirtualAccountDomainRepository repository;
    private VirtualAccountQueryService queryService;
    private DomainEventPublisher eventPublisher;
    private IssueVirtualAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(VirtualAccountDomainRepository.class);
        queryService = mock(VirtualAccountQueryService.class);
        eventPublisher = mock(DomainEventPublisher.class);
        useCase = new IssueVirtualAccountUseCase(repository, queryService, eventPublisher);
    }

    @Test
    void shouldThrowExceptionWhenMerchantHasTwoAccounts() {
        when(queryService.countByIntegration(1L)).thenReturn(2);
        
        IssueVirtualAccountCommand cmd = new IssueVirtualAccountCommand(1L, "CUST-1", "Test Account", "Wema", com.atlashub.shared.money.CurrencyCode.NGN, "idem1");
        
        assertThrows(BusinessRuleException.class, () -> useCase.execute(cmd));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenMerchantHasDuplicateBank() {
        when(queryService.countByIntegration(1L)).thenReturn(1);
        when(queryService.existsByIntegrationAndBankName(1L, "Wema")).thenReturn(true);
        
        IssueVirtualAccountCommand cmd = new IssueVirtualAccountCommand(1L, "CUST-1", "Test Account", "Wema", com.atlashub.shared.money.CurrencyCode.NGN, "idem2");
        
        assertThrows(BusinessRuleException.class, () -> useCase.execute(cmd));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldSaveAccountWhenValid() {
        when(queryService.countByIntegration(1L)).thenReturn(0);
        when(queryService.existsByIntegrationAndBankName(any(), any())).thenReturn(false);
        when(repository.save(any(VirtualAccount.class))).thenAnswer(i -> i.getArguments()[0]);
        
        IssueVirtualAccountCommand cmd = new IssueVirtualAccountCommand(1L, "CUST-1", "Test Account", "Zenith", com.atlashub.shared.money.CurrencyCode.NGN, "idem3");
        
        useCase.execute(cmd);
        
        verify(repository, times(1)).save(any(VirtualAccount.class));
    }
}
