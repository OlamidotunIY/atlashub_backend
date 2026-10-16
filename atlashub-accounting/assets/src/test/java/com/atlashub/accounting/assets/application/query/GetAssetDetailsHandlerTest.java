package com.atlashub.accounting.assets.application.query;

import com.atlashub.accounting.assets.application.query.GetAssetDetails.GetAssetDetailsHandler;
import com.atlashub.accounting.assets.application.query.GetAssetDetails.GetAssetDetailsQuery;
import com.atlashub.accounting.assets.application.query.ListAssets.AssetResult;
import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.exceptions.AssetNotFoundException;
import com.atlashub.accounting.assets.domain.repositories.AssetRepository;
import com.atlashub.accounting.assets.domain.valueobject.DepreciationMethod;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAssetDetailsHandlerTest {

    private AssetRepository assetRepository;
    private GetAssetDetailsHandler handler;

    @BeforeEach
    void setUp() {
        assetRepository = mock(AssetRepository.class);
        handler = new GetAssetDetailsHandler(assetRepository);
    }

    @Test
    @DisplayName("Should return asset details when asset exists")
    void shouldReturnAssetDetails() {
        Asset asset = Asset.create(
                1L,
                10L,
                "Monitor",
                "AST-MON-01",
                "IT",
                LocalDate.now(),
                Money.of(new BigDecimal("150000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                24,
                DepreciationMethod.STRAIGHT_LINE
        );

        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));

        AssetResult result = handler.execute(new GetAssetDetailsQuery(1L, 10L));

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("AST-MON-01", result.tagNumber());
    }

    @Test
    @DisplayName("Should throw AssetNotFoundException when asset does not exist or org mismatch")
    void shouldThrowWhenNotFound() {
        when(assetRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AssetNotFoundException.class, () -> handler.execute(new GetAssetDetailsQuery(1L, 10L)));
    }
}
