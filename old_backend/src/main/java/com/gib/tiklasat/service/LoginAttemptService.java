package com.gib.tiklasat.service;

import com.gib.tiklasat.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Kaba kuvvet saldırısı tespiti (BR-S-003).
 *
 * İki seviyeli kontrol:
 * 1. IP bazlı: Aynı IP'den son 15 dakikada 10'dan fazla başarısız deneme → engelle
 * 2. E-posta bazlı: Aynı e-postaya son 15 dakikada 5'ten fazla başarısız deneme → engelle
 *
 * BR-K-005: IP adresi kişisel veridir, 6 ay sonra temizlenir (zamanlanmış iş ile).
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS_PER_IP = 10;
    private static final int MAX_ATTEMPTS_PER_EMAIL = 5;
    private static final int WINDOW_MINUTES = 15;

    private final LoginAttemptRepository loginAttemptRepository;

    /**
     * Bu IP'den giriş denemesine izin verilir mi?
     */
    public boolean isIpBlocked(String ipAddress) {
        Instant windowStart = Instant.now().minus(WINDOW_MINUTES, ChronoUnit.MINUTES);
        long failedAttempts = loginAttemptRepository
                .countByIpAddressAndSuccessfulFalseAndAttemptedAtAfter(ipAddress, windowStart);
        return failedAttempts >= MAX_ATTEMPTS_PER_IP;
    }

    /**
     * Bu e-posta adresi için giriş denemesine izin verilir mi?
     */
    public boolean isEmailBlocked(String email) {
        Instant windowStart = Instant.now().minus(WINDOW_MINUTES, ChronoUnit.MINUTES);
        long failedAttempts = loginAttemptRepository
                .countByEmailAndSuccessfulFalseAndAttemptedAtAfter(email, windowStart);
        return failedAttempts >= MAX_ATTEMPTS_PER_EMAIL;
    }
}
