package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    /** Hash ile token bul. Doğrulama linklerinde kullanılır. */
    Optional<VerificationToken> findByTokenHash(String tokenHash);
}
