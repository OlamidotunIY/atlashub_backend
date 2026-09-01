package com.atlashub.identity.adapter.in.web.controller;

import com.atlashub.identity.adapter.in.web.request.CreateUserRequest;
import com.atlashub.identity.adapter.in.web.request.ListUsersRequestDto;
import com.atlashub.identity.application.command.CreateUserCommand;
import com.atlashub.identity.application.query.GetUserQuery;
import com.atlashub.identity.application.query.ListUsersQuery;
import com.atlashub.identity.application.result.CreateUserResult;
import com.atlashub.identity.application.result.UserDto;
import com.atlashub.identity.application.usecase.CreateUserUseCase;
import com.atlashub.identity.application.usecase.GetUserUseCase;
import com.atlashub.identity.application.usecase.ListUsersUseCase;
import com.atlashub.shared.dto.ApiResponse;
import com.atlashub.shared.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
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
    @Operation(summary = "Create a User", description = "Registers a new user on the platform")
    public ResponseEntity<ApiResponse<CreateUserResult>> create(@Valid @RequestBody CreateUserRequest request) {
        CreateUserCommand command = new CreateUserCommand(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.country(),
                request.inviteToken()
        );
        CreateUserResult result = createUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "User created successfully", result, null));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get a User", description = "Retrieves a User by ID")
    public ResponseEntity<ApiResponse<UserDto>> get(@PathVariable String userId, Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        GetUserQuery query = new GetUserQuery(Long.valueOf(OrganizationIdStr), Long.valueOf(userId));
        UserDto result = getUserUseCase.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "User retrieved successfully", result, null));
    }

    @GetMapping
    @Operation(summary = "List Users", description = "Retrieves a paginated list of Users")
    public ResponseEntity<ApiResponse<List<UserDto>>> list(
            @org.springframework.web.bind.annotation.ModelAttribute ListUsersRequestDto request,
            Principal principal) {
        String OrganizationIdStr = principal != null ? principal.getName() : "anonymous";
        ListUsersQuery query = new ListUsersQuery(Long.valueOf(OrganizationIdStr), request.page(), request.size(), null);
        PageResult<UserDto> result = listUsersUseCase.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved successfully", result.content(), new ApiResponse.Meta(result.totalElements(), 0, result.pageSize(), result.pageNumber(), result.totalPages())));
    }
}
