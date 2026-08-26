package com.atlashub.identity.adapter.in.web.controller;

import com.atlashub.identity.application.command.RegenerateApiKeyCommand;
import com.atlashub.identity.application.command.RevokeApiKeyCommand;
import com.atlashub.identity.application.dto.ApiKeyDto;
import com.atlashub.identity.application.query.ListApiKeysQuery;
import com.atlashub.identity.application.usecase.ListApiKeysUseCase;
import com.atlashub.identity.application.usecase.RegenerateApiKeyUseCase;
import com.atlashub.identity.application.usecase.RevokeApiKeyUseCase;
import com.atlashub.identity.domain.model.ApiEnvironment;
import com.atlashub.identity.domain.model.KeyType;
import com.atlashub.identity.adapter.in.web.request.RegenerateApiKeyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import com.atlashub.identity.adapter.in.web.response.ListApiKeysResponseDto;
import com.atlashub.identity.adapter.in.web.response.RegenerateApiKeyResponseDto;
import com.atlashub.identity.adapter.in.web.response.RevokeApiKeyResponseDto;
import com.atlashub.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/keys")
@Tag(name = "API Keys", description = "Organization API key management")
public class ApiKeyController {

    private final ListApiKeysUseCase listApiKeysUseCase;
    private final RegenerateApiKeyUseCase regenerateApiKeyUseCase;
    private final RevokeApiKeyUseCase revokeApiKeyUseCase;

    public ApiKeyController(ListApiKeysUseCase listApiKeysUseCase,
                            RegenerateApiKeyUseCase regenerateApiKeyUseCase,
                            RevokeApiKeyUseCase revokeApiKeyUseCase) {
        this.listApiKeysUseCase = listApiKeysUseCase;
        this.regenerateApiKeyUseCase = regenerateApiKeyUseCase;
        this.revokeApiKeyUseCase = revokeApiKeyUseCase;
    }

    @GetMapping
    @Operation(summary = "List all API keys", description = "Retrieves all active and revoked API keys for the authenticated Organization")
    public ResponseEntity<ApiResponse<ListApiKeysResponseDto>> listKeys(Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        ListApiKeysQuery query = new ListApiKeysQuery(Long.valueOf(OrganizationIdStr));
        List<ApiKeyDto> keys = listApiKeysUseCase.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Keys retrieved successfully", new ListApiKeysResponseDto(keys), null));
    }

    @PostMapping("/regenerate")
    @Operation(summary = "Regenerate an API key", description = "Revokes the active key of the specified type and generates a new one")
    public ResponseEntity<ApiResponse<RegenerateApiKeyResponseDto>> regenerateKey(@Valid @RequestBody RegenerateApiKeyRequest request, Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        RegenerateApiKeyCommand command = new RegenerateApiKeyCommand(
                Long.valueOf(OrganizationIdStr),
                KeyType.valueOf(request.keyType()),
                ApiEnvironment.valueOf(request.environment())
        );
        String rawKey = regenerateApiKeyUseCase.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Key regenerated successfully", new RegenerateApiKeyResponseDto(rawKey), null));
    }

    @DeleteMapping("/{keyId}")
    @Operation(summary = "Revoke an API key", description = "Revokes a specific API key")
    public ResponseEntity<ApiResponse<RevokeApiKeyResponseDto>> revokeKey(@PathVariable String keyId, Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        RevokeApiKeyCommand command = new RevokeApiKeyCommand(
                Long.valueOf(OrganizationIdStr),
                Long.valueOf(keyId)
        );
        revokeApiKeyUseCase.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Key revoked successfully", new RevokeApiKeyResponseDto(keyId, false), null));
    }
}


