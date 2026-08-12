package com.gib.tiklasat.dto;

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
