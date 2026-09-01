package com.atlashub.eventbus.application.port;

import java.util.Set;

public interface EventTrackerPort {
    Set<String> getExpectedConsumers(String eventType);
    void createPendingTrackers(String eventId, Set<String> consumerIds);
}
