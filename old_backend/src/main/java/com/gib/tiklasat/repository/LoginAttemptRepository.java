package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    /**
     * Belirli bir IP'den belirli bir tarihten sonra kaç başarısız giriş denemesi yapılmış?
     * Kaba kuvvet tespiti için (BR-S-003).
     * Örnek: Son 15 dakikada 5'ten fazla başarısız deneme → hesap kilitle.
     */
    long countByIpAddressAndSuccessfulFalseAndAttemptedAtAfter(
            String ipAddress, Instant after);

    /**
     * Belirli bir e-postaya belirli bir tarihten sonra kaç başarısız deneme?
     * Hesap bazlı kaba kuvvet tespiti.
     */
    long countByEmailAndSuccessfulFalseAndAttemptedAtAfter(
            String email, Instant after);
}
