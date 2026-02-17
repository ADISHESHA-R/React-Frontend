package com.Shopping.Shopping.controller.api;

import com.Shopping.Shopping.dto.ApiResponse;
import com.Shopping.Shopping.dto.UserDTO;
import com.Shopping.Shopping.model.User;
import com.Shopping.Shopping.repository.UserRepository;
import com.Shopping.Shopping.security.JwtTokenProvider;
import com.Shopping.Shopping.service.UserDetailsServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class ApiAuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    public ApiAuthController(UserRepository userRepository, 
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider,
                           UserDetailsServiceImpl userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserDTO>> signup(@RequestBody SignupRequest request) {
        try {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Username already exists"));
            }

            if (!isValidPassword(request.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Password must be at least 8 characters long and include an uppercase letter, lowercase letter, digit, and special character."));
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhoneNumber(request.getPhoneNumber());
            user.setAddress(request.getAddress());
            
            User savedUser = userRepository.saveAndFlush(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", convertToDTO(savedUser)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Registration failed: " + e.getMessage()));
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

            // Generate token
            String token = tokenProvider.generateToken(userDetails);

            Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
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

    @lombok.Data
    static class SignupRequest {
        private String username;
        private String password;
        private String phoneNumber;
        private String address;
    }

    @lombok.Data
    static class LoginRequest {
        private String username;
        private String password;
    }
}
