package com.atlashub.identity.adapter.in.web.controller;

import com.atlashub.identity.application.command.CreateUserCommand;
import com.atlashub.identity.application.dto.UserDto;
import com.atlashub.identity.application.dto.CreateUserResult;
import com.atlashub.identity.application.query.GetUserQuery;
import com.atlashub.identity.application.query.ListUsersQuery;
import com.atlashub.identity.application.usecase.CreateUserUseCase;
import com.atlashub.identity.application.usecase.GetUserUseCase;
import com.atlashub.identity.application.usecase.ListUsersUseCase;
import com.atlashub.identity.adapter.in.web.request.CreateUserRequest;
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
@RequestMapping("/api/v1/Users")
@Tag(name = "Users", description = "User management")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;

    public UserController(CreateUserUseCase createUserUseCase, 
                              GetUserUseCase getUserUseCase,
                              ListUsersUseCase listUsersUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a User", description = "Creates a new User for a Organization")
    public ResponseEntity<ApiResponse<CreateUserResult>> create(@Valid @RequestBody CreateUserRequest request, Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        
        CreateUserCommand command = new CreateUserCommand(
                Long.valueOf(OrganizationIdStr),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.metadata()
        );
        CreateUserResult result = createUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "User created successfully", result, null));
    }

    @GetMapping("/{UserId}")
    @Operation(summary = "Get a User", description = "Retrieves a User by ID")
    public ResponseEntity<ApiResponse<UserDto>> get(@PathVariable String UserId, Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        GetUserQuery query = new GetUserQuery(Long.valueOf(OrganizationIdStr), Long.valueOf(UserId));
        UserDto result = getUserUseCase.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "User retrieved successfully", result, null));
    }
    
    @GetMapping
    @Operation(summary = "List Users", description = "Retrieves a paginated list of Users")
    public ResponseEntity<ApiResponse<List<UserDto>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        ListUsersQuery query = new ListUsersQuery(Long.valueOf(OrganizationIdStr), page, size, null);
        PageResult<UserDto> result = listUsersUseCase.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved successfully", result.content(), new ApiResponse.Meta(result.totalElements(), 0, result.pageSize(), result.pageNumber(), result.totalPages())));
    }
}
