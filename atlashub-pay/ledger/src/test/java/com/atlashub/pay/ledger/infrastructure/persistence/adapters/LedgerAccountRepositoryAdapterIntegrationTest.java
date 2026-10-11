package com.atlashub.pay.ledger.infrastructure.persistence.adapters;

import com.atlashub.pay.ledger.domain.valueobject.LedgerRestrictionType;
import com.atlashub.pay.ledger.infrastructure.persistence.entities.LedgerAccountJpa;
import com.atlashub.pay.ledger.infrastructure.persistence.mappers.LedgerAccountMapper;
import com.atlashub.pay.ledger.infrastructure.persistence.mappers.LedgerAccountMapperImpl;
import com.atlashub.pay.ledger.infrastructure.persistence.repositories.SpringDataLedgerAccountRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.ZonedDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false"
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(LedgerAccountRepositoryAdapterIntegrationTest.AdapterConfig.class)
class LedgerAccountRepositoryAdapterIntegrationTest {

    private static final Long ACCOUNT_ID = 1001L;
    private static final Long ORGANIZATION_ID = 42L;

    @Autowired private SpringDataLedgerAccountRepository springDataRepository;
    @Autowired private LedgerAccountRepositoryAdapter repository;
    @Autowired private PlatformTransactionManager transactionManager;

    @BeforeEach
    void persistRestrictedAccount() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.executeWithoutResult(status -> {
            springDataRepository.deleteAll();
            springDataRepository.flush();
            springDataRepository.saveAndFlush(new LedgerAccountJpa(
                ACCOUNT_ID,
                ORGANIZATION_ID,
                ApiEnvironment.LIVE,
                "OPERATING",
                "Operating Account",
                null,
                null,
                null,
                "NGN",
                "CREDIT",
                "FROZEN",
                Set.of(LedgerRestrictionType.COMPLIANCE, LedgerRestrictionType.RISK),
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                null
            ));
        });
    }

    @Test
    void mapsRestrictionsForOrganizationAccountListAfterPersistenceSessionCloses() {
        var accounts = repository.findAllByOrganizationIdAndEnvironment(ORGANIZATION_ID, ApiEnvironment.LIVE);

        assertEquals(1, accounts.size());
        assertEquals(Set.of(LedgerRestrictionType.COMPLIANCE, LedgerRestrictionType.RISK),
                accounts.getFirst().getActiveRestrictions());
    }

    @Test
    void mapsRestrictionsForSingleAccountAfterPersistenceSessionCloses() {
        var account = repository.findById(ACCOUNT_ID).orElseThrow();

        assertTrue(account.getActiveRestrictions().contains(LedgerRestrictionType.COMPLIANCE));
        assertTrue(account.getActiveRestrictions().contains(LedgerRestrictionType.RISK));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = LedgerAccountJpa.class)
    @EnableJpaRepositories(basePackageClasses = SpringDataLedgerAccountRepository.class)
    static class TestApplication {
    }

    @TestConfiguration
    static class AdapterConfig {
        @Bean ValueObjectMapper valueObjectMapper() { return new ValueObjectMapper(); }
        @Bean LedgerAccountMapper ledgerAccountMapper() { return new LedgerAccountMapperImpl(); }
        @Bean DomainSequenceGenerator domainSequenceGenerator() { return mock(DomainSequenceGenerator.class); }
        @Bean DomainEventPublisher domainEventPublisher() { return mock(DomainEventPublisher.class); }

        @Bean
        LedgerAccountRepositoryAdapter ledgerAccountRepositoryAdapter(
                SpringDataLedgerAccountRepository repository,
                LedgerAccountMapper mapper,
                DomainSequenceGenerator sequenceGenerator,
                DomainEventPublisher eventPublisher) {
            return new LedgerAccountRepositoryAdapter(repository, mapper, sequenceGenerator, eventPublisher);
        }
    }
}
