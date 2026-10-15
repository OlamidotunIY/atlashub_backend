package com.atlashub.accounting.assets.application.command;

import com.atlashub.accounting.assets.application.command.RegisterAsset.RegisterAssetCommand;
import com.atlashub.accounting.assets.application.command.RegisterAsset.RegisterAssetHandler;
import com.atlashub.accounting.assets.domain.entities.Asset;
import com.atlashub.accounting.assets.domain.exceptions.InvalidAssetOperationException;
import com.atlashub.accounting.assets.domain.repositories.AssetRepository;
import com.atlashub.accounting.assets.domain.valueobject.DepreciationMethod;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterAssetHandlerTest {

    private AssetRepository assetRepository;
    private RegisterAssetHandler handler;

    @BeforeEach
    void setUp() {
        assetRepository = mock(AssetRepository.class);
        handler = new RegisterAssetHandler(assetRepository);
    }

    @Test
    @DisplayName("Should register new asset successfully")
    void shouldRegisterAssetSuccessfully() {
        RegisterAssetCommand command = new RegisterAssetCommand(
                10L,
                "Dell Server",
                "AST-SRV-01",
                "Servers",
                LocalDate.now(),
                Money.of(new BigDecimal("3000000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("300000.00"), CurrencyCode.NGN),
                36,
                DepreciationMethod.STRAIGHT_LINE
        );

        when(assetRepository.findByOrganizationIdAndTagNumber(10L, "AST-SRV-01")).thenReturn(Optional.empty());
        when(assetRepository.nextIdentity()).thenReturn(501L);
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Long assetId = handler.execute(command);

        assertEquals(501L, assetId);
        ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository).save(captor.capture());

        Asset saved = captor.getValue();
        assertEquals(501L, saved.getId());
        assertEquals("Dell Server", saved.getName());
        assertEquals("AST-SRV-01", saved.getTagNumber());
    }

    @Test
    @DisplayName("Should throw exception when asset tag already exists")
    void shouldThrowExceptionWhenTagExists() {
        RegisterAssetCommand command = new RegisterAssetCommand(
                10L,
                "Dell Server",
                "AST-SRV-01",
                "Servers",
                LocalDate.now(),
                Money.of(new BigDecimal("3000000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("300000.00"), CurrencyCode.NGN),
                36,
                DepreciationMethod.STRAIGHT_LINE
        );

        Asset existing = mock(Asset.class);
        when(assetRepository.findByOrganizationIdAndTagNumber(10L, "AST-SRV-01")).thenReturn(Optional.of(existing));

        assertThrows(InvalidAssetOperationException.class, () -> handler.execute(command));
    }
}
