package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.AuthResponse;
import com.gib.tiklasat.dto.LoginRequest;
import com.gib.tiklasat.dto.UserRegisterDto;
import com.gib.tiklasat.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth") // Güvenlik ayarlarında bu adresi Herkese Açık yapmıştık
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // KAYIT OL
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserRegisterDto request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // GİRİŞ YAP
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
