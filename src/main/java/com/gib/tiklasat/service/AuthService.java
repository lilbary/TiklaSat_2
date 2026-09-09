package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.AuthResponse;
import com.gib.tiklasat.dto.LoginRequest;
import com.gib.tiklasat.dto.UserDto;
import com.gib.tiklasat.dto.UserRegisterDto;
import com.gib.tiklasat.entity.Role;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.exception.ConflictException;
import com.gib.tiklasat.exception.ResourceNotFoundException;
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
        
        // BR-U-001: e-posta duyarsız benzersiz olduğu için normalize edilmiş
        // haliyle saklıyoruz. Arama zaten duyarsız (UserRepository), ama veriyi
        // tek biçimde tutmak token'daki e-postanın da tutarlı olmasını sağlıyor.
        //
        // Locale.ROOT ZORUNLU: sistem locale'i tr_TR olduğunda düz toLowerCase()
        // Türkçe kurallarını uygular ve "TIKLASAT" → "tıklasat" (noktasız ı) olur.
        // E-posta adresi bambaşka bir adrese dönüşür, çakışma tespit edilemez.
        String email = request.getEmail() == null ? null
                : request.getEmail().trim().toLowerCase(java.util.Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Bu e-posta adresi zaten kullanılıyor!");
        }

        User user = new User();
        user.setEmail(email);
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.addRole(Role.BUYER); // BR-U-003: kayıt olan herkes otomatik BUYER
        
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
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // Yeni bir bilet basıp geri döndür
        String jwtToken = jwtService.generateToken(user);
        
        return new AuthResponse(jwtToken, UserDto.fromEntity(user));
    }
}
