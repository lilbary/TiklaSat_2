package com.gib.tiklasat.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Kimlik doğrulama yanıt DTO'su.
 * Hem kayıt hem giriş sonrası döner.
 */
@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private String email;
    private String fullName;

    public static AuthResponse of(String accessToken, String refreshToken,
                                   long expiresInMs, String email, String fullName) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresInMs / 1000) // saniye cinsinden
                .email(email)
                .fullName(fullName)
                .build();
    }
}
