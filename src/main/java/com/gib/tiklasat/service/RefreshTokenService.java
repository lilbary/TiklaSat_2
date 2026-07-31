package com.gib.tiklasat.service;

import com.gib.tiklasat.entity.RefreshToken;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.repository.RefreshTokenRepository;
import com.gib.tiklasat.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Refresh token yönetimi (BR-S-008).
 *
 * Güvenlik stratejisi:
 * - Token'ın kendisi DEĞİL, SHA-256 hash'i veritabanında saklanır.
 * - Token rotasyonu: Kullanılan token revoke edilir, yenisi üretilir.
 * - Eğer revoke edilmiş token tekrar sunulursa → hırsızlık tespiti → tüm zincir iptal.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    /**
     * Yeni refresh token üretir ve hash'ini veritabanına kaydeder.
     */
    @Transactional
    public String createRefreshToken(User user, String roles,
                                      String ipAddress, String userAgent) {
        String rawToken = jwtService.generateRefreshToken(
                user.getId(), user.getEmail(), roles);

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(sha256(rawToken));
        entity.setIssuedAt(Instant.now());
        entity.setExpiresAt(Instant.now().plusMillis(
                jwtService.getRefreshTokenExpirationMs()));
        entity.setIpAddress(ipAddress);
        entity.setUserAgent(userAgent);

        refreshTokenRepository.save(entity);
        return rawToken;
    }

    /**
     * Refresh token'ı doğrular ve rotasyon yapar.
     * Eski token revoke edilir, yeni token üretilir.
     *
     * @return Yeni raw refresh token, geçersizse empty
     */
    @Transactional
    public Optional<String> rotateRefreshToken(String rawToken, User user, String roles,
                                                String ipAddress, String userAgent) {
        String hash = sha256(rawToken);
        Optional<RefreshToken> existing = refreshTokenRepository.findByTokenHash(hash);

        if (existing.isEmpty()) {
            return Optional.empty();
        }

        RefreshToken oldToken = existing.get();

        // Zaten revoke edilmiş token sunuldu → HIRSIZLIK TESPİTİ
        // Tüm kullanıcı token'larını iptal et (BR-S-008)
        if (oldToken.isRevoked()) {
            refreshTokenRepository.deleteByUserId(user.getId());
            return Optional.empty();
        }

        // Süresi geçmiş mi?
        if (oldToken.isExpired()) {
            return Optional.empty();
        }

        // Eski token'ı revoke et
        oldToken.setRevokedAt(Instant.now());

        // Yeni token üret
        String newRawToken = createRefreshToken(user, roles, ipAddress, userAgent);

        return Optional.of(newRawToken);
    }

    /**
     * SHA-256 hash üretir.
     * Token'ın kendisi veritabanında SAKLANMAZ, yalnızca hash'i saklanır.
     */
    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algoritması bulunamadı", e);
        }
    }
}
