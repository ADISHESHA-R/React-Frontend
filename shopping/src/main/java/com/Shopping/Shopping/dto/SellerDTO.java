package com.Shopping.Shopping.dto;

import lombok.Data;

@Data
public class SellerDTO {
    private Long id;
    private String username;
    private String email;
    private boolean emailVerified;
    private String whatsappNumber;
    private String businessEmail;
    private String gstNumber;
    private String photoBase64;
}
