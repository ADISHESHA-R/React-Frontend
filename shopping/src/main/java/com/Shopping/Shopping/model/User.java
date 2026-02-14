package com.Shopping.Shopping.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Base64;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    
    private String password;
    
    private String phoneNumber;
    
    private String alternateNumber;
    
    private String address;

    @Lob
    @Column(columnDefinition = "BYTEA")
    private byte[] photo;

    public String getPhotoBase64() {
        return this.photo != null ? Base64.getEncoder().encodeToString(this.photo) : null;
    }
}
