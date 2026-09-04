package com.gib.tiklasat.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;



//bu acces ve refresh tokendan ayrı olan bir passwordResetToken
@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private final StringRedisTemplate redisTemplate;
    private static final String REDIS_PREFIX = "RESET:";

    // Token üretir ve Redis'e 15 dakikalığına kaydeder
    public String createToken(String email) {
        String token = UUID.randomUUID().toString();

        // Key: "RESET:abc-123-xyz"  Value: "ornek@mail.com"  Süre: 15 Dakika
        redisTemplate.opsForValue().set(
                REDIS_PREFIX + token,
                email,
                15,
                TimeUnit.MINUTES
        );

        return token;
    }

    // Token'ı doğrular, geçerliyse e-posta adresini döner
    public String validateAndGetEmail(String token) {
        String email = redisTemplate.opsForValue().get(REDIS_PREFIX + token);
        if (email == null) {
            throw new RuntimeException("Şifre sıfırlama bağlantısı geçersiz veya süresi dolmuş!");
        }
        return email;
    }

    // Şifre sıfırlandıktan sonra token'ı siler (tek kullanımlık)
    public void deleteToken(String token) {
        redisTemplate.delete(REDIS_PREFIX + token);
    }
}