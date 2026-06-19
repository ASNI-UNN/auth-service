package org.asni.auth.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.asni.auth.domain.model.Role;

public record UpdateUserRequest(
        @Size(min = 3, max = 50) String username,
        @Email String email,
        Role role,
        Boolean active
) {}
