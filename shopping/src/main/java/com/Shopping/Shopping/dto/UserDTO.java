package com.Shopping.Shopping.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private boolean emailVerified;
    private String phoneNumber;
    private String alternateNumber;
    private String address;
    private String photoBase64;
}
