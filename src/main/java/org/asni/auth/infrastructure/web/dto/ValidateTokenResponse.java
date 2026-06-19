package org.asni.auth.infrastructure.web.dto;

import org.asni.auth.application.result.TokenInfo;
import org.asni.auth.domain.model.Role;

import java.util.UUID;

public record ValidateTokenResponse(UUID userId, String username, Role role) {

    public static ValidateTokenResponse from(TokenInfo info) {
        return new ValidateTokenResponse(info.userId(), info.username(), info.role());
    }
}
