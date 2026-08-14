package com.gib.tiklasat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Kendi fırlattığımız RuntimeException'ları (İş Kuralları Hataları) yakala
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        
        // Frontend'in (React) kolayca okuyabileceği temiz bir JSON formatı oluşturuyoruz
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value()); // HTTP 400
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage()); // Bizim yazdığımız mesaj (Örn: "Teklifiniz çok düşük!")

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
