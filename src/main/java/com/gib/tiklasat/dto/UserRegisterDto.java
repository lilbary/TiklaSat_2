package com.gib.tiklasat.dto;

import lombok.Data;

@Data
public class UserRegisterDto {
    private String email;
    private String fullName;
    private String password;
    private String phone;
}
