package com.atlashub.eventbus.presentation.rest;

import com.atlashub.eventbus.application.command.ReplayEventCommand;
import com.atlashub.eventbus.application.command.ReplayEventsCommand;
import com.atlashub.eventbus.application.usecase.ReplayEventUseCase;
import com.atlashub.eventbus.application.usecase.ReplayEventsUseCase;
import com.atlashub.eventbus.presentation.dto.ReplayEventsRequest;
import com.atlashub.eventbus.presentation.dto.ReplayEventsResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/events")
@Tag(name = "Internal Events", description = "Authorized event recovery operations")
@SecurityRequirement(name = "bearerAuth")
public class EventReplayController {
    private final ReplayEventUseCase replayEventUseCase;
    private final ReplayEventsUseCase replayEventsUseCase;

    public EventReplayController(ReplayEventUseCase replayEventUseCase, ReplayEventsUseCase replayEventsUseCase) {
        this.replayEventUseCase = replayEventUseCase;
        this.replayEventsUseCase = replayEventsUseCase;
    }

    @PostMapping("/{eventId}/replay")
    @Operation(summary = "Replay one failed event")
    public ResponseEntity<ApiResponse<Void>> replayEvent(@PathVariable String eventId) {
        replayEventUseCase.execute(new ReplayEventCommand(eventId));
        return ResponseEntity.ok(new ApiResponse<>(true, "Event replayed successfully", null, null));
    }

    @PostMapping("/replay")
    @Operation(summary = "Replay a bounded event window")
    public ResponseEntity<ApiResponse<ReplayEventsResponse>> replayEvents(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody ReplayEventsRequest request) {
        var result = replayEventsUseCase.execute(new ReplayEventsCommand(request.topic(), request.from(), request.to(),
                request.limit(), request.dryRun(), principal.userId()));
        return ResponseEntity.ok(new ApiResponse<>(true,
                request.dryRun() ? "Replay preview completed" : "Events replayed successfully",
                ReplayEventsResponse.from(result), null));
    }
}
