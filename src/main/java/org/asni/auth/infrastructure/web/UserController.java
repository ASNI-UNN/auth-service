package org.asni.auth.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.asni.auth.application.command.CreateUserCommand;
import org.asni.auth.application.command.UpdateUserCommand;
import org.asni.auth.domain.port.in.UserUseCase;
import org.asni.auth.infrastructure.web.dto.CreateUserRequest;
import org.asni.auth.infrastructure.web.dto.UpdateUserRequest;
import org.asni.auth.infrastructure.web.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management — Admin only")
class UserController {

    private final UserUseCase userUseCase;

    UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new user")
    ResponseEntity<UserResponse> create(@RequestBody @Valid CreateUserRequest request) {
        var user = userUseCase.createUser(
                new CreateUserCommand(request.username(), request.email(), request.password(), request.role())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users")
    ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> users = userUseCase.findAll().stream().map(UserResponse::from).toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal == #id")
    @Operation(summary = "Get user by id")
    ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(UserResponse.from(userUseCase.findById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user")
    ResponseEntity<UserResponse> update(@PathVariable UUID id,
                                        @RequestBody @Valid UpdateUserRequest request) {
        var user = userUseCase.update(id,
                new UpdateUserCommand(request.username(), request.email(), request.role(), request.active()));
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        userUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
