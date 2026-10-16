package com.atlashub.accounting.assets.application.query;

import com.atlashub.accounting.assets.application.query.ListAssets.AssetResult;
import com.atlashub.accounting.assets.application.query.ListAssets.ListAssetsHandler;
import com.atlashub.accounting.assets.application.query.ListAssets.ListAssetsQuery;
import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.repositories.AssetRepository;
import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.accounting.assets.domain.valueobject.DepreciationMethod;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListAssetsHandlerTest {

    private AssetRepository assetRepository;
    private ListAssetsHandler handler;

    @BeforeEach
    void setUp() {
        assetRepository = mock(AssetRepository.class);
        handler = new ListAssetsHandler(assetRepository);
    }

    @Test
    @DisplayName("Should list assets for organization without status filter")
    void shouldListAssetsWithoutStatusFilter() {
        Asset asset = Asset.create(
                1L,
                10L,
                "Office Chair",
                "AST-CHR-01",
                "Furniture",
                LocalDate.now(),
                Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                12,
                DepreciationMethod.STRAIGHT_LINE
        );

        when(assetRepository.findByOrganizationId(10L)).thenReturn(List.of(asset));

        List<AssetResult> results = handler.execute(new ListAssetsQuery(10L, null));

        assertEquals(1, results.size());
        assertEquals("AST-CHR-01", results.getFirst().tagNumber());
    }

    @Test
    @DisplayName("Should list assets for organization with status filter")
    void shouldListAssetsWithStatusFilter() {
        Asset asset = Asset.create(
                1L,
                10L,
                "Office Chair",
                "AST-CHR-01",
                "Furniture",
                LocalDate.now(),
                Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                12,
                DepreciationMethod.STRAIGHT_LINE
        );

        when(assetRepository.findByOrganizationIdAndStatus(10L, AssetStatus.ACTIVE)).thenReturn(List.of(asset));

        List<AssetResult> results = handler.execute(new ListAssetsQuery(10L, AssetStatus.ACTIVE));

        assertEquals(1, results.size());
        assertEquals(AssetStatus.ACTIVE, results.getFirst().status());
    }
}
