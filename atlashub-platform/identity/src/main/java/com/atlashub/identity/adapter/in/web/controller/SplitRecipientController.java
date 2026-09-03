package com.atlashub.identity.adapter.in.web.controller;

import com.atlashub.identity.application.command.RegisterSplitRecipientCommand;
import com.atlashub.identity.application.result.RegisterSplitRecipientResult;
import com.atlashub.identity.application.result.SplitRecipientDto;
import com.atlashub.identity.application.query.GetSplitRecipientQuery;
import com.atlashub.identity.application.query.ListSplitRecipientsQuery;
import com.atlashub.identity.application.usecase.GetSplitRecipientUseCase;
import com.atlashub.identity.application.usecase.ListSplitRecipientsUseCase;
import com.atlashub.identity.application.usecase.RegisterSplitRecipientUseCase;
import com.atlashub.identity.adapter.in.web.request.RegisterSplitRecipientRequest;
import com.atlashub.shared.application.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import com.atlashub.shared.application.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/SplitRecipients")
@Tag(name = "Sub Accounts", description = "Sub Account management")
public class SplitRecipientController {

    private final RegisterSplitRecipientUseCase registerSplitRecipientUseCase;
    private final GetSplitRecipientUseCase getSplitRecipientUseCase;
    private final ListSplitRecipientsUseCase listSplitRecipientsUseCase;

    public SplitRecipientController(RegisterSplitRecipientUseCase registerSplitRecipientUseCase,
                                GetSplitRecipientUseCase getSplitRecipientUseCase,
                                ListSplitRecipientsUseCase listSplitRecipientsUseCase) {
        this.registerSplitRecipientUseCase = registerSplitRecipientUseCase;
        this.getSplitRecipientUseCase = getSplitRecipientUseCase;
        this.listSplitRecipientsUseCase = listSplitRecipientsUseCase;
    }

    @PostMapping
    @Operation(summary = "Register a sub account", description = "Registers a new sub account for a Organization")
    public ResponseEntity<ApiResponse<RegisterSplitRecipientResult>> register(@Valid @RequestBody RegisterSplitRecipientRequest request, Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        RegisterSplitRecipientCommand command = new RegisterSplitRecipientCommand(
                Long.valueOf(OrganizationIdStr),
                request.bankCode(),
                request.accountNumber(),
                request.description()
        );
        RegisterSplitRecipientResult result = registerSplitRecipientUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Sub account created successfully", result, null));
    }
    
    @GetMapping("/{SplitRecipientId}")
    @Operation(summary = "Get a sub account", description = "Retrieves a sub account by ID")
    public ResponseEntity<ApiResponse<SplitRecipientDto>> get(@PathVariable String SplitRecipientId, Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        GetSplitRecipientQuery query = new GetSplitRecipientQuery(Long.valueOf(OrganizationIdStr), Long.valueOf(SplitRecipientId));
        SplitRecipientDto result = getSplitRecipientUseCase.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sub account retrieved successfully", result, null));
    }
    
    @GetMapping
    @Operation(summary = "List sub accounts", description = "Retrieves a paginated list of sub accounts")
    public ResponseEntity<ApiResponse<List<SplitRecipientDto>>> list(
            @org.springframework.web.bind.annotation.ModelAttribute com.atlashub.identity.adapter.in.web.request.ListSplitRecipientsRequestDto request,
            Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        ListSplitRecipientsQuery query = new ListSplitRecipientsQuery(Long.valueOf(OrganizationIdStr), request.page(), request.size());
        PageResult<SplitRecipientDto> result = listSplitRecipientsUseCase.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sub accounts retrieved successfully", result.content(), new ApiResponse.Meta(result.totalElements(), 0, result.pageSize(), result.pageNumber(), result.totalPages())));
    }
}
