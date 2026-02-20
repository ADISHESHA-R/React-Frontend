package com.Shopping.Shopping.service;

import com.Shopping.Shopping.model.EmailOtp;
import com.Shopping.Shopping.repository.EmailOtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {
    
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    
    private final EmailOtpRepository otpRepository;
    private final EmailService emailService;
    private final Random random = new Random();
    
    // Result class to return both OTP and email status
    public static class OtpResult {
        private final String otp;
        private final boolean emailSent;
        
        public OtpResult(String otp, boolean emailSent) {
            this.otp = otp;
            this.emailSent = emailSent;
        }
        
        public String getOtp() {
            return otp;
        }
        
        public boolean isEmailSent() {
            return emailSent;
        }
    }
    
    public OtpService(EmailOtpRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }
    
    @Transactional
    public OtpResult generateAndSendOtp(String email, String userType) {
        try {
            logger.info("=== OTP GENERATION STARTED ===");
            logger.info("Email: {}, UserType: {}", email, userType);
            
            // Generate 6-digit OTP
            String otp = String.format("%06d", random.nextInt(1000000));
            logger.info("Generated OTP: {}", otp);
            
            // Invalidate previous OTPs for this email
            logger.info("Invalidating previous OTPs for email: {}", email);
            otpRepository.markAllAsUsed(email, userType);
            logger.info("Previous OTPs invalidated");
            
            // Save new OTP
            EmailOtp emailOtp = new EmailOtp();
            emailOtp.setEmail(email);
            emailOtp.setOtp(otp);
            emailOtp.setUserType(userType);
            
            logger.info("Saving OTP to database...");
            EmailOtp savedOtp = otpRepository.save(emailOtp);
            logger.info("OTP saved successfully with ID: {}", savedOtp.getId());
            logger.info("OTP expires at: {}", savedOtp.getExpiresAt());
            
            // Send email (non-blocking - don't fail if email service is unavailable)
            logger.info("Attempting to send email...");
            boolean emailSent = emailService.sendOtpEmail(email, otp, userType);
            
            if (emailSent) {
                logger.info("=== OTP GENERATION COMPLETE - Email sent successfully ===");
                logger.info("OTP generated and sent to {} for {}", email, userType);
            } else {
                logger.warn("=== OTP GENERATION COMPLETE - Email failed but OTP saved ===");
                logger.warn("OTP generated for {} ({}) but email failed to send. OTP: {} - Check logs or use resend-otp endpoint", 
                           email, userType, otp);
            }
            
            return new OtpResult(otp, emailSent);
        } catch (Exception e) {
            logger.error("=== OTP GENERATION FAILED ===");
            logger.error("Error generating OTP for email: {}, userType: {}", email, userType, e);
            throw new RuntimeException("Failed to generate OTP: " + e.getMessage(), e);
        }
    }
    
    public boolean verifyOtp(String email, String otp, String userType) {
        Optional<EmailOtp> otpOpt = otpRepository.findByEmailAndOtpAndUserTypeAndUsedFalse(
            email, otp, userType);
        
        if (otpOpt.isEmpty()) {
            logger.warn("Invalid OTP for email: {}", email);
            return false;
        }
        
        EmailOtp emailOtp = otpOpt.get();
        
        // Check if OTP is expired
        if (emailOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.warn("Expired OTP for email: {}", email);
            return false;
        }
        
        // Mark as used
        emailOtp.setUsed(true);
        otpRepository.save(emailOtp);
        
        logger.info("OTP verified successfully for email: {}", email);
        return true;
    }
    
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
        logger.info("Cleaned up expired OTPs");
    }
}
