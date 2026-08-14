package com.gib.tiklasat.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    // ARTIK VERİTABANINA İHTİYACIMIZ YOK, CustomUserDetailsService'i sildik!

    // GÜMRÜK KAPISI: Her internet isteği buradaki filtreden geçer.
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Bilet (Token) yoksa veya yanlış formatta gelmişse, kapıdan geçmesine izin ver ama "Misafir" (Yetkisiz) olarak.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " kelimesinden sonrasını (Bileti) al.
        jwt = authHeader.substring(7);
        // Biletten Email adresini oku
        userEmail = jwtService.extractUsername(jwt);

        // Eğer biletten Email çıktıysa ve sistemde o an kimse giriş yapmamış görünüyorsa:
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Bilet sahte değilse ve süresi dolmadıysa (DB'ye HİÇ SORMADAN!):
            if (jwtService.isTokenValid(jwt)) {
                
                // Biletten Rolleri (Örn: ROLE_USER) çek
                java.util.List<String> roles = jwtService.extractRoles(jwt);
                
                // Spring'in anladığı Yetki listesine çevir
                java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = 
                    roles != null ? roles.stream()
                         .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                         .collect(java.util.stream.Collectors.toList()) 
                    : java.util.Collections.emptyList();

                // Kapıdan geçiş iznini sadece E-posta ve Roller ile oluştur (UserDetails'e gerek kalmadı)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail,
                        null,
                        authorities
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // Sisteme bu kişiyi "Giriş Yapmış" olarak kaydet
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // İşlemi bitir ve isteği hedefine yolla
        filterChain.doFilter(request, response);
    }
}
