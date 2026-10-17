package com.atlashub.logistics.warehousing.application.command.RegisterWarehouse;

import com.atlashub.logistics.warehousing.domain.entities.Warehouse;
import com.atlashub.logistics.warehousing.domain.exceptions.DuplicateWarehouseCodeException;
import com.atlashub.logistics.warehousing.domain.repositories.WarehouseRepository;
import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.FacilityType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterWarehouseHandlerTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    private RegisterWarehouseHandler handler;

    private final Address address = new Address("Main St", "Ikeja", "Lagos", "Nigeria", "100001");

    @BeforeEach
    void setUp() {
        handler = new RegisterWarehouseHandler(warehouseRepository);
    }

    @Test
    @DisplayName("Should successfully register a new warehouse")
    void shouldRegisterWarehouse() {
        when(warehouseRepository.findByCode("WH-IKJ-01")).thenReturn(Optional.empty());
        when(warehouseRepository.nextIdentity()).thenReturn(101L);

        RegisterWarehouseCommand command = new RegisterWarehouseCommand(
                100L,
                "Ikeja Central",
                "WH-IKJ-01",
                FacilityType.MERCHANT_WAREHOUSE,
                address,
                10L,
                "corr-reg-1"
        );

        RegisterWarehouseResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(101L, result.warehouseId());
        assertEquals("Ikeja Central", result.name());
        assertEquals("WH-IKJ-01", result.code());
        assertEquals("ACTIVE", result.status());

        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    @DisplayName("Should throw DuplicateWarehouseCodeException when code exists")
    void shouldThrowDuplicateCode() {
        Warehouse existing = Warehouse.create(
                101L, 100L, "Existing", "WH-IKJ-01", FacilityType.MERCHANT_WAREHOUSE, address, 10L, "corr"
        );
        when(warehouseRepository.findByCode("WH-IKJ-01")).thenReturn(Optional.of(existing));

        RegisterWarehouseCommand command = new RegisterWarehouseCommand(
                100L, "Ikeja Central", "WH-IKJ-01", FacilityType.MERCHANT_WAREHOUSE, address, 10L, "corr"
        );

        assertThrows(DuplicateWarehouseCodeException.class, () -> handler.execute(command));
    }
}
