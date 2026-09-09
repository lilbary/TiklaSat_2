package com.gib.tiklasat.dto;

import com.gib.tiklasat.entity.Role;
import com.gib.tiklasat.entity.User;
import lombok.Data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class UserDto {

    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private Instant createdAt;

    // BR-U-008: admin rol atayıp geri alabilmek için önce kimde hangi rol
    // olduğunu görmeli. Gizli bir bilgi değil — kullanıcının kendi rolleri
    // zaten JWT'sinin içinde açıkça duruyor.
    private Set<Role> roles = new HashSet<>();

    // DİKKAT: 'password' alanını buraya E-K-L-E-M-İ-Y-O-R-U-Z!

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRoles(new HashSet<>(user.getRoles()));

        return dto;
    }
}
