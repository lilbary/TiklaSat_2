package com.gib.tiklasat.user;


import com.gib.tiklasat.user.User;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PublicProfileDto {
    private UUID id;
    private String fullName;
    private Instant memberSince;
//object mapper map struck
    public static PublicProfileDto fromEntity (User user){
        PublicProfileDto dto = new PublicProfileDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setMemberSince(user.getCreatedAt());
        return dto;
    }






}
