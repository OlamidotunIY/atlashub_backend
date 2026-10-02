package com.atlashub.iam.presentation.rest;

import com.atlashub.iam.application.commands.AssignRole.AssignRoleCommand;
import com.atlashub.iam.application.commands.AssignRole.AssignRoleHandler;
import com.atlashub.iam.application.commands.DeactivateMember.DeactivateMemberCommand;
import com.atlashub.iam.application.commands.DeactivateMember.DeactivateMemberHandler;
import com.atlashub.iam.application.queries.GetMemberDetails.GetMemberDetailsHandler;
import com.atlashub.iam.application.queries.GetMemberDetails.GetMemberDetailsQuery;
import com.atlashub.iam.application.queries.GetMemberDetails.MemberDetailsResult;
import com.atlashub.iam.application.queries.ListMembers.ListMembersHandler;
import com.atlashub.iam.application.queries.ListMembers.ListMembersQuery;
import com.atlashub.iam.application.queries.ListMembers.MemberResult;
import com.atlashub.iam.presentation.dto.AssignRoleRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/iam/members")
public class OrganizationMemberController {
    private final ListMembersHandler listHandler;
    private final GetMemberDetailsHandler detailsHandler;
    private final AssignRoleHandler assignRoleHandler;
    private final DeactivateMemberHandler deactivateHandler;

    public OrganizationMemberController(ListMembersHandler listHandler,
                                        GetMemberDetailsHandler detailsHandler,
                                        AssignRoleHandler assignRoleHandler,
                                        DeactivateMemberHandler deactivateHandler) {
        this.listHandler = listHandler;
        this.detailsHandler = detailsHandler;
        this.assignRoleHandler = assignRoleHandler;
        this.deactivateHandler = deactivateHandler;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberResult>>> list(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) String status) {
        return ok(listHandler.execute(new ListMembersQuery(principal.activeOrganizationId(), status)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberDetailsResult>> details(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long id) {
        return ok(detailsHandler.execute(new GetMemberDetailsQuery(id, principal.activeOrganizationId())));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AssignRoleRequest request) {
        assignRoleHandler.execute(new AssignRoleCommand(
                id, request.roleId(), principal.userId(), principal.activeOrganizationId()));
        return done("Role assigned");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long id) {
        deactivateHandler.execute(new DeactivateMemberCommand(id, principal.activeOrganizationId()));
        return done("Member deactivated");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
