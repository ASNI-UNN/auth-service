package org.asni.auth.application.result;

import org.asni.auth.domain.model.Role;

import java.util.UUID;

public record TokenInfo(UUID userId, String username, Role role) {}
