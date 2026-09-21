package com.atlashub.eventbus.adapter.in.web.controller;

import com.atlashub.eventbus.application.command.ReplayEventCommand;
import com.atlashub.eventbus.application.usecase.ReplayEventUseCase;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.annotation.PublicEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/events")
public class EventReplayController {

    private final ReplayEventUseCase replayEventUseCase;

    public EventReplayController(ReplayEventUseCase replayEventUseCase) {
        this.replayEventUseCase = replayEventUseCase;
    }

    @PublicEndpoint
    @PostMapping("/{eventId}/replay")
    public ResponseEntity<ApiResponse<Void>> replayEvent(@PathVariable String eventId) {
        replayEventUseCase.execute(new ReplayEventCommand(eventId));
        return ResponseEntity.ok(new ApiResponse<>(true, "Event replayed successfully", null, null));
    }
}
