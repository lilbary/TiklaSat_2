package com.gib.tiklasat.dto;

import com.gib.tiklasat.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterDto {

    @NotBlank(message = "E-posta adresi zorunludur.")
    @Email(message = "Geçerli bir e-posta adresi giriniz.")
    private String email;

    @NotBlank(message = "Ad soyad zorunludur.")
    private String fullName;

    @StrongPassword // BR-U-006
    private String password;

    @NotBlank(message = "Telefon numarası zorunludur.")
    private String phone;
}
