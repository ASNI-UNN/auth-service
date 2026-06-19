package org.asni.auth.application.result;

import org.asni.auth.domain.model.Role;

import java.util.UUID;

public record AuthResult(String token, UUID userId, String username, Role role) {}
