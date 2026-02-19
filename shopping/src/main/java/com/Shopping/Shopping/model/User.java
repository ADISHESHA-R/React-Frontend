package com.Shopping.Shopping.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    private String email;
    
    @Column(nullable = true)
    private Boolean emailVerified = false;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(columnDefinition = "BYTEA")
    private byte[] photo;

    public String getPhotoBase64() {
        return this.photo != null ? Base64.getEncoder().encodeToString(this.photo) : null;
    }
    
    // Helper method for backward compatibility
    public boolean isEmailVerified() {
        return emailVerified != null && emailVerified;
    }
}
