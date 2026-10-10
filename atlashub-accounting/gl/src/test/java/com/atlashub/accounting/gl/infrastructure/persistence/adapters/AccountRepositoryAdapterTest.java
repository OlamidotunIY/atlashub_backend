package com.atlashub.accounting.gl.infrastructure.persistence.adapters;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.accounting.gl.domain.valueobject.AccountType;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.AccountJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.mappers.AccountMapper;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataAccountRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountRepositoryAdapterTest {

    private SpringDataAccountRepository springDataRepo;
    private AccountMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private AccountRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataAccountRepository.class);
        mapper = mock(AccountMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new AccountRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    void nextIdentity_delegatesToSequenceGenerator() {
        when(sequenceGenerator.nextIdentity("accounting_account_seq")).thenReturn(1001L);

        Long nextId = adapter.nextIdentity();

        assertEquals(1001L, nextId);
        verify(sequenceGenerator).nextIdentity("accounting_account_seq");
    }

    @Test
    void findByOrganizationIdAndCode_mapsToDomain() {
        AccountJpa jpa = new AccountJpa(1L, 10L, "1000", "Cash", AccountType.ASSET, null, false, true, ZonedDateTime.now(), 0L);
        Account domain = Account.create(1L, 10L, "1000", "Cash", AccountType.ASSET, null, false);

        when(springDataRepo.findByOrganizationIdAndCode(10L, "1000")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Account> result = adapter.findByOrganizationIdAndCode(10L, "1000");

        assertTrue(result.isPresent());
        assertEquals("1000", result.get().getCode());
        verify(springDataRepo).findByOrganizationIdAndCode(10L, "1000");
    }

    @Test
    void findByOrganizationId_mapsList() {
        AccountJpa jpa = new AccountJpa(1L, 10L, "1000", "Cash", AccountType.ASSET, null, false, true, ZonedDateTime.now(), 0L);
        Account domain = Account.create(1L, 10L, "1000", "Cash", AccountType.ASSET, null, false);

        when(springDataRepo.findByOrganizationId(10L)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<Account> result = adapter.findByOrganizationId(10L);

        assertEquals(1, result.size());
        assertEquals("1000", result.get(0).getCode());
    }

    @Test
    void findByOrganizationIdAndIsActiveTrue_mapsList() {
        AccountJpa jpa = new AccountJpa(1L, 10L, "1000", "Cash", AccountType.ASSET, null, false, true, ZonedDateTime.now(), 0L);
        Account domain = Account.create(1L, 10L, "1000", "Cash", AccountType.ASSET, null, false);

        when(springDataRepo.findByOrganizationIdAndIsActiveTrue(10L)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<Account> result = adapter.findByOrganizationIdAndIsActiveTrue(10L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
    }

    @Test
    void save_persistsAndReturnsDomain() {
        Account domain = Account.create(1L, 10L, "1000", "Cash", AccountType.ASSET, null, false);
        AccountJpa jpa = new AccountJpa(1L, 10L, "1000", "Cash", AccountType.ASSET, null, false, true, ZonedDateTime.now(), 0L);

        when(springDataRepo.findById(1L)).thenReturn(Optional.empty());
        when(mapper.toPersistence(domain)).thenReturn(jpa);
        when(springDataRepo.save(jpa)).thenReturn(jpa);
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Account saved = adapter.save(domain);

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
        verify(springDataRepo).save(jpa);
    }
}
