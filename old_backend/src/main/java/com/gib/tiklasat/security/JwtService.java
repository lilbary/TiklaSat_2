package com.gib.tiklasat.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT token üretme ve doğrulama servisi (BR-S-002).
 *
 * Access Token: Kısa ömürlü (15 dk), her istekte Authorization header'da gönderilir.
 * Refresh Token: Uzun ömürlü (7 gün), yalnızca /api/auth/refresh endpoint'ine gönderilir.
 *
 * Token içeriği:
 *   - sub: kullanıcı UUID
 *   - email: kullanıcı e-postası
 *   - roles: kullanıcı rolleri (virgülle ayrılmış)
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
            @Value("${tiklasat.jwt.secret}") String secret,
            @Value("${tiklasat.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${tiklasat.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(secret.getBytes())));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    /**
     * Access token üretir.
     * @param userId   Kullanıcı UUID
     * @param email    Kullanıcı e-postası
     * @param roles    Virgülle ayrılmış roller (ör. "BUYER,SELLER")
     */
    public String generateAccessToken(UUID userId, String email, String roles) {
        return buildToken(userId, email, roles, accessTokenExpirationMs);
    }

    /**
     * Refresh token üretir. Bu token veritabanına SHA-256 hash'i ile kaydedilir.
     */
    public String generateRefreshToken(UUID userId, String email, String roles) {
        return buildToken(userId, email, roles, refreshTokenExpirationMs);
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    /**
     * Token'dan kullanıcı UUID'sini çıkarır.
     */
    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).getSubject());
    }

    /**
     * Token'dan e-posta adresini çıkarır.
     */
    public String extractEmail(String token) {
        return extractClaims(token).get("email", String.class);
    }

    /**
     * Token'dan rolleri çıkarır.
     */
    public String extractRoles(String token) {
        return extractClaims(token).get("roles", String.class);
    }

    /**
     * Token geçerli mi? (imza + süre kontrolü)
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Private ──────────────────────────────────────

    private String buildToken(UUID userId, String email, String roles, long expirationMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId.toString())
                .claims(Map.of(
                        "email", email,
                        "roles", roles
                ))
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
