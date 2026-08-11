package com.gib.tiklasat.dto;

import com.gib.tiklasat.entity.User;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserDto {
    
    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private Instant createdAt;
    
    // DİKKAT: 'password' alanını buraya E-K-L-E-M-İ-Y-O-R-U-Z!

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setCreatedAt(user.getCreatedAt());
        
        return dto;
    }
}
