package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.auth.AuthResponse;
import com.gib.tiklasat.dto.auth.LoginRequest;
import com.gib.tiklasat.dto.auth.RegisterRequest;
import com.gib.tiklasat.entity.LoginAttempt;
import com.gib.tiklasat.entity.Role;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.repository.LoginAttemptRepository;
import com.gib.tiklasat.repository.RoleRepository;
import com.gib.tiklasat.repository.UserRepository;
import com.gib.tiklasat.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Kimlik doğrulama iş mantığı.
 *
 * Kayıt akışı:
 * 1. E-posta benzersizliği kontrol et (BR-U-001)
 * 2. Parolayı Argon2id ile hash'le (BR-S-001)
 * 3. BUYER rolü ata (BR-U-003)
 * 4. JWT üret ve döndür
 *
 * Giriş akışı:
 * 1. Kaba kuvvet kontrolü (BR-S-003)
 * 2. E-posta ile kullanıcıyı bul
 * 3. Parolayı doğrula
 * 4. Giriş denemesini kaydet (başarılı/başarısız)
 * 5. JWT üret ve döndür
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    /**
     * Yeni kullanıcı kaydı.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request,
                                  String ipAddress, String userAgent) {
        // 1. E-posta benzersizliği (BR-U-001)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi zaten kayıtlı");
        }

        // 2. Kullanıcı oluştur
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setFullName(request.getFullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPasswordAlgo("ARGON2ID");
        user.setPhone(request.getPhone());
        user.setStatus("PENDING_VERIFICATION"); // BR-U-004

        // 3. BUYER rolü ata (BR-U-003 — herkes varsayılan olarak alıcıdır)
        Role buyerRole = roleRepository.findByCode("BUYER")
                .orElseThrow(() -> new IllegalStateException("BUYER rolü bulunamadı"));
        user.getRoles().add(buyerRole);

        userRepository.save(user);

        // 4. Token üret
        String roles = "BUYER";
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), roles);
        String refreshToken = refreshTokenService.createRefreshToken(
                user, roles, ipAddress, userAgent);

        return AuthResponse.of(accessToken, refreshToken,
                jwtService.getAccessTokenExpirationMs(),
                user.getEmail(), user.getFullName());
    }

    /**
     * Kullanıcı girişi.
     */
    @Transactional
    public AuthResponse login(LoginRequest request,
                               String ipAddress, String userAgent) {
        String email = request.getEmail().toLowerCase().trim();

        // 1. Kaba kuvvet kontrolü (BR-S-003)
        if (loginAttemptService.isIpBlocked(ipAddress)) {
            throw new IllegalStateException(
                    "Çok fazla başarısız giriş denemesi. Lütfen 15 dakika sonra tekrar deneyiniz.");
        }
        if (loginAttemptService.isEmailBlocked(email)) {
            throw new IllegalStateException(
                    "Bu hesap için çok fazla başarısız giriş denemesi. Lütfen 15 dakika sonra tekrar deneyiniz.");
        }

        // 2. Kullanıcıyı bul
        User user = userRepository.findByEmail(email).orElse(null);

        // 3. Kullanıcı yoksa veya parola yanlışsa
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Başarısız denemeyi kaydet
            recordLoginAttempt(email, user, ipAddress, userAgent, false);
            throw new IllegalArgumentException("E-posta veya parola hatalı");
        }

        // 4. Hesap durumu kontrolü (BR-U-007)
        if ("BANNED".equals(user.getStatus()) || "ANONYMIZED".equals(user.getStatus())) {
            throw new IllegalStateException("Hesabınız devre dışı bırakılmıştır");
        }
        if ("SUSPENDED".equals(user.getStatus())) {
            throw new IllegalStateException("Hesabınız geçici olarak askıya alınmıştır");
        }

        // 5. Hesap kilitli mi? (BR-S-003)
        if (user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(java.time.Instant.now())) {
            throw new IllegalStateException("Hesabınız geçici olarak kilitlenmiştir");
        }

        // 6. Başarılı giriş kaydı
        recordLoginAttempt(email, user, ipAddress, userAgent, true);
        user.setLastLoginAt(java.time.Instant.now());

        // 7. Token üret
        String roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.joining(","));

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), roles);
        String refreshToken = refreshTokenService.createRefreshToken(
                user, roles, ipAddress, userAgent);

        return AuthResponse.of(accessToken, refreshToken,
                jwtService.getAccessTokenExpirationMs(),
                user.getEmail(), user.getFullName());
    }

    /**
     * Token yenileme.
     */
    @Transactional
    public AuthResponse refreshToken(String rawRefreshToken,
                                      String ipAddress, String userAgent) {
        // Token'dan kullanıcı bilgisini çıkar
        if (!jwtService.isTokenValid(rawRefreshToken)) {
            throw new IllegalArgumentException("Geçersiz veya süresi dolmuş refresh token");
        }

        java.util.UUID userId = jwtService.extractUserId(rawRefreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

        String roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.joining(","));

        // Rotasyon
        String newRefreshToken = refreshTokenService.rotateRefreshToken(
                rawRefreshToken, user, roles, ipAddress, userAgent)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token geçersiz veya kullanılmış"));

        String newAccessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), roles);

        return AuthResponse.of(newAccessToken, newRefreshToken,
                jwtService.getAccessTokenExpirationMs(),
                user.getEmail(), user.getFullName());
    }

    // ── Private ──────────────────────────────────────

    private void recordLoginAttempt(String email, User user,
                                     String ipAddress, String userAgent,
                                     boolean successful) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(email);
        attempt.setUser(user);
        attempt.setIpAddress(ipAddress);
        attempt.setUserAgent(userAgent);
        attempt.setSuccessful(successful);
        loginAttemptRepository.save(attempt);
    }
}
