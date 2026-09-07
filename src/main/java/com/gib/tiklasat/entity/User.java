package com.gib.tiklasat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    // YENİ EKLENEN KISIMLAR
    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;
    
    // BR-U-003: bir kullanıcı aynı anda birden fazla role sahip olabilir.
    // EAGER kasıtlı: getAuthorities() giriş ve token üretimi sırasında çağrılıyor,
    // LAZY olsaydı session kapandıktan sonra LazyInitializationException riski olurdu.
    // Maliyeti düşük — kullanıcı başına tipik olarak 1-3 satır.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // --- SPRING SECURITY USERDETAILS METOTLARI ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security rolleri "ROLE_" önekiyle bekler; hasRole("ADMIN")
        // aslında "ROLE_ADMIN" yetkisini arıyor.
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }

    /** Rol ekler; zaten varsa hiçbir şey yapmaz (Set olduğu için tekrar oluşmaz). */
    public void addRole(Role role) {
        roles.add(role);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    @Override
    public String getUsername() {
        return email; // Bizim sistemimizde kullanıcı adı Email'dir.
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // mappedBy = "user" -> Address sınıfındaki "user" alanına işaret eder
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Address> addresses = new ArrayList<>();
}
