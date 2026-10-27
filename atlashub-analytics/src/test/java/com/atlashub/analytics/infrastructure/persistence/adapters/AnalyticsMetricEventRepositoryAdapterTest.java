package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.AnalyticsMetricEvent;
import com.atlashub.analytics.infrastructure.persistence.entities.AnalyticsMetricEventJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.AnalyticsMetricEventMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataAnalyticsMetricEventRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalyticsMetricEventRepositoryAdapterTest {
    @Test
    void finds_a_bounded_recent_activity_feed() {
        var repository = mock(SpringDataAnalyticsMetricEventRepository.class);
        var mapper = mock(AnalyticsMetricEventMapper.class);
        var timestamp = ZonedDateTime.parse("2026-10-18T10:00:00Z");
        var record = new AnalyticsMetricEventJpa(1L, "event-1", 9L, "live", "POS_SALE_COMPLETED",
                "commerce", null, "NGN", "{}", timestamp, 0L);
        var domain = AnalyticsMetricEvent.create(1L, "event-1", 9L, "live", "POS_SALE_COMPLETED",
                "commerce", null, "NGN", "{}", timestamp);
        when(repository.findRecent(9L, "live", PageRequest.of(0, 12))).thenReturn(List.of(record));
        when(mapper.toDomain(record)).thenReturn(domain);
        var adapter = new AnalyticsMetricEventRepositoryAdapter(repository, mapper,
                mock(DomainSequenceGenerator.class), mock(DomainEventPublisher.class));

        var result = adapter.findRecent(9L, "live", 12);

        assertEquals("event-1", result.getFirst().getEventId());
        verify(repository).findRecent(9L, "live", PageRequest.of(0, 12));
    }
}
