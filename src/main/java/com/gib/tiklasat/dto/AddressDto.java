package com.gib.tiklasat.dto;

import com.gib.tiklasat.entity.Address;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AddressDto {
    private UUID id;
    private String title;
    private String city;
    private String district;
    private String fullAddress;
    private Instant createdAt;
    private boolean isDefault;

    // Veritabanı objesini, dışarı yollanacak DTO'ya çevirir
    public static AddressDto fromEntity(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setTitle(address.getTitle());
        dto.setCity(address.getCity());
        dto.setDistrict(address.getDistrict());
        dto.setFullAddress(address.getFullAddress());
        dto.setCreatedAt(address.getCreatedAt());
        dto.setDefault(address.isDefault());
        return dto;
    }
}