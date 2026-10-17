package com.atlashub.analytics.application.command;

import com.atlashub.analytics.application.command.CreateBusinessFeatureSnapshot.CreateBusinessFeatureSnapshotCommand;
import com.atlashub.analytics.application.command.CreateBusinessFeatureSnapshot.CreateBusinessFeatureSnapshotHandler;
import com.atlashub.analytics.application.command.CreateBusinessFeatureSnapshot.CreateBusinessFeatureSnapshotResult;
import com.atlashub.analytics.domain.entities.BusinessFeatureSnapshot;
import com.atlashub.analytics.domain.repositories.BusinessFeatureSnapshotRepository;
import com.atlashub.analytics.domain.valueobject.DataQualityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateBusinessFeatureSnapshotHandlerTest {

    private BusinessFeatureSnapshotRepository repository;
    private CreateBusinessFeatureSnapshotHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(BusinessFeatureSnapshotRepository.class);
        handler = new CreateBusinessFeatureSnapshotHandler(repository);
    }

    @Test
    @DisplayName("Should create snapshot, save to repository, and return result")
    void shouldCreateSnapshot() {
        ZonedDateTime now = ZonedDateTime.now();
        BusinessFeatureSnapshot savedEntity = new BusinessFeatureSnapshot(
                55L,
                100L,
                "live",
                "v1.0",
                now.minusDays(30),
                now,
                now,
                DataQualityStatus.COMPLETE,
                "{}",
                "{}",
                now,
                now.plusDays(90)
        );

        when(repository.save(any(BusinessFeatureSnapshot.class))).thenReturn(savedEntity);

        CreateBusinessFeatureSnapshotCommand command = new CreateBusinessFeatureSnapshotCommand(
                100L,
                "live",
                "v1.0",
                now.minusDays(30),
                now,
                now,
                DataQualityStatus.COMPLETE,
                "{\"revenue\": 1000}",
                "{\"hash\": \"abc\"}",
                now.plusDays(90),
                "corr-123"
        );

        CreateBusinessFeatureSnapshotResult result = handler.execute(command);

        ArgumentCaptor<BusinessFeatureSnapshot> captor = ArgumentCaptor.forClass(BusinessFeatureSnapshot.class);
        verify(repository).save(captor.capture());
        BusinessFeatureSnapshot captured = captor.getValue();

        assertEquals(100L, captured.getOrganizationId());
        assertEquals("v1.0", captured.getSchemaVersion());

        assertNotNull(result);
        assertEquals(55L, result.snapshotId());
        assertEquals(100L, result.organizationId());
        assertEquals("live", result.environment());
        assertEquals("v1.0", result.schemaVersion());
        assertEquals(DataQualityStatus.COMPLETE, result.completeness());
    }
}
