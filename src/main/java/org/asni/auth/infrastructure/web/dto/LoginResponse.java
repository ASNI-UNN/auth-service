package org.asni.auth.infrastructure.web.dto;

import org.asni.auth.application.result.AuthResult;
import org.asni.auth.domain.model.Role;

import java.util.UUID;

public record LoginResponse(String token, UUID userId, String username, Role role) {

    public static LoginResponse from(AuthResult result) {
        return new LoginResponse(result.token(), result.userId(), result.username(), result.role());
    }
}
