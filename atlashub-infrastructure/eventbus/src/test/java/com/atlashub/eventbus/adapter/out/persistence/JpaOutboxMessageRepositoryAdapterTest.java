package com.atlashub.eventbus.adapter.out.persistence;

import com.atlashub.eventbus.adapter.out.persistence.entity.OutboxMessageJpaEntity;
import com.atlashub.eventbus.adapter.out.persistence.repository.SpringDataOutboxMessageRepository;
import com.atlashub.eventbus.domain.valueobject.OutboxStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaOutboxMessageRepositoryAdapterTest {
    private final SpringDataOutboxMessageRepository repository = mock(SpringDataOutboxMessageRepository.class);
    private final JpaOutboxMessageRepositoryAdapter adapter = new JpaOutboxMessageRepositoryAdapter(repository);

    @Test
    void maps_bounded_replay_candidates_in_repository_order() {
        ZonedDateTime from = ZonedDateTime.parse("2026-10-01T00:00:00Z");
        ZonedDateTime to = ZonedDateTime.parse("2026-10-02T00:00:00Z");
        var entity = new OutboxMessageJpaEntity("event-1", "commerce.sales", "{}", OutboxStatus.SENT,
                from.plusHours(1), from.plusHours(1), 0);
        when(repository.findReplayCandidates("commerce.sales", from, to, PageRequest.of(0, 25)))
                .thenReturn(List.of(entity));

        var result = adapter.findReplayCandidates("commerce.sales", from, to, 25);

        assertEquals(List.of("event-1"), result.stream().map(message -> message.getId()).toList());
        verify(repository).findReplayCandidates("commerce.sales", from, to, PageRequest.of(0, 25));
    }
}
