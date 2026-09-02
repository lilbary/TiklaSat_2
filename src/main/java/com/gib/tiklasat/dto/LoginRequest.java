package com.gib.tiklasat.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private boolean rememberMe;
}
