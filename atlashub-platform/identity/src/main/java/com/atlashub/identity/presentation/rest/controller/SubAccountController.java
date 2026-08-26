package com.atlashub.identity.presentation.rest.controller;

import com.atlashub.identity.application.command.RegisterSubAccountCommand;
import com.atlashub.identity.application.dto.RegisterSubAccountResult;
import com.atlashub.identity.application.dto.SubAccountDto;
import com.atlashub.identity.application.query.GetSubAccountQuery;
import com.atlashub.identity.application.query.ListSubAccountsQuery;
import com.atlashub.identity.application.usecase.GetSubAccountUseCase;
import com.atlashub.identity.application.usecase.ListSubAccountsUseCase;
import com.atlashub.identity.application.usecase.RegisterSubAccountUseCase;
import com.atlashub.identity.presentation.rest.request.RegisterSubAccountRequest;
import com.atlashub.shared.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import com.atlashub.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/subaccounts")
@Tag(name = "Sub Accounts", description = "Sub Account management")
public class SubAccountController {

    private final RegisterSubAccountUseCase registerSubAccountUseCase;
    private final GetSubAccountUseCase getSubAccountUseCase;
    private final ListSubAccountsUseCase listSubAccountsUseCase;

    public SubAccountController(RegisterSubAccountUseCase registerSubAccountUseCase,
                                GetSubAccountUseCase getSubAccountUseCase,
                                ListSubAccountsUseCase listSubAccountsUseCase) {
        this.registerSubAccountUseCase = registerSubAccountUseCase;
        this.getSubAccountUseCase = getSubAccountUseCase;
        this.listSubAccountsUseCase = listSubAccountsUseCase;
    }

    @PostMapping
    @Operation(summary = "Register a sub account", description = "Registers a new sub account for a merchant")
    public ResponseEntity<ApiResponse<RegisterSubAccountResult>> register(@Valid @RequestBody RegisterSubAccountRequest request, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        RegisterSubAccountCommand command = new RegisterSubAccountCommand(
                Long.valueOf(merchantIdStr),
                request.bankCode(),
                request.accountNumber(),
                request.description()
        );
        RegisterSubAccountResult result = registerSubAccountUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Sub account created successfully", result, null));
    }
    
    @GetMapping("/{subAccountId}")
    @Operation(summary = "Get a sub account", description = "Retrieves a sub account by ID")
    public ResponseEntity<ApiResponse<SubAccountDto>> get(@PathVariable String subAccountId, Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        GetSubAccountQuery query = new GetSubAccountQuery(Long.valueOf(merchantIdStr), Long.valueOf(subAccountId));
        SubAccountDto result = getSubAccountUseCase.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sub account retrieved successfully", result, null));
    }
    
    @GetMapping
    @Operation(summary = "List sub accounts", description = "Retrieves a paginated list of sub accounts")
    public ResponseEntity<ApiResponse<List<SubAccountDto>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        String merchantIdStr = principal != null ? principal.getName() : "anonymous";
        ListSubAccountsQuery query = new ListSubAccountsQuery(Long.valueOf(merchantIdStr), page, size);
        PageResult<SubAccountDto> result = listSubAccountsUseCase.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sub accounts retrieved successfully", result.content(), new ApiResponse.Meta(result.totalElements(), 0, result.pageSize(), result.pageNumber(), result.totalPages())));
    }
}
