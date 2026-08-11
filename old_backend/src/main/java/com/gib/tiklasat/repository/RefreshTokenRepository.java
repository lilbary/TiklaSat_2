package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /** SHA-256 hash ile token bul. Token doğrulama sırasında kullanılır. */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Bir kullanıcının tüm aktif token'larını iptal etmek için (güvenlik) */
    int deleteByUserId(UUID userId);
}
