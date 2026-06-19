package org.asni.auth.application.command;

import org.asni.auth.domain.model.Role;

public record UpdateUserCommand(String username, String email, Role role, Boolean active) {}
