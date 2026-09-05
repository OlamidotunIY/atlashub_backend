package com.atlashub.identity.adapter.in.web.controller;

import com.atlashub.identity.application.command.AcceptInvitationCommand;
import com.atlashub.identity.application.command.DeclineInvitationCommand;
import com.atlashub.identity.application.command.SendInvitationCommand;
import com.atlashub.identity.application.query.GetInvitationByTokenQuery;
import com.atlashub.identity.application.result.InvitationDto;
import com.atlashub.identity.application.usecase.AcceptInvitationUseCase;
import com.atlashub.identity.application.usecase.DeclineInvitationUseCase;
import com.atlashub.identity.application.port.InvitationQueryService;
import com.atlashub.identity.application.usecase.SendInvitationUseCase;
import com.atlashub.identity.domain.valueobject.OrganizationRole;
import com.atlashub.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Invitations", description = "Organization invitation management")
public class InvitationController {

    private final SendInvitationUseCase sendInvitationUseCase;
    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final DeclineInvitationUseCase declineInvitationUseCase;
    private final InvitationQueryService queryService;

    public InvitationController(
            SendInvitationUseCase sendInvitationUseCase,
            AcceptInvitationUseCase acceptInvitationUseCase,
            DeclineInvitationUseCase declineInvitationUseCase,
            InvitationQueryService queryService) {
        this.sendInvitationUseCase = sendInvitationUseCase;
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.declineInvitationUseCase = declineInvitationUseCase;
        this.queryService = queryService;
    }

    @PostMapping("/organizations/{organizationId}/invitations")
    @Operation(summary = "Send an invitation")
    public ResponseEntity<ApiResponse<Void>> sendInvitation(
            @PathVariable Long organizationId,
            @RequestBody com.atlashub.identity.adapter.in.web.request.SendInvitationRequestDto request,
            Principal principal) {
        Long inviterId = Long.valueOf(principal.getName());
        sendInvitationUseCase.execute(new SendInvitationCommand(organizationId, request.email(), request.role(), inviterId));
        return ResponseEntity.ok(new ApiResponse<>(true, "Invitation sent", null, null));
    }

    @GetMapping("/invitations/{token}")
    @Operation(summary = "Get invitation details by token")
    public ResponseEntity<ApiResponse<InvitationDto>> getInvitation(@PathVariable String token) {
        InvitationDto dto = queryService.getInvitationByToken(token).orElseThrow();
        return ResponseEntity.ok(new ApiResponse<>(true, "Invitation retrieved", dto, null));
    }

    @PostMapping("/invitations/{token}/accept")
    @Operation(summary = "Accept an invitation (Existing User)")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(@PathVariable String token, Principal principal) {
        Long acceptingUserId = Long.valueOf(principal.getName());
        acceptInvitationUseCase.execute(new AcceptInvitationCommand(token, acceptingUserId));
        return ResponseEntity.ok(new ApiResponse<>(true, "Invitation accepted", null, null));
    }

    @PostMapping("/invitations/{token}/decline")
    @Operation(summary = "Decline an invitation")
    public ResponseEntity<ApiResponse<Void>> declineInvitation(@PathVariable String token) {
        declineInvitationUseCase.execute(new DeclineInvitationCommand(token));
        return ResponseEntity.ok(new ApiResponse<>(true, "Invitation declined", null, null));
    }
}
