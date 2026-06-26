package com.wordforge.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

class AuthDto {

    record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8) String password,
            String nativeLang
    ) {}

    record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    record TokenResponse(String token, Long userId, String email) {}
}
