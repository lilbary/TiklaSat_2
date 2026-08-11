package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /** E-posta ile kullanıcı bul. Login ve kayıt kontrolünde kullanılır. */
    Optional<User> findByEmail(String email);

    /** E-posta zaten kayıtlı mı? Kayıt sırasında kontrol (BR-U-001). */
    boolean existsByEmail(String email);
}
