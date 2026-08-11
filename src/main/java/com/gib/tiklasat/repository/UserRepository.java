package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    // Kullanıcı giriş yaparken (login) e-posta adresinden kullanıcıyı bulmak için
    User findByEmail(String email);
    
    // Kayıt (register) olurken bu e-posta daha önce alınmış mı diye kontrol etmek için
    boolean existsByEmail(String email);
}
