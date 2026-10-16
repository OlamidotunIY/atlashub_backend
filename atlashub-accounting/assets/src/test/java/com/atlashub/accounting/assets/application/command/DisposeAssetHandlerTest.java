package com.atlashub.accounting.assets.application.command;

import com.atlashub.accounting.assets.application.command.DisposeAsset.DisposeAssetCommand;
import com.atlashub.accounting.assets.application.command.DisposeAsset.DisposeAssetHandler;
import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.exceptions.AssetNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DisposeAssetHandlerTest {

    private AssetRepository assetRepository;
    private DisposeAssetHandler handler;

    @BeforeEach
    void setUp() {
        assetRepository = mock(AssetRepository.class);
        handler = new DisposeAssetHandler(assetRepository);
    }

    @Test
    @DisplayName("Should dispose asset successfully")
    void shouldDisposeAsset() {
        Asset asset = Asset.create(
                1L,
                10L,
                "Company Car",
                "AST-CAR-01",
                "Vehicles",
                LocalDate.now().minusYears(3),
                Money.of(new BigDecimal("4000000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                48,
                DepreciationMethod.STRAIGHT_LINE
        );

        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));

        DisposeAssetCommand command = new DisposeAssetCommand(
                1L,
                10L,
                Money.of(new BigDecimal("2500000.00"), CurrencyCode.NGN),
                LocalDate.now()
        );

        handler.execute(command);

        assertEquals(AssetStatus.DISPOSED, asset.getStatus());
        verify(assetRepository).save(asset);
    }

    @Test
    @DisplayName("Should throw AssetNotFoundException when asset does not exist")
    void shouldThrowWhenAssetNotFound() {
        when(assetRepository.findById(999L)).thenReturn(Optional.empty());

        DisposeAssetCommand command = new DisposeAssetCommand(
                999L,
                10L,
                Money.of(new BigDecimal("2500000.00"), CurrencyCode.NGN),
                LocalDate.now()
        );

        assertThrows(AssetNotFoundException.class, () -> handler.execute(command));
    }
}
