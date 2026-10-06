package com.atlashub.pay.ledger.presentation.rest;

import com.atlashub.pay.ledger.application.commands.CloseAccount.CloseAccountCommand;
import com.atlashub.pay.ledger.application.commands.CloseAccount.CloseAccountHandler;
import com.atlashub.pay.ledger.application.commands.FreezeAccount.FreezeAccountCommand;
import com.atlashub.pay.ledger.application.commands.FreezeAccount.FreezeAccountHandler;
import com.atlashub.pay.ledger.application.commands.UnfreezeAccount.UnfreezeAccountCommand;
import com.atlashub.pay.ledger.application.commands.UnfreezeAccount.UnfreezeAccountHandler;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ledger-accounts")
@Tag(name = "Ledger Administration", description = "Platform ledger account controls")
public class LedgerAccountAdministrationController {
    private final FreezeAccountHandler freezeHandler;
    private final UnfreezeAccountHandler unfreezeHandler;
    private final CloseAccountHandler closeHandler;

    public LedgerAccountAdministrationController(FreezeAccountHandler freezeHandler,
                                                 UnfreezeAccountHandler unfreezeHandler,
                                                 CloseAccountHandler closeHandler) {
        this.freezeHandler = freezeHandler;
        this.unfreezeHandler = unfreezeHandler;
        this.closeHandler = closeHandler;
    }

    @PostMapping("/{accountId}/freeze")
    @Operation(summary = "Freeze a ledger account")
    public ResponseEntity<ApiResponse<Void>> freeze(@AuthenticationPrincipal AuthenticatedPrincipal principal,
                                                    @PathVariable Long accountId) {
        freezeHandler.execute(new FreezeAccountCommand(accountId, principal.userId(), "MANUAL"));
        return done("Ledger account frozen");
    }

    @PostMapping("/{accountId}/unfreeze")
    @Operation(summary = "Unfreeze a ledger account")
    public ResponseEntity<ApiResponse<Void>> unfreeze(@AuthenticationPrincipal AuthenticatedPrincipal principal,
                                                      @PathVariable Long accountId) {
        unfreezeHandler.execute(new UnfreezeAccountCommand(accountId, principal.userId(), "MANUAL"));
        return done("Ledger account unfrozen");
    }

    @PostMapping("/{accountId}/close")
    @Operation(summary = "Close a ledger account")
    public ResponseEntity<ApiResponse<Void>> close(@AuthenticationPrincipal AuthenticatedPrincipal principal,
                                                   @PathVariable Long accountId) {
        closeHandler.execute(new CloseAccountCommand(accountId, principal.userId()));
        return done("Ledger account closed");
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
