package com.gib.tiklasat.auth;

import com.gib.tiklasat.user.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token; // Bilet
    private UserDto user; // Giriş yapan kullanıcının şifresiz bilgileri
}
