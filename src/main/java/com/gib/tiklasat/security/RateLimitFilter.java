package com.gib.tiklasat.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private static final int MAX_REQUESTS_PER_SECOND = 50; // Saniyede maksimum istek hakkı

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        
        // IP adresini bulamazsak veya yerel testlerde bazı durumlarda boş gelirse diye basit bir kontrol
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = "unknown";
        }
        
        String redisKey = "rate_limit:" + clientIp;

        // Redis'teki sayacı 1 artır
        Long requestsCount = redisTemplate.opsForValue().increment(redisKey);

        if (requestsCount != null && requestsCount == 1) {
            // İlk istekte, sayacın ömrünü 1 saniye yap
            redisTemplate.expire(redisKey, 1, TimeUnit.SECONDS);
        }

        if (requestsCount != null && requestsCount > MAX_REQUESTS_PER_SECOND) {
            // Sınır aşıldı
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Çok fazla istek attınız, lütfen yavaşlayın!");
            return; // Zinciri kır, Controller'a gitmesini engelle
        }

        // Sınır aşılmadıysa normal işleyişe devam et
        filterChain.doFilter(request, response);
    }
}
