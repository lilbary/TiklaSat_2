package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.UserDto;
import com.gib.tiklasat.dto.UserRegisterDto;
import com.gib.tiklasat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // KULLANICI KAYIT OLMA (REGISTER) İSTEĞİNİ KARŞILAR
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserRegisterDto request) {
        return ResponseEntity.ok(userService.register(request));
    }

    // TARAYICIDAN KULLANICILARI GÖRMEK İÇİN (TEST AMAÇLI)
    @GetMapping
    public ResponseEntity<java.util.List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
