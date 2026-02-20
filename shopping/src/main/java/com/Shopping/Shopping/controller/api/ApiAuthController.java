package com.Shopping.Shopping.controller.api;

import com.Shopping.Shopping.dto.ApiResponse;
import com.Shopping.Shopping.dto.UserDTO;
import com.Shopping.Shopping.model.User;
import com.Shopping.Shopping.repository.UserRepository;
import com.Shopping.Shopping.security.JwtTokenProvider;
import com.Shopping.Shopping.service.OtpService;
import com.Shopping.Shopping.service.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/auth")
public class ApiAuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiAuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final OtpService otpService;

    public ApiAuthController(UserRepository userRepository, 
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider,
                           UserDetailsServiceImpl userDetailsService,
                           OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.otpService = otpService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signup(@RequestBody SignupRequest request) {
        try {
            // Validate email
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Email is required"));
            }
            
            if (!isValidEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid email format"));
            }
            
            // Check if email already exists
            Optional<User> existingUserByEmail = userRepository.findByEmail(request.getEmail());
            if (existingUserByEmail.isPresent()) {
                User existingUser = existingUserByEmail.get();
                // If email is verified, reject registration
                if (existingUser.isEmailVerified()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Email already registered and verified"));
                }
                // If email exists but not verified, delete old record to allow re-registration
                userRepository.delete(existingUser);
            }
            
            // Check if username already exists (only if verified)
            Optional<User> existingUserByUsername = userRepository.findByUsername(request.getUsername());
            if (existingUserByUsername.isPresent()) {
                User existingUser = existingUserByUsername.get();
                // If username exists and email is verified, reject
                if (existingUser.isEmailVerified()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Username already exists"));
                }
                // If username exists but email not verified, delete old record
                userRepository.delete(existingUser);
            }

            if (!isValidPassword(request.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Password must be at least 8 characters long and include an uppercase letter, lowercase letter, digit, and special character."));
            }

            // Create user but mark as unverified
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setAddress(request.getAddress());
            user.setEmailVerified(false);
            
            User savedUser = userRepository.saveAndFlush(user);
            logger.info("User saved successfully with ID: {}, Email: {}", savedUser.getId(), savedUser.getEmail());
            
            // Generate and send OTP
            logger.info("Calling OTP service to generate and send OTP for email: {}", request.getEmail());
            OtpService.OtpResult otpResult = otpService.generateAndSendOtp(request.getEmail(), "USER");
            logger.info("OTP service returned. OTP generated: {}, Email sent: {}", 
                       otpResult != null ? "YES" : "NO", 
                       otpResult != null && otpResult.isEmailSent());
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());
            
            if (otpResult != null && otpResult.isEmailSent()) {
                response.put("message", "Registration successful. Please verify your email with the OTP sent to your email address.");
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("OTP sent to your email. Please verify to complete registration.", response));
            } else {
                // Email failed - return OTP in response for testing (can be removed in production)
                response.put("message", "Registration successful. OTP generated but email delivery failed. Please check logs or use resend-otp endpoint.");
                response.put("otp", otpResult != null ? otpResult.getOtp() : "N/A");
                response.put("emailDeliveryFailed", true);
                logger.warn("Email delivery failed for user signup. Returning OTP in response for testing: {}", 
                           otpResult != null ? otpResult.getOtp() : "N/A");
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Registration successful. Email delivery failed - OTP available in response.", response));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyEmail(@RequestBody VerifyEmailRequest request) {
        try {
            if (request.getEmail() == null || request.getOtp() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Email and OTP are required"));
            }
            
            // Verify OTP
            if (!otpService.verifyOtp(request.getEmail(), request.getOtp(), "USER")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid or expired OTP"));
            }
            
            // Find user by email
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
            
            // Mark email as verified
            User user = userOpt.get();
            user.setEmailVerified(true);
            userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Email verified successfully");
            response.put("user", convertToDTO(user));
            
            return ResponseEntity.ok(ApiResponse.success("Email verified successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Verification failed: " + e.getMessage()));
        }
    }

    @PostMapping("/resend-otp")
    @Transactional(readOnly = false)
    public ResponseEntity<ApiResponse<Map<String, Object>>> resendOtp(@RequestBody ResendOtpRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Email is required"));
            }
            
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
            
            User user = userOpt.get();
            if (user.isEmailVerified()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Email already verified"));
            }
            
            // Generate and send new OTP (this method already has @Transactional)
            OtpService.OtpResult otpResult = otpService.generateAndSendOtp(request.getEmail(), "USER");
            
            Map<String, Object> response = new HashMap<>();
            if (otpResult != null && otpResult.isEmailSent()) {
                response.put("message", "OTP resent to your email");
            } else {
                response.put("message", "OTP generated but email delivery failed. Please check logs or try again.");
                response.put("otp", otpResult != null ? otpResult.getOtp() : "N/A");
                response.put("emailDeliveryFailed", true);
            }
            
            return ResponseEntity.ok(ApiResponse.success("OTP resent successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to resend OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        try {
            // Load user details directly (avoiding AuthenticationManager loop)
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            
            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid username or password"));
            }

            // Check if email is verified (only if email exists - grandfather existing users)
            Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Only check email verification if email exists (backward compatibility)
                if (user.getEmail() != null && !user.getEmail().isEmpty() && !user.isEmailVerified()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Please verify your email before logging in. Check your email for OTP or use /resend-otp endpoint."));
                }
            }

            // Generate token
            String token = tokenProvider.generateToken(userDetails);

            UserDTO userDTO = userOpt.map(this::convertToDTO).orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("user", userDTO);

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Not authenticated"));
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found"));
        }

        return ResponseEntity.ok(ApiResponse.success(convertToDTO(userOpt.get())));
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAlternateNumber(user.getAlternateNumber());
        dto.setAddress(user.getAddress());
        dto.setPhotoBase64(user.getPhotoBase64());
        return dto;
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
            password.matches(".*[A-Z].*") &&
            password.matches(".*[a-z].*") &&
            password.matches(".*\\d.*") &&
            password.matches(".*[!@#$%^&*()_+=<>?].*");
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    @lombok.Data
    static class SignupRequest {
        private String username;
        private String password;
        private String email;
        private String phoneNumber;
        private String address;
    }

    @lombok.Data
    static class LoginRequest {
        private String username;
        private String password;
    }

    @lombok.Data
    static class VerifyEmailRequest {
        private String email;
        private String otp;
    }

    @lombok.Data
    static class ResendOtpRequest {
        private String email;
    }
}
