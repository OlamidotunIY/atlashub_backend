package com.atlashub.accounting.gl.presentation.rest;

import com.atlashub.accounting.gl.application.command.ApproveJournalEntry.ApproveJournalEntryCommand;
import com.atlashub.accounting.gl.application.command.ApproveJournalEntry.ApproveJournalEntryHandler;
import com.atlashub.accounting.gl.application.command.CreateJournalEntry.CreateJournalEntryCommand;
import com.atlashub.accounting.gl.application.command.CreateJournalEntry.CreateJournalEntryHandler;
import com.atlashub.accounting.gl.application.command.CreateJournalEntry.CreateJournalEntryResult;
import com.atlashub.accounting.gl.application.command.CreateJournalEntry.JournalLineInput;
import com.atlashub.accounting.gl.application.command.VoidJournalEntry.VoidJournalEntryCommand;
import com.atlashub.accounting.gl.application.command.VoidJournalEntry.VoidJournalEntryHandler;
import com.atlashub.accounting.gl.application.query.GetJournalEntryById.GetJournalEntryByIdHandler;
import com.atlashub.accounting.gl.application.query.GetJournalEntryById.GetJournalEntryByIdQuery;
import com.atlashub.accounting.gl.application.query.GetJournalEntryById.JournalEntryResult;
import com.atlashub.accounting.gl.application.query.ListJournalEntries.JournalEntrySummaryResult;
import com.atlashub.accounting.gl.application.query.ListJournalEntries.ListJournalEntriesHandler;
import com.atlashub.accounting.gl.application.query.ListJournalEntries.ListJournalEntriesQuery;
import com.atlashub.accounting.gl.domain.valueobject.EntrySource;
import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import com.atlashub.accounting.gl.presentation.dto.CreateJournalEntryRequest;
import com.atlashub.accounting.gl.presentation.dto.CreateJournalEntryResponse;
import com.atlashub.accounting.gl.presentation.dto.JournalEntryResponse;
import com.atlashub.accounting.gl.presentation.dto.JournalLineResponse;
import com.atlashub.accounting.gl.presentation.dto.VoidJournalEntryRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/journal-entries")
@Tag(name = "Journal Entries", description = "General ledger double-entry journal management")
@Validated
public class JournalEntryController {

    private final CreateJournalEntryHandler createJournalEntryHandler;
    private final GetJournalEntryByIdHandler getJournalEntryByIdHandler;
    private final ListJournalEntriesHandler listJournalEntriesHandler;
    private final ApproveJournalEntryHandler approveJournalEntryHandler;
    private final VoidJournalEntryHandler voidJournalEntryHandler;

    public JournalEntryController(CreateJournalEntryHandler createJournalEntryHandler,
                                 GetJournalEntryByIdHandler getJournalEntryByIdHandler,
                                 ListJournalEntriesHandler listJournalEntriesHandler,
                                 ApproveJournalEntryHandler approveJournalEntryHandler,
                                 VoidJournalEntryHandler voidJournalEntryHandler) {
        this.createJournalEntryHandler = createJournalEntryHandler;
        this.getJournalEntryByIdHandler = getJournalEntryByIdHandler;
        this.listJournalEntriesHandler = listJournalEntriesHandler;
        this.approveJournalEntryHandler = approveJournalEntryHandler;
        this.voidJournalEntryHandler = voidJournalEntryHandler;
    }

    @PostMapping
    @Operation(summary = "Record a manual journal entry", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CreateJournalEntryResponse>> recordJournalEntry(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateJournalEntryRequest request) {
        List<JournalLineInput> lines = request.lines().stream()
                .map(line -> new JournalLineInput(
                        line.accountId(),
                        Money.of(line.amount(), CurrencyCode.valueOf(line.currency())),
                        line.type()
                ))
                .toList();

        CreateJournalEntryResult result = createJournalEntryHandler.execute(new CreateJournalEntryCommand(
                principal.activeOrganizationId(),
                request.entryNumber(),
                request.date(),
                request.reference(),
                request.description(),
                EntrySource.MANUAL,
                principal.userId(),
                lines
        ));

        CreateJournalEntryResponse response = new CreateJournalEntryResponse(
                result.entryId(),
                result.entryNumber(),
                result.status(),
                result.lineCount()
        );
        return ok(response);
    }

    @GetMapping(params = "id")
    @Operation(summary = "Get journal entry by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<JournalEntryResponse>> getJournalEntryById(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam @Positive Long id) {
        JournalEntryResult result = getJournalEntryByIdHandler.execute(
                new GetJournalEntryByIdQuery(id, principal.activeOrganizationId()));

        List<JournalLineResponse> lines = result.lines().stream()
                .map(l -> new JournalLineResponse(
                        l.id(),
                        l.accountId(),
                        l.amount().amount(),
                        l.amount().currency().name(),
                        l.type()
                ))
                .toList();

        JournalEntryResponse response = new JournalEntryResponse(
                result.id(),
                result.organizationId(),
                result.entryNumber(),
                result.date(),
                result.reference(),
                result.description(),
                lines,
                result.status(),
                result.source(),
                result.totalDebits().amount(),
                result.totalCredits().amount(),
                result.totalDebits().currency().name(),
                result.initiatedBy(),
                result.approvedBy(),
                result.approvedAt(),
                result.createdAt()
        );
        return ok(response);
    }

    @GetMapping
    @Operation(summary = "List journal entries for organization", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<JournalEntrySummaryResult>>> listJournalEntries(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) JournalEntryStatus status) {
        List<JournalEntrySummaryResult> results = listJournalEntriesHandler.execute(
                new ListJournalEntriesQuery(principal.activeOrganizationId(), status));
        return ok(results);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a pending journal entry", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> approveJournalEntry(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable @Positive Long id) {
        approveJournalEntryHandler.execute(new ApproveJournalEntryCommand(
                id,
                principal.activeOrganizationId(),
                principal.userId()
        ));
        return done("Journal entry approved");
    }

    @PostMapping("/{id}/void")
    @Operation(summary = "Void a posted journal entry", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> voidJournalEntry(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable @Positive Long id,
            @Valid @RequestBody VoidJournalEntryRequest request) {
        voidJournalEntryHandler.execute(new VoidJournalEntryCommand(
                id,
                principal.activeOrganizationId(),
                principal.userId(),
                request.reason()
        ));
        return done("Journal entry voided");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
