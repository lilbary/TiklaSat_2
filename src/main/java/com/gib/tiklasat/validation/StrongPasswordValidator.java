package com.gib.tiklasat.validation;

import com.gib.tiklasat.security.PasswordBlacklist;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 10;

    // Jakarta Validation validator'ları Spring bean'i olabiliyor, bu sayede
    // kara listeyi enjekte edip her doğrulamada yeniden yüklemekten kurtuluyoruz.
    private final PasswordBlacklist blacklist;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return fail(context, "Parola boş olamaz.");
        }

        if (password.length() < MIN_LENGTH) {
            return fail(context, "Parola en az " + MIN_LENGTH + " karakter olmalıdır.");
        }

        // Tek geçişte üç koşulu birden topluyoruz — parola üzerinde üç ayrı
        // tarama yapmaya gerek yok.
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        if (!hasUpper || !hasLower || !hasDigit) {
            return fail(context, "Parola en az bir büyük harf, bir küçük harf ve bir rakam içermelidir.");
        }

        if (blacklist.contains(password)) {
            return fail(context, "Bu parola çok yaygın kullanılıyor, lütfen farklı bir parola seçin.");
        }

        return true;
    }

    /**
     * Varsayılan mesajı kapatıp ihlal edilen kurala özel mesajı koyuyoruz;
     * kullanıcı "parola geçersiz" yerine tam olarak neyi düzeltmesi gerektiğini
     * görüyor.
     */
    private boolean fail(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
