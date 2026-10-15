package com.atlashub.accounting.assets.infrastructure.persistence;

import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.accounting.assets.domain.valueobject.DepreciationMethod;
import com.atlashub.accounting.assets.infrastructure.persistence.adapters.AssetRepositoryAdapter;
import com.atlashub.accounting.assets.infrastructure.persistence.entities.AssetJpaEntity;
import com.atlashub.accounting.assets.infrastructure.persistence.mappers.AssetMapper;
import com.atlashub.accounting.assets.infrastructure.persistence.repositories.SpringDataAssetRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssetRepositoryAdapterTest {

    private SpringDataAssetRepository springDataRepo;
    private AssetMapper mapper;
    private DomainSequenceGenerator sequenceGenerator;
    private DomainEventPublisher eventPublisher;
    private AssetRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        springDataRepo = mock(SpringDataAssetRepository.class);
        mapper = mock(AssetMapper.class);
        sequenceGenerator = mock(DomainSequenceGenerator.class);
        eventPublisher = mock(DomainEventPublisher.class);
        adapter = new AssetRepositoryAdapter(springDataRepo, mapper, sequenceGenerator, eventPublisher);
    }

    @Test
    @DisplayName("Should find assets by organization ID and status")
    void shouldFindByOrgAndStatus() {
        AssetJpaEntity jpa = mock(AssetJpaEntity.class);
        Asset domain = Asset.create(
                1L,
                10L,
                "Desk",
                "AST-DSK-01",
                "Furniture",
                LocalDate.now(),
                Money.of(new BigDecimal("75000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                12,
                DepreciationMethod.STRAIGHT_LINE
        );

        when(springDataRepo.findByOrganizationIdAndStatus(10L, AssetStatus.ACTIVE)).thenReturn(List.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        List<Asset> result = adapter.findByOrganizationIdAndStatus(10L, AssetStatus.ACTIVE);

        assertEquals(1, result.size());
        assertEquals("AST-DSK-01", result.getFirst().getTagNumber());
    }

    @Test
    @DisplayName("Should find asset by tag number")
    void shouldFindByTagNumber() {
        AssetJpaEntity jpa = mock(AssetJpaEntity.class);
        Asset domain = Asset.create(
                1L,
                10L,
                "Desk",
                "AST-DSK-01",
                "Furniture",
                LocalDate.now(),
                Money.of(new BigDecimal("75000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                12,
                DepreciationMethod.STRAIGHT_LINE
        );

        when(springDataRepo.findByOrganizationIdAndTagNumber(10L, "AST-DSK-01")).thenReturn(Optional.of(jpa));
        when(mapper.toDomain(jpa)).thenReturn(domain);

        Optional<Asset> result = adapter.findByOrganizationIdAndTagNumber(10L, "AST-DSK-01");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }
}
