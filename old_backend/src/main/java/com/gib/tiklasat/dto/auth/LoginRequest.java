package com.gib.tiklasat.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Giriş isteği DTO'su.
 */
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;

    @NotBlank(message = "Parola boş olamaz")
    private String password;
}
