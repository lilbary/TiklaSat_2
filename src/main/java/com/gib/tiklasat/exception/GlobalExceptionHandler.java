package com.gib.tiklasat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Kaynak bulunamadı (ör. "Açık artırma bulunamadı!") → 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Yasak işlem (ör. "Kendi ilanınıza teklif veremezsiniz") → 403
    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenActionException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    // Mevcut durumla çelişen istek (ör. "E-posta zaten kullanılıyor", "Teklif çok düşük") → 409
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // Yanlış email/şifre — Spring Security'nin authenticationManager.authenticate() sırasında
    // kendiliğinden fırlattığı hata → 401
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse("E-posta veya şifre hatalı", HttpStatus.UNAUTHORIZED);
    }

    // Yukarıdakilerin hiçbirine uymayan, gerçekten "kullanıcı girdisi hatalı" durumlar → 400
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // @Valid işaretli bir gövde kuralları karşılamadığında Spring bunu fırlatır → 400
    // (ör. BR-U-006 parola gücü, e-posta formatı, zorunlu alanlar).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        // Alan bazlı tüm hatalar: frontend istediği alanın altına yazabilsin.
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        // "message" alanı diğer hatalarla aynı formatta kalsın diye ilk mesajı
        // oraya da koyuyoruz — mevcut frontend kodu yalnızca onu okuyor.
        String firstMessage = fieldErrors.values().stream().findFirst()
                .orElse("Gönderilen bilgiler geçersiz.");

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", firstMessage);
        body.put("fieldErrors", fieldErrors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Ortak JSON gövdesini tek yerden üreten yardımcı metot
    private ResponseEntity<Map<String, Object>> buildResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        return new ResponseEntity<>(errorResponse, status);
    }
}
