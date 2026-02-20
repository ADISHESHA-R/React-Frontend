package com.Shopping.Shopping.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public boolean sendOtpEmail(String toEmail, String otp, String userType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Email Verification OTP");
            message.setText(String.format(
                "Hello,\n\n" +
                "Your OTP for %s email verification is: %s\n\n" +
                "This OTP will expire in 10 minutes.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\nShopping Team",
                userType, otp
            ));
            
            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", toEmail);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send OTP email to: {} - Error: {}", toEmail, e.getMessage());
            logger.warn("OTP for {} ({}): {} - Please check application logs if email service is unavailable", toEmail, userType, otp);
            // Don't throw exception - allow signup to succeed even if email fails
            // OTP is still saved in database and can be retrieved via resend-otp
            return false;
        }
    }
}
