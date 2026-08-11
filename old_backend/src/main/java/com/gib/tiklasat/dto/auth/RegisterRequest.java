package com.gib.tiklasat.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Kayıt isteği DTO'su.
 * BR-U-001: E-posta benzersiz olmalı.
 * BR-U-004: E-posta doğrulaması gerekli (kayıt sonrası).
 * BR-S-001: Parola minimum 8 karakter.
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Ad soyad boş olamaz")
    @Size(max = 100, message = "Ad soyad en fazla 100 karakter olabilir")
    private String fullName;

    @NotBlank(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;

    @NotBlank(message = "Parola boş olamaz")
    @Size(min = 8, max = 128, message = "Parola 8-128 karakter arasında olmalı")
    private String password;

    @Size(max = 20, message = "Telefon en fazla 20 karakter olabilir")
    private String phone;
}
