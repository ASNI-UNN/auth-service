package org.asni.auth.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateTokenRequest(@NotBlank String token) {}
