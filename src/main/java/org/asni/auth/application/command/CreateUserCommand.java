package org.asni.auth.application.command;

import org.asni.auth.domain.model.Role;

public record CreateUserCommand(String username, String email, String password, Role role) {}
