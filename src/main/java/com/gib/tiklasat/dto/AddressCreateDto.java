package com.gib.tiklasat.dto;

import lombok.Data;

@Data
public class AddressCreateDto {
    private String title;
    private String city;
    private String district;
    private String fullAddress;
    private Boolean isDefault;
}