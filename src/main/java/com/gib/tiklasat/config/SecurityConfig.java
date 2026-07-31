package com.gib.tiklasat.config;

import com.gib.tiklasat.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security yapılandırması.
 *
 * Temel kararlar:
 * - Oturum: STATELESS (JWT tabanlı, sunucuda session tutulmaz)
 * - Parola: Argon2id (BR-S-001)
 * - CSRF: Devre dışı (stateless API, cookie kullanılmaz)
 * - Ziyaretçi: /api/auth/**, GET /api/listings/**, GET /api/categories/** açık (BR-U-002)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF kapalı — stateless JWT API
            .csrf(AbstractHttpConfigurer::disable)

            // Oturum: stateless
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Yetkilendirme kuralları
            .authorizeHttpRequests(auth -> auth
                // ── Herkese açık (Ziyaretçi = BR-U-002) ──
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/test").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/listings/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                // ── Admin/Moderatör işlemleri ──
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ── Geri kalan her şey kimlik doğrulaması gerektirir ──
                .anyRequest().authenticated()
            )

            // Authentication provider
            .authenticationProvider(authenticationProvider())

            // JWT filtresini UsernamePasswordAuthenticationFilter'dan ÖNCE ekle
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Argon2id parola encoder (BR-S-001).
     * Spring Security'nin yerleşik implementasyonu.
     * Parametreler: saltLength=16, hashLength=32, parallelism=1, memory=65536 (64MB), iterations=3
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 65536, 3);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
