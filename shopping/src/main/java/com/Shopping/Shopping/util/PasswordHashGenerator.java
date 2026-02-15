package com.Shopping.Shopping.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hash
 * Run this once to generate the hash, then update AdminDetailsService
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "ADI@28RSCA";
        String hash = encoder.encode(password);
        System.out.println("========================================");
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("========================================");
        System.out.println("Copy this hash to AdminDetailsService.java");
        
        // Verify the hash works
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification: " + (matches ? "SUCCESS" : "FAILED"));
    }
}
