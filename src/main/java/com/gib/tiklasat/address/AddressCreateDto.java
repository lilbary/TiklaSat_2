package com.gib.tiklasat.address;

import lombok.Data;

@Data
public class AddressCreateDto {
    private String title;
    private String city;
    private String district;
    private String fullAddress;
    private Boolean isDefault;
}