package com.Shopping.Shopping.repository;

import com.Shopping.Shopping.model.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    
    Optional<EmailOtp> findByEmailAndOtpAndUserTypeAndUsedFalse(
        String email, String otp, String userType);
    
    @Modifying
    @Query("DELETE FROM EmailOtp e WHERE e.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE EmailOtp e SET e.used = true WHERE e.email = :email AND e.userType = :userType")
    void markAllAsUsed(@Param("email") String email, @Param("userType") String userType);
}
