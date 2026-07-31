package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.auth.AuthResponse;
import com.gib.tiklasat.dto.auth.LoginRequest;
import com.gib.tiklasat.dto.auth.RefreshTokenRequest;
import com.gib.tiklasat.dto.auth.RegisterRequest;
import com.gib.tiklasat.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kimlik doğrulama endpoint'leri.
 *
 * POST /api/auth/register  → Yeni kullanıcı kaydı
 * POST /api/auth/login     → Giriş
 * POST /api/auth/refresh   → Token yenileme
 *
 * Tüm endpoint'ler herkese açık (SecurityConfig'de permitAll).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Yeni kullanıcı kaydı.
     * @return 201 CREATED + JWT token çifti
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.register(
                request,
                getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent"));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Kullanıcı girişi.
     * @return 200 OK + JWT token çifti
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.login(
                request,
                getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent"));

        return ResponseEntity.ok(response);
    }

    /**
     * Token yenileme (BR-S-008 rotasyon).
     * @return 200 OK + Yeni JWT token çifti
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.refreshToken(
                request.getRefreshToken(),
                getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent"));

        return ResponseEntity.ok(response);
    }

    // ── Private ──────────────────────────────────────

    /**
     * İstemci IP adresini çıkarır.
     * Proxy/load balancer arkasındaysa X-Forwarded-For header'ını kullanır.
     */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
