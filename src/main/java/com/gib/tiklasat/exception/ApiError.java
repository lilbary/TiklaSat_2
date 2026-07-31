package com.gib.tiklasat.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * API hata yanıt yapısı.
 * Tüm hata durumlarında istemciye tutarlı bir JSON formatı döner.
 */
@Getter
@AllArgsConstructor
public class ApiError {
    private int status;
    private String error;
    private String message;
    private Instant timestamp;

    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, Instant.now());
    }
}
