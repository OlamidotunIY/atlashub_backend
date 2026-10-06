package com.atlashub.iam.presentation.rest;

import com.atlashub.iam.application.commands.AcceptInvitation.AcceptInvitationCommand;
import com.atlashub.iam.application.commands.AcceptInvitation.AcceptInvitationHandler;
import com.atlashub.iam.application.commands.DeclineInvitation.DeclineInvitationCommand;
import com.atlashub.iam.application.commands.DeclineInvitation.DeclineInvitationHandler;
import com.atlashub.iam.application.commands.InviteMember.InviteMemberCommand;
import com.atlashub.iam.application.commands.InviteMember.InviteMemberHandler;
import com.atlashub.iam.application.commands.RevokeInvitation.RevokeInvitationCommand;
import com.atlashub.iam.application.commands.RevokeInvitation.RevokeInvitationHandler;
import com.atlashub.iam.application.queries.ListInvitations.InvitationResult;
import com.atlashub.iam.application.queries.ListInvitations.ListInvitationsHandler;
import com.atlashub.iam.application.queries.ListInvitations.ListInvitationsQuery;
import com.atlashub.iam.presentation.dto.InvitationTokenRequest;
import com.atlashub.iam.presentation.dto.InviteMemberRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invitations")
@Tag(name = "Invitations", description = "Organization member invitations")
public class InvitationController {
    private final InviteMemberHandler inviteHandler;
    private final ListInvitationsHandler listHandler;
    private final RevokeInvitationHandler revokeHandler;
    private final AcceptInvitationHandler acceptHandler;
    private final DeclineInvitationHandler declineHandler;

    public InvitationController(InviteMemberHandler inviteHandler,
                                ListInvitationsHandler listHandler,
                                RevokeInvitationHandler revokeHandler,
                                AcceptInvitationHandler acceptHandler,
                                DeclineInvitationHandler declineHandler) {
        this.inviteHandler = inviteHandler;
        this.listHandler = listHandler;
        this.revokeHandler = revokeHandler;
        this.acceptHandler = acceptHandler;
        this.declineHandler = declineHandler;
    }

    @PostMapping
    @Operation(summary = "Invite an organization member")
    public ResponseEntity<ApiResponse<Void>> invite(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody InviteMemberRequest request) {
        inviteHandler.execute(new InviteMemberCommand(
                principal.activeOrganizationId(), principal.userId(), request.email(), request.roleId()));
        return done("Invitation sent");
    }

    @GetMapping
    @Operation(summary = "List organization invitations")
    public ResponseEntity<ApiResponse<List<InvitationResult>>> list(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) String status) {
        return ok(listHandler.execute(new ListInvitationsQuery(principal.activeOrganizationId(), status)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel an invitation")
    public ResponseEntity<ApiResponse<Void>> revoke(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long id) {
        revokeHandler.execute(new RevokeInvitationCommand(
                id, principal.userId(), principal.activeOrganizationId()));
        return done("Invitation revoked");
    }

    @PostMapping("/accept")
    @Operation(summary = "Accept an invitation")
    public ResponseEntity<ApiResponse<Void>> accept(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody InvitationTokenRequest request) {
        acceptHandler.execute(new AcceptInvitationCommand(request.token(), principal.userId()));
        return done("Invitation accepted");
    }

    @PostMapping("/decline")
    @Operation(summary = "Decline an invitation")
    public ResponseEntity<ApiResponse<Void>> decline(
            @Valid @RequestBody InvitationTokenRequest request) {
        declineHandler.execute(new DeclineInvitationCommand(request.token()));
        return done("Invitation declined");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
