package com.atlashub.hr.attendance.infrastructure.messaging.listeners;

import com.atlashub.hr.attendance.application.command.MarkAttendanceAsLeave.MarkAttendanceAsLeaveCommand;
import com.atlashub.hr.attendance.application.command.MarkAttendanceAsLeave.MarkAttendanceAsLeaveHandler;
import com.atlashub.hr.attendance.infrastructure.messaging.events.LeaveApprovedKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class LeaveApprovedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(LeaveApprovedListener.class);
    private static final String GROUP_ID = "hr-attendance-leave-approved";
    private final MarkAttendanceAsLeaveHandler handler;

    public LeaveApprovedListener(ObjectMapper objectMapper, MarkAttendanceAsLeaveHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("LeaveApprovedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "hr-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(
                payload,
                "LeaveApprovedEvent",
                LeaveApprovedKafkaEvent.class,
                log,
                GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new MarkAttendanceAsLeaveCommand(
                        event.payload().employeeId(),
                        event.payload().organizationId(),
                        event.payload().startDate(),
                        event.payload().endDate()
                ))
        );
    }
}
