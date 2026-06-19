package org.asni.auth.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.asni.auth.application.command.LoginCommand;
import org.asni.auth.domain.exception.InvalidCredentialsException;
import org.asni.auth.domain.port.in.AuthUseCase;
import org.asni.auth.infrastructure.web.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Authentication endpoints")
class AuthController {

    private final AuthUseCase authUseCase;

    AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive JWT token")
    ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        var result = authUseCase.login(new LoginCommand(request.username(), request.password()));
        return ResponseEntity.ok(LoginResponse.from(result));
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate current token")
    ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        authUseCase.logout(extractToken(authHeader));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate token — used by other services")
    ResponseEntity<ValidateTokenResponse> validate(@RequestBody @Valid ValidateTokenRequest request) {
        var info = authUseCase.validateToken(request.token());
        return ResponseEntity.ok(ValidateTokenResponse.from(info));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info from token")
    ResponseEntity<ValidateTokenResponse> me(@RequestHeader("Authorization") String authHeader) {
        var info = authUseCase.validateToken(extractToken(authHeader));
        return ResponseEntity.ok(ValidateTokenResponse.from(info));
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException();
        }
        return authHeader.substring(7);
    }
}
