package com.gib.tiklasat.security;

import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security'nin kullanıcı yükleme servisi.
 * E-posta ile kullanıcıyı veritabanından çeker ve
 * Spring Security'nin anlayacağı UserDetails nesnesine dönüştürür.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Kullanıcı bulunamadı: " + email));

        // Rolleri Spring Security authority'lerine dönüştür
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
                .toList();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountLocked(user.getLockedUntil() != null
                        && user.getLockedUntil().isAfter(java.time.Instant.now()))
                .disabled(!"ACTIVE".equals(user.getStatus())
                        && !"PENDING_VERIFICATION".equals(user.getStatus()))
                .build();
    }
}
