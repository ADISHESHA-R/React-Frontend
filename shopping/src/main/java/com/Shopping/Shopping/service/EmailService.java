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
            logger.info("Attempting to send email to: {}", toEmail);
            
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
            
            logger.info("Sending email message...");
            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", toEmail);
            return true;
        } catch (org.springframework.mail.MailException e) {
            logger.error("MailException sending OTP email to: {} - Error: {}", toEmail, e.getMessage());
            logger.error("Exception class: {}, Cause: {}", e.getClass().getName(), e.getCause());
            logger.warn("OTP for {} ({}): {} - Please check application logs if email service is unavailable", toEmail, userType, otp);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error sending OTP email to: {} - Error: {}", toEmail, e.getMessage(), e);
            logger.warn("OTP for {} ({}): {} - Please check application logs if email service is unavailable", toEmail, userType, otp);
            return false;
        }
    }
}
