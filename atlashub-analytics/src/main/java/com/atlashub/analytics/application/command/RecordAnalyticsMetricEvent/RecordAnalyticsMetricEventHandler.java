package com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent;

import com.atlashub.analytics.domain.entities.AnalyticsMetricEvent;
import com.atlashub.analytics.domain.repositories.AnalyticsMetricEventRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RecordAnalyticsMetricEventHandler extends Command<RecordAnalyticsMetricEventCommand, Void> {

    private final AnalyticsMetricEventRepository repository;

    public RecordAnalyticsMetricEventHandler(AnalyticsMetricEventRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(RecordAnalyticsMetricEventCommand command) {
        AnalyticsMetricEvent event = AnalyticsMetricEvent.create(
                null,
                command.eventId(),
                command.organizationId(),
                command.environment(),
                command.metricType(),
                command.module(),
                command.amount(),
                command.currency(),
                command.metadata(),
                command.occurredAt()
        );

        repository.save(event);
        return null;
    }
}
