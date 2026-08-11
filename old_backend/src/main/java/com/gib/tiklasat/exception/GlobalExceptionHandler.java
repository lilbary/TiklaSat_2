package com.gib.tiklasat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Tüm controller'lar için merkezi hata yakalayıcı.
 * Her hata türü için uygun HTTP durum kodu ve tutarlı ApiError formatı döner.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Validasyon hataları (ör. @NotBlank, @Email, @Size) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "VALIDATION_ERROR", message));
    }

    /** Yanlış e-posta veya parola */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(401, "BAD_CREDENTIALS", "E-posta veya parola hatalı"));
    }

    /** Hesap kilitli (BR-S-003) */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiError> handleLocked(LockedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(403, "ACCOUNT_LOCKED",
                        "Hesabınız geçici olarak kilitlenmiştir. Lütfen daha sonra tekrar deneyiniz."));
    }

    /** Hesap devre dışı (ban, suspend) */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(403, "ACCOUNT_DISABLED",
                        "Hesabınız devre dışı bırakılmıştır."));
    }

    /** IllegalArgumentException — iş kuralı ihlalleri */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "BAD_REQUEST", ex.getMessage()));
    }

    /** IllegalStateException — durum tutarsızlıkları */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(409, "CONFLICT", ex.getMessage()));
    }

    /** Beklenmeyen hatalar */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(500, "INTERNAL_ERROR",
                        "Beklenmeyen bir hata oluştu. Lütfen daha sonra tekrar deneyiniz."));
    }
}
