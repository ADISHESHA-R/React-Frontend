package com.Shopping.Shopping.controller;

import com.Shopping.Shopping.model.User;
import com.Shopping.Shopping.model.Seller;
import com.Shopping.Shopping.repository.UserRepository;
import com.Shopping.Shopping.repository.SellerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.Shopping.Shopping.service.ProductService;

import java.io.IOException;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final UserRepository userRepo;
    private final SellerRepository sellerRepo;
    private final PasswordEncoder passwordEncoder;
    private final ProductService productService;

    public AuthController(UserRepository userRepo,
                         SellerRepository sellerRepo,
                         PasswordEncoder passwordEncoder,
                         ProductService productService) {
        this.userRepo = userRepo;
        this.sellerRepo = sellerRepo;
        this.passwordEncoder = passwordEncoder;
        this.productService = productService;
    }

    // User Signup Form
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    // User Signup Submit
    @PostMapping("/signup")
    @Transactional
    public String signupSubmit(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam(value = "alternateNumber", required = false) String alternateNumber,
            @RequestParam("address") String address,
            @RequestParam("photo") MultipartFile photoFile,
            Model model) {

        log.info("User signup attempt for username: {}", username);
        
        if (userRepo.findByUsername(username).isPresent()) {
            log.warn("Signup failed - username already exists: {}", username);
            model.addAttribute("error", "Username already exists!");
            model.addAttribute("user", new User());
            return "signup";
        }

        if (!isValidPassword(password)) {
            log.warn("Signup failed - invalid password for username: {}", username);
            model.addAttribute("error", "Password must be at least 8 characters long and include an uppercase letter, lowercase letter, digit, and special character.");
            model.addAttribute("user", new User());
            return "signup";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhoneNumber(phoneNumber);
        user.setAlternateNumber(alternateNumber);
        user.setAddress(address);

        try {
            if (photoFile != null && !photoFile.isEmpty()) {
                user.setPhoto(photoFile.getBytes());
            }
        } catch (IOException e) {
            log.error("Failed to process user photo: {}", e.getMessage());
        }

        try {
            log.info("Attempting to save user: {}", username);
            // Use saveAndFlush to ensure immediate persistence
            User savedUser = userRepo.saveAndFlush(user);
            log.info("User saved and flushed with ID: {}", savedUser.getId());
            
            log.info("User registered successfully: {} with ID: {}", username, savedUser.getId());
            log.info("User password encoded: {}", savedUser.getPassword() != null ? savedUser.getPassword().substring(0, Math.min(20, savedUser.getPassword().length())) + "..." : "null");
            
            return "redirect:/login?signup=success";
        } catch (Exception e) {
            log.error("Error saving user: {}", e.getMessage(), e);
            model.addAttribute("error", "Registration failed. Please try again.");
            model.addAttribute("user", new User());
            return "signup";
        }
    }

    // Seller Signup Form
    @GetMapping("/seller-signup")
    public String sellerSignupForm(Model model) {
        model.addAttribute("seller", new Seller());
        return "seller-signup";
    }

    // Seller Signup Submit
    @PostMapping("/seller-signup")
    @Transactional
    public String sellerSignupSubmit(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("whatsappNumber") String whatsappNumber,
            @RequestParam("businessEmail") String businessEmail,
            @RequestParam("gstNumber") String gstNumber,
            @RequestParam("photo") MultipartFile photoFile,
            Model model) {

        log.info("Seller signup attempt for username: {}", username);
        
        if (sellerRepo.findByUsername(username).isPresent()) {
            log.warn("Seller signup failed - username already exists: {}", username);
            model.addAttribute("error", "Username already exists!");
            model.addAttribute("seller", new Seller());
            return "seller-signup";
        }

        Seller seller = new Seller();
        seller.setUsername(username);
        seller.setPassword(passwordEncoder.encode(password));
        seller.setEmail(email);
        seller.setWhatsappNumber(whatsappNumber);
        seller.setBusinessEmail(businessEmail);
        seller.setGstNumber(gstNumber);

        try {
            if (photoFile != null && !photoFile.isEmpty()) {
                seller.setPhoto(photoFile.getBytes());
            }
        } catch (IOException e) {
            log.error("Failed to process seller photo: {}", e.getMessage());
        }

        try {
            log.info("Attempting to save seller: {}", username);
            // Use saveAndFlush to ensure immediate persistence
            Seller savedSeller = sellerRepo.saveAndFlush(seller);
            log.info("Seller saved and flushed with ID: {}", savedSeller.getId());
            log.info("Seller registered successfully: {}", username);
            return "redirect:/seller-login";
        } catch (Exception e) {
            log.error("Error saving seller: {}", e.getMessage(), e);
            model.addAttribute("error", "Registration failed. Please try again.");
            model.addAttribute("seller", new Seller());
            return "seller-signup";
        }
    }

    // User Login Page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Seller Login Page
    @GetMapping("/seller-login")
    public String sellerLoginPage() {
        return "seller-login";
    }

    // Profile for Users
    @GetMapping("/profile")
    public String profileForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("userBase64", user.getPhotoBase64());
        return "profile";
    }

    // Profile for Sellers
    @GetMapping("/seller-profile")
    public String sellerProfileForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Seller seller = sellerRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Seller not found"));
        model.addAttribute("seller", seller);
        model.addAttribute("sellerBase64", seller.getPhotoBase64());
        return "seller-profile";
    }

    // Profile Update for Users
    @PostMapping("/profile")
    @Transactional
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam("alternateNumber") String alternateNumber,
                                @RequestParam("address") String address,
                                @RequestParam("photo") MultipartFile photo) {
        log.info("Updating profile for user: {}", userDetails.getUsername());
        User user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setAlternateNumber(alternateNumber);
        user.setAddress(address);
        try {
            if (!photo.isEmpty()) {
                user.setPhoto(photo.getBytes());
                log.debug("Profile photo updated for user: {}", userDetails.getUsername());
            }
        } catch (IOException e) {
            log.error("Failed to update profile photo: {}", e.getMessage());
        }
        userRepo.save(user);
        log.info("Profile updated successfully for user: {}", userDetails.getUsername());
        return "redirect:/profile";
    }

    // Profile Update for Sellers
    @PostMapping("/seller-profile")
    @Transactional
    public String updateSellerProfile(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam("whatsappNumber") String whatsappNumber,
                                      @RequestParam("businessEmail") String businessEmail,
                                      @RequestParam("gstNumber") String gstNumber,
                                      @RequestParam("photo") MultipartFile photo) {
        log.info("Updating profile for seller: {}", userDetails.getUsername());
        Seller seller = sellerRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Seller not found"));
        seller.setWhatsappNumber(whatsappNumber);
        seller.setBusinessEmail(businessEmail);
        seller.setGstNumber(gstNumber);
        try {
            if (!photo.isEmpty()) {
                seller.setPhoto(photo.getBytes());
                log.debug("Profile photo updated for seller: {}", userDetails.getUsername());
            }
        } catch (IOException e) {
            log.error("Failed to update seller profile photo: {}", e.getMessage());
        }
        sellerRepo.save(seller);
        log.info("Profile updated successfully for seller: {}", userDetails.getUsername());
        return "redirect:/seller-profile";
    }
    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        User user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Add user and product list to the model
        model.addAttribute("user", user);
        model.addAttribute("products", productService.getAllProducts());

        return "home";
    }

    // Home for Sellers
    @GetMapping("/seller-home")
    public String sellerHome(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/seller-login";
        Seller seller = sellerRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Seller not found"));
        model.addAttribute("seller", seller);
        return "seller-home";
    }

    // Password validation logic
    private boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*()_+=<>?].*");
    }
}
