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
    private final CustomUserDetailsService userDetailsService;

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
            
            // Veritabanından bu Email'e sahip kullanıcıyı bul
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Bilet sahte değilse ve süresi dolmadıysa:
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                // Kapıdan geçiş iznini (Authentication) oluştur
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // Sisteme bu kişiyi "Giriş Yapmış (Onaylı)" olarak kaydet
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // İşlemi bitir ve isteği hedefine yolla
        filterChain.doFilter(request, response);
    }
}
