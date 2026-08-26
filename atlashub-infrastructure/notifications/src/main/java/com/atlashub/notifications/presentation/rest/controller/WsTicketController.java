package com.atlashub.notifications.presentation.rest.controller;

import com.atlashub.notifications.application.service.WsTicketService;
import com.atlashub.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import com.atlashub.notifications.presentation.rest.response.WsTicketResponseDto;

@RestController
@io.swagger.v3.oas.annotations.tags.Tag(name = "WebSockets", description = "WebSocket ticket generation for realtime events")
@RequestMapping("/api/v1/ws")
public class WsTicketController {

    private final WsTicketService ticketService;

    public WsTicketController(WsTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/ticket")
    public ResponseEntity<ApiResponse<WsTicketResponseDto>> generateTicket(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>(false, "Unauthorized", null, null));
        }
        
        String userId = principal.getName();
        String ticket = ticketService.generateTicket(userId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket generated", new WsTicketResponseDto(ticket), null));
    }
}


