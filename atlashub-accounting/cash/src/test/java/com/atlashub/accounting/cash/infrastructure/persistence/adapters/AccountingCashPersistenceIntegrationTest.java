package com.atlashub.accounting.cash.infrastructure.persistence.adapters;

import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.entities.BankStatementLine;
import com.atlashub.accounting.cash.domain.entities.CashEvacuation;
import com.atlashub.accounting.cash.domain.valueobject.EvacuationStatus;
import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.accounting.cash.domain.valueobject.StatementEntryType;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.BankReconciliationJpaEntity;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.BankStatementLineJpaEntity;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.CashEvacuationJpaEntity;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.ReconciliationItemJpaEntity;
import com.atlashub.accounting.cash.infrastructure.persistence.mappers.BankReconciliationMapper;
import com.atlashub.accounting.cash.infrastructure.persistence.mappers.BankReconciliationMapperImpl;
import com.atlashub.accounting.cash.infrastructure.persistence.mappers.BankStatementLineMapper;
import com.atlashub.accounting.cash.infrastructure.persistence.mappers.BankStatementLineMapperImpl;
import com.atlashub.accounting.cash.infrastructure.persistence.mappers.CashEvacuationMapper;
import com.atlashub.accounting.cash.infrastructure.persistence.mappers.CashEvacuationMapperImpl;
import com.atlashub.accounting.cash.infrastructure.persistence.mappers.ReconciliationItemMapper;
import com.atlashub.accounting.cash.infrastructure.persistence.mappers.ReconciliationItemMapperImpl;
import com.atlashub.accounting.cash.infrastructure.persistence.repositories.SpringDataBankReconciliationRepository;
import com.atlashub.accounting.cash.infrastructure.persistence.repositories.SpringDataBankStatementLineRepository;
import com.atlashub.accounting.cash.infrastructure.persistence.repositories.SpringDataCashEvacuationRepository;
import com.atlashub.accounting.cash.infrastructure.persistence.repositories.SpringDataReconciliationItemRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(AccountingCashPersistenceIntegrationTest.AdapterConfig.class)
class AccountingCashPersistenceIntegrationTest {

    @Autowired private CashEvacuationRepositoryAdapter cashEvacuationRepository;
    @Autowired private BankReconciliationRepositoryAdapter bankReconciliationRepository;
    @Autowired private SpringDataBankStatementLineRepository statementLineRepository;
    @Autowired private DomainSequenceGenerator sequenceGenerator;
    @Autowired private DomainEventPublisher eventPublisher;

    @BeforeEach
    void resetMocks() {
        clearInvocations(sequenceGenerator, eventPublisher);
    }

    @Test
    void persistsCashEvacuationAndPublishesConfirmationOnce() {
        when(sequenceGenerator.nextIdentity("accounting_cash_evacuation_seq")).thenReturn(101L);
        assertEquals(101L, cashEvacuationRepository.nextIdentity());

        CashEvacuation evacuation = CashEvacuation.create(
                101L, 42L, 7L, 9L, Money.of("125000", CurrencyCode.NGN), 33L);
        cashEvacuationRepository.save(evacuation);
        evacuation.confirm();
        cashEvacuationRepository.save(evacuation);

        CashEvacuation restored = cashEvacuationRepository.findById(101L).orElseThrow();
        assertEquals(EvacuationStatus.CONFIRMED, restored.getStatus());
        assertEquals(Money.of("125000", CurrencyCode.NGN), restored.getAmount());
        assertEquals(1, cashEvacuationRepository
                .findByOrganizationIdAndStatus(42L, EvacuationStatus.CONFIRMED).size());
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    void persistsCompleteReconciliationAndRestoresMatchedAndUnmatchedItems() {
        when(sequenceGenerator.nextIdentity("accounting_reconciliation_seq")).thenReturn(201L);
        assertEquals(201L, bankReconciliationRepository.nextIdentity());

        BankReconciliation reconciliation = BankReconciliation.create(
                201L,
                42L,
                "OPERATING-NGN",
                LocalDate.of(2026, 10, 10),
                Money.of("15000", CurrencyCode.NGN),
                Money.of("14000", CurrencyCode.NGN),
                List.of(
                        BankStatementLine.create(301L, 201L, LocalDate.of(2026, 10, 9),
                                "Customer payment", Money.of("10000", CurrencyCode.NGN),
                                StatementEntryType.CREDIT),
                        BankStatementLine.create(302L, 201L, LocalDate.of(2026, 10, 10),
                                "Bank charge", Money.of("500", CurrencyCode.NGN),
                                StatementEntryType.DEBIT)
                ),
                List.of(401L, 402L)
        );
        bankReconciliationRepository.save(reconciliation);

        reconciliation.matchItem(501L, 301L, 401L);
        bankReconciliationRepository.save(reconciliation);

        BankReconciliation restored = bankReconciliationRepository.findById(201L).orElseThrow();
        assertEquals(1, restored.getMatchedItems().size());
        assertEquals(301L, restored.getMatchedItems().getFirst().getBankStatementLineId());
        assertEquals(List.of(402L), restored.getUnmatchedBookItems());
        assertEquals(1, restored.getUnmatchedBankItems().size());
        assertEquals(302L, restored.getUnmatchedBankItems().getFirst().getId());
        assertEquals(2, statementLineRepository.findByReconciliationIdOrderById(201L).size());

        restored.finalizeReconciliation(true);
        bankReconciliationRepository.save(restored);

        assertEquals(ReconciliationStatus.RECONCILED,
                bankReconciliationRepository.findById(201L).orElseThrow().getStatus());
        assertEquals(1, bankReconciliationRepository
                .findByOrganizationIdAndStatus(42L, ReconciliationStatus.RECONCILED).size());
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    void deletesReconciliationAndItsChildRecords() {
        BankReconciliation reconciliation = BankReconciliation.create(
                202L,
                43L,
                "OPERATING-NGN",
                LocalDate.of(2026, 10, 10),
                Money.of("500", CurrencyCode.NGN),
                Money.of("500", CurrencyCode.NGN),
                List.of(BankStatementLine.create(303L, 202L, LocalDate.of(2026, 10, 10),
                        "Deposit", Money.of("500", CurrencyCode.NGN), StatementEntryType.CREDIT)),
                List.of(403L)
        );
        bankReconciliationRepository.save(reconciliation);

        bankReconciliationRepository.deleteById(202L);

        assertTrue(bankReconciliationRepository.findById(202L).isEmpty());
        assertTrue(statementLineRepository.findByReconciliationIdOrderById(202L).isEmpty());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {
            CashEvacuationJpaEntity.class,
            BankReconciliationJpaEntity.class,
            BankStatementLineJpaEntity.class,
            ReconciliationItemJpaEntity.class
    })
    @EnableJpaRepositories(basePackageClasses = {
            SpringDataCashEvacuationRepository.class,
            SpringDataBankReconciliationRepository.class,
            SpringDataBankStatementLineRepository.class,
            SpringDataReconciliationItemRepository.class
    })
    static class TestApplication {
    }

    @TestConfiguration
    static class AdapterConfig {
        @Bean CashEvacuationMapper cashEvacuationMapper() { return new CashEvacuationMapperImpl(); }
        @Bean BankReconciliationMapper bankReconciliationMapper() { return new BankReconciliationMapperImpl(); }
        @Bean BankStatementLineMapper bankStatementLineMapper() { return new BankStatementLineMapperImpl(); }
        @Bean ReconciliationItemMapper reconciliationItemMapper() { return new ReconciliationItemMapperImpl(); }
        @Bean DomainSequenceGenerator domainSequenceGenerator() { return mock(DomainSequenceGenerator.class); }
        @Bean DomainEventPublisher domainEventPublisher() { return mock(DomainEventPublisher.class); }

        @Bean
        CashEvacuationRepositoryAdapter cashEvacuationRepositoryAdapter(
                SpringDataCashEvacuationRepository repository,
                CashEvacuationMapper mapper,
                DomainSequenceGenerator sequenceGenerator,
                DomainEventPublisher eventPublisher) {
            return new CashEvacuationRepositoryAdapter(repository, mapper, sequenceGenerator, eventPublisher);
        }

        @Bean
        BankReconciliationRepositoryAdapter bankReconciliationRepositoryAdapter(
                SpringDataBankReconciliationRepository reconciliationRepository,
                SpringDataBankStatementLineRepository statementLineRepository,
                SpringDataReconciliationItemRepository reconciliationItemRepository,
                BankReconciliationMapper reconciliationMapper,
                BankStatementLineMapper statementLineMapper,
                ReconciliationItemMapper reconciliationItemMapper,
                DomainSequenceGenerator sequenceGenerator,
                DomainEventPublisher eventPublisher) {
            return new BankReconciliationRepositoryAdapter(
                    reconciliationRepository,
                    statementLineRepository,
                    reconciliationItemRepository,
                    reconciliationMapper,
                    statementLineMapper,
                    reconciliationItemMapper,
                    sequenceGenerator,
                    eventPublisher
            );
        }
    }
}
