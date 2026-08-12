package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.AuthResponse;
import com.gib.tiklasat.dto.LoginRequest;
import com.gib.tiklasat.dto.UserDto;
import com.gib.tiklasat.dto.UserRegisterDto;
import com.gib.tiklasat.entity.Role;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.repository.UserRepository;
import com.gib.tiklasat.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // 1. YENİ KULLANICI KAYDI
    public AuthResponse register(UserRegisterDto request) {
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu e-posta adresi zaten kullanılıyor!");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(Role.USER);
        
        // ŞİFREYİ KRİPTOLAYARAK KAYDEDİYORUZ! (En büyük fark bu)
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user = userRepository.save(user);

        // Kullanıcıya biletini (Token) basıyoruz
        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken, UserDto.fromEntity(user));
    }

    // 2. SİSTEME GİRİŞ YAPMA (LOGIN)
    public AuthResponse login(LoginRequest request) {
        
        // Spring Security bizim yerimize şifrelerin eşleşip eşleşmediğini kontrol eder
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Şifre doğruysa kullanıcıyı veritabanından bul
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Yeni bir bilet basıp geri döndür
        String jwtToken = jwtService.generateToken(user);
        
        return new AuthResponse(jwtToken, UserDto.fromEntity(user));
    }
}
