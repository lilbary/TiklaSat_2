package com.gib.tiklasat.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Token yenileme isteği DTO'su.
 * BR-S-008: Kullanılan refresh token revoke edilir, yeni çift üretilir.
 */
@Getter
@Setter
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token boş olamaz")
    private String refreshToken;
}
