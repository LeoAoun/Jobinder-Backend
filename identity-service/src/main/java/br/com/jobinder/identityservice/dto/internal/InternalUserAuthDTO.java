package br.com.jobinder.identityservice.dto.internal;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record InternalUserAuthDTO(
        @NotBlank UUID id,
        @NotBlank String phone,
        @NotBlank String hashedPassword,
        @NotBlank String role
) {}
