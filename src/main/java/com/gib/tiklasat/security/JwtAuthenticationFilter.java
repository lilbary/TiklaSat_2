package com.gib.tiklasat.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Her HTTP isteğinde çalışan JWT doğrulama filtresi.
 *
 * Akış:
 * 1. Authorization header'dan "Bearer <token>" al
 * 2. Token geçerli mi kontrol et (imza + süre)
 * 3. Geçerliyse → SecurityContext'e authentication koy
 * 4. Geçersizse → filtre zincirini devam ettir (Spring Security 401 döner)
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Authorization header'ı kontrol et
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Token'ı çıkar
        String token = authHeader.substring(7);

        // 3. Token geçerli mi?
        if (jwtService.isTokenValid(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String email = jwtService.extractEmail(token);
            String roles = jwtService.extractRoles(token);

            // Rollerden authority listesi oluştur
            var authorities = Arrays.stream(roles.split(","))
                    .filter(r -> !r.isBlank())
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.trim()))
                    .toList();

            // 4. Authentication oluştur ve SecurityContext'e koy
            var authentication = new UsernamePasswordAuthenticationToken(
                    email, null, authorities);
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
