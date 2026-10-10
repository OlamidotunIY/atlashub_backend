package com.atlashub.pay.txquery.infrastructure.persistence.adapters;

import com.atlashub.pay.txquery.domain.entities.TransactionAccountEntry;
import com.atlashub.pay.txquery.domain.entities.TransactionRecord;
import com.atlashub.pay.txquery.domain.valueobject.TransactionDirection;
import com.atlashub.pay.txquery.domain.valueobject.TransactionFilter;
import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.pay.txquery.domain.valueobject.TransactionType;
import com.atlashub.pay.txquery.infrastructure.persistence.entities.TransactionAccountEntryJpa;
import com.atlashub.pay.txquery.infrastructure.persistence.entities.TransactionRecordJpa;
import com.atlashub.pay.txquery.infrastructure.persistence.mappers.TransactionAccountEntryMapperImpl;
import com.atlashub.pay.txquery.infrastructure.persistence.mappers.TransactionRecordMapperImpl;
import com.atlashub.pay.txquery.infrastructure.persistence.repositories.SpringDataTransactionAccountEntryRepository;
import com.atlashub.pay.txquery.infrastructure.persistence.repositories.SpringDataTransactionRecordRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(TransactionRecordRepositoryAdapterIntegrationTest.AdapterConfig.class)
class TransactionRecordRepositoryAdapterIntegrationTest {
    @Autowired private TransactionRecordRepositoryAdapter repository;

    @Test
    void persistsEntriesAndFiltersByAccountDirectionWithRealJpa() {
        ZonedDateTime now = ZonedDateTime.now();
        TransactionRecord record = TransactionRecord.create(100L, 42L, ApiEnvironment.LIVE,
                TransactionType.CHARGE, TransactionStatus.SUCCESSFUL,
                Money.of(new BigDecimal("2500"), CurrencyCode.NGN), "CARD", "PAYSTACK", "CHG-100",
                "COMMERCE", "ORDER-100", "CUSTOMER", "CUST-100", null, null, null,
                "Customer payment", Map.of("order", "ORDER-100"), now);
        record.attachAccountEntries(List.of(
                new TransactionAccountEntry(1001L, 100L, 11L, "OPERATING", "Operating Account", "CREDIT",
                        TransactionDirection.INCOMING, Money.of(new BigDecimal("2500"), CurrencyCode.NGN)),
                new TransactionAccountEntry(1002L, 100L, 12L, "PROVIDER_CLEARING", "Provider Clearing", "DEBIT",
                        TransactionDirection.OUTGOING, Money.of(new BigDecimal("2500"), CurrencyCode.NGN))));
        repository.save(record);

        TransactionFilter filter = new TransactionFilter(42L, ApiEnvironment.LIVE, 11L,
                TransactionType.CHARGE, TransactionStatus.SUCCESSFUL, TransactionDirection.INCOMING,
                "card", "paystack", "commerce", "ORDER-100", "customer", "CUST-100", null,
                "ngn", null, "payment", new BigDecimal("2000"), new BigDecimal("3000"),
                now.minusMinutes(1), now.plusMinutes(1), 0, 20, "desc");
        var result = repository.search(filter);

        assertEquals(1, result.totalElements());
        assertEquals(2, result.content().getFirst().getAccountEntries().size());
        assertTrue(repository.findByOrganizationIdAndEnvironmentAndReference(
                42L, ApiEnvironment.LIVE, "CHG-100").isPresent());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = {TransactionRecordJpa.class, TransactionAccountEntryJpa.class})
    @EnableJpaRepositories(basePackageClasses = {
            SpringDataTransactionRecordRepository.class, SpringDataTransactionAccountEntryRepository.class})
    static class TestApplication {
    }

    @TestConfiguration
    static class AdapterConfig {
        @Bean TransactionRecordMapperImpl transactionRecordMapper() { return new TransactionRecordMapperImpl(); }
        @Bean TransactionAccountEntryMapperImpl transactionAccountEntryMapper() {
            return new TransactionAccountEntryMapperImpl();
        }
        @Bean DomainSequenceGenerator domainSequenceGenerator() { return mock(DomainSequenceGenerator.class); }
        @Bean TransactionRecordRepositoryAdapter adapter(SpringDataTransactionRecordRepository records,
                SpringDataTransactionAccountEntryRepository entries, TransactionRecordMapperImpl recordMapper,
                TransactionAccountEntryMapperImpl entryMapper, DomainSequenceGenerator sequences) {
            return new TransactionRecordRepositoryAdapter(records, entries, recordMapper, entryMapper, sequences);
        }
    }
}
