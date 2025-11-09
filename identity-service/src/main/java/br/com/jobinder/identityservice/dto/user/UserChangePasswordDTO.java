package br.com.jobinder.identityservice.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserChangePasswordDTO(
        @NotBlank String oldPassword,
        @NotBlank String newPassword
) {}