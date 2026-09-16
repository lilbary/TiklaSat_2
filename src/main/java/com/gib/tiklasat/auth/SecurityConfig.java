package com.gib.tiklasat.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final RateLimitFilter rateLimitFilter;

    // Şifreleri (Kriptolayarak) veritabanına kaydetmek için kullanacağımız şifreleyici
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring Security'nin "Kullanıcı adı ve Şifre kontrolünü nereden yapayım?" diye sorduğu yer
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Kullanıcıları buradan bul
        authProvider.setPasswordEncoder(passwordEncoder());     // Şifreleri bununla karşılaştır
        return authProvider;
    }

    // Login (Giriş) işlemi yaparken kullanılacak yönetici
    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // GÜVENLİK KURALLARININ (GÜMRÜK POLİTİKASININ) YAZILDIĞI ANA YER
    // GÜVENLİK KURALLARININ (GÜMRÜK POLİTİKASININ) YAZILDIĞI ANA YER
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. FRONTEND İÇİN CORS İZNİ (Yeni eklediğimiz kısım)
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                    corsConfig.setAllowedOrigins(java.util.List.of("http://localhost:5173", "http://localhost:5174", "http://localhost:5175")); // Frontend'in muhtemel adresleri
                    corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfig.setAllowedHeaders(java.util.List.of("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                .csrf(csrf -> csrf.disable()) // API yazdığımız için CSRF korumasına gerek yok

                // Kimliği doğrulanmamış istek 403 değil 401 almalı.
                // 401 = "kim olduğunu kanıtlayamadın", 403 = "kimliğin belli ama yetkin yok".
                // Bu ayrım olmadan frontend'deki otomatik token yenileme hiç tetiklenmiyordu:
                // AuthContext 401'e bakıyor, biz ise geçersiz/süresi dolmuş token'a da 403 dönüyorduk.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"status\":401,\"error\":\"Unauthorized\","
                                            + "\"message\":\"Oturum açmanız gerekiyor veya süreniz doldu.\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // Spring, 403/500 üretirken isteği içeriden /error'a yönlendiriyor ve bu
                        // yönlendirme güvenlik zincirinden BİR KEZ DAHA geçiyor. STATELESS olduğumuz
                        // için o ikinci geçişte SecurityContext boş; istek anonim sayılıp
                        // anyRequest().authenticated() kuralına takılıyor ve entry point gerçek
                        // 403'ün üstüne 401 yazıyor. Iç yönlendirmeyi kontrolden muaf tutuyoruz.
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()

                        // 1. GİRİŞ VE KAYIT SAYFASI HERKESE AÇIK OLMALI
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/users/*/profile").permitAll()

                        // 2. İLANLARA, KATEGORİLERE, AÇIK ARTIRMALARA VE TEKLİF GEÇMİŞİNE BAKMAK
                        // HERKESE AÇIK (Sadece GET istekleri)
                        .requestMatchers(HttpMethod.GET, "/api/categories/**", "/api/listings/**", "/api/auctions/**", "/api/bids/**").permitAll()

                        // YÜKLENEN FOTOĞRAFLARA ERİŞİM HERKESE AÇIK
                        .requestMatchers("/uploads/**").permitAll()

                        // 2.5. WEBSOCKET BAĞLANTISI (SADECE DİNLEME) HERKESE AÇIK
                        .requestMatchers("/ws-auction/**").permitAll()

                        // SWAGGER UI HERKESE AÇIK
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // 3. ADMIN PANELİ SADECE "ADMIN" ROLÜNE SAHİP OLANLARA AÇIK
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 4. BUNLARIN DIŞINDAKİ HER ŞEY İÇİN KAPI KİLİTLİ (Giriş Yapmak Zorunlu)
                        .anyRequest().authenticated()
                )
                // Sistemimizde oturum (Session) tutmayacağız, her istek kendi Biletini (Token) getirecek (STATELESS)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Kimlik doğrulayıcıyı sisteme tanıtıyoruz
                .authenticationProvider(authenticationProvider())

                // 1. Önce Hız Sınırı (Rate Limit) kalkanından geç
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)

                // 2. Hız sınırını geçtiyse Jwt Kimlik kontrolünden geç
                .addFilterAfter(jwtAuthFilter, RateLimitFilter.class);

        return http.build();
    }
}
