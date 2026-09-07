package com.gib.tiklasat.dto;

import com.gib.tiklasat.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Sıfırlama anahtarı zorunludur.")
    private String token;

    // BR-U-006 kayıtla sınırlı değil: parola sıfırlarken de aynı güç kuralı geçerli,
    // yoksa kullanıcı sıfırlama akışından zayıf bir parolaya geçebilirdi.
    @StrongPassword
    private String newPassword;
}
