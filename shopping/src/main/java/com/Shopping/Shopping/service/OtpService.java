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
    
    public OtpService(EmailOtpRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }
    
    @Transactional
    public String generateAndSendOtp(String email, String userType) {
        // Generate 6-digit OTP
        String otp = String.format("%06d", random.nextInt(1000000));
        
        // Invalidate previous OTPs for this email
        otpRepository.markAllAsUsed(email, userType);
        
        // Save new OTP
        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setEmail(email);
        emailOtp.setOtp(otp);
        emailOtp.setUserType(userType);
        otpRepository.save(emailOtp);
        
        // Send email (non-blocking - don't fail if email service is unavailable)
        boolean emailSent = emailService.sendOtpEmail(email, otp, userType);
        
        if (emailSent) {
            logger.info("OTP generated and sent to {} for {}", email, userType);
        } else {
            logger.warn("OTP generated for {} ({}) but email failed to send. OTP: {} - Check logs or use resend-otp endpoint", 
                       email, userType, otp);
        }
        
        return otp;
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
