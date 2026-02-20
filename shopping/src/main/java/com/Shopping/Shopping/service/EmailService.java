package com.Shopping.Shopping.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
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
    
    @Value("${sendgrid.api.key:}")
    private String sendGridApiKey;
    
    @Value("${sendgrid.from.email:}")
    private String sendGridFromEmail;
    
    @Value("${email.provider:SMTP}")
    private String emailProvider; // SMTP or SENDGRID
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public boolean sendOtpEmail(String toEmail, String otp, String userType) {
        // Try SendGrid first if configured, otherwise fall back to SMTP
        if ("SENDGRID".equalsIgnoreCase(emailProvider) && sendGridApiKey != null && !sendGridApiKey.isEmpty()) {
            return sendViaSendGrid(toEmail, otp, userType);
        } else {
            return sendViaSmtp(toEmail, otp, userType);
        }
    }
    
    private boolean sendViaSendGrid(String toEmail, String otp, String userType) {
        try {
            logger.info("Attempting to send email via SendGrid to: {}", toEmail);
            
            if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
                logger.error("SendGrid API key is not configured");
                return false;
            }
            
            String from = sendGridFromEmail != null && !sendGridFromEmail.isEmpty() 
                ? sendGridFromEmail 
                : (fromEmail != null && !fromEmail.isEmpty() ? fromEmail : "noreply@yourdomain.com");
            
            Email fromEmailObj = new Email(from);
            Email toEmailObj = new Email(toEmail);
            String subject = "Email Verification OTP";
            Content content = new Content("text/plain", 
                String.format(
                    "Hello,\n\n" +
                    "Your OTP for %s email verification is: %s\n\n" +
                    "This OTP will expire in 10 minutes.\n\n" +
                    "If you didn't request this, please ignore this email.\n\n" +
                    "Best regards,\nShopping Team",
                    userType, otp
                ));
            
            Mail mail = new Mail(fromEmailObj, subject, toEmailObj, content);
            
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("OTP email sent successfully via SendGrid to: {}", toEmail);
                return true;
            } else {
                logger.error("SendGrid API error: Status {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
                logger.warn("OTP for {} ({}): {} - SendGrid failed, check logs", toEmail, userType, otp);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to send OTP email via SendGrid to: {} - Error: {}", 
                toEmail, e.getMessage(), e);
            logger.warn("OTP for {} ({}): {} - SendGrid error, check logs", toEmail, userType, otp);
            return false;
        }
    }
    
    private boolean sendViaSmtp(String toEmail, String otp, String userType) {
        try {
            logger.info("Attempting to send email via SMTP to: {}", toEmail);
            
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
            
            logger.info("Sending email message via SMTP...");
            mailSender.send(message);
            logger.info("OTP email sent successfully via SMTP to: {}", toEmail);
            return true;
        } catch (org.springframework.mail.MailException e) {
            logger.error("MailException sending OTP email via SMTP to: {} - Error: {}", toEmail, e.getMessage());
            logger.error("Exception class: {}, Cause: {}", e.getClass().getName(), e.getCause());
            logger.warn("OTP for {} ({}): {} - Please check application logs if email service is unavailable", toEmail, userType, otp);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error sending OTP email via SMTP to: {} - Error: {}", toEmail, e.getMessage(), e);
            logger.warn("OTP for {} ({}): {} - Please check application logs if email service is unavailable", toEmail, userType, otp);
            return false;
        }
    }
}
