package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // BR-U-001: e-posta büyük/küçük harf duyarsız. Karşılaştırmayı metot adına
    // bırakmak yerine sorguyu elle yazıyoruz, çünkü Spring Data'nın ürettiği
    // IgnoreCase varyantı upper() kullanıyor ve V25'teki lower(email) index'ine
    // oturmuyor. Bu haliyle sol taraf index ifadesiyle birebir eşleşiyor.
    @Query("SELECT u FROM User u WHERE lower(u.email) = lower(:email)")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT count(u) > 0 FROM User u WHERE lower(u.email) = lower(:email)")
    boolean existsByEmail(@Param("email") String email);
}
