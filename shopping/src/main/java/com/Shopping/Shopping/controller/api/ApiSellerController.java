package com.Shopping.Shopping.controller.api;

import com.Shopping.Shopping.dto.ApiResponse;
import com.Shopping.Shopping.dto.ProductDTO;
import com.Shopping.Shopping.dto.SellerDTO;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.Seller;
import com.Shopping.Shopping.repository.ProductRepository;
import com.Shopping.Shopping.repository.SellerRepository;
import com.Shopping.Shopping.security.JwtTokenProvider;
import com.Shopping.Shopping.service.ProductService;
import com.Shopping.Shopping.service.SellerDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/seller")
public class ApiSellerController {

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final SellerDetailsService sellerDetailsService;

    public ApiSellerController(SellerRepository sellerRepository,
                               ProductRepository productRepository,
                               ProductService productService,
                               PasswordEncoder passwordEncoder,
                               JwtTokenProvider tokenProvider,
                               SellerDetailsService sellerDetailsService) {
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.sellerDetailsService = sellerDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        try {
            // Load seller details directly (avoiding AuthenticationManager loop)
            UserDetails userDetails = sellerDetailsService.loadUserByUsername(request.getUsername());
            
            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid username or password"));
            }

            // Generate token
            String token = tokenProvider.generateToken(userDetails);

            Optional<Seller> sellerOpt = sellerRepository.findByUsername(request.getUsername());
            SellerDTO sellerDTO = sellerOpt.map(this::convertToDTO).orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("seller", sellerDTO);

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SellerDTO>> signup(@RequestBody SellerSignupRequest request) {
        try {
            // Validate required fields
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Username is required"));
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Password is required"));
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Email is required"));
            }

            if (sellerRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Username already exists"));
            }

            if (!isValidPassword(request.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Password must be at least 8 characters long and include an uppercase letter, lowercase letter, digit, and special character."));
            }

            Seller seller = new Seller();
            seller.setUsername(request.getUsername());
            seller.setPassword(passwordEncoder.encode(request.getPassword()));
            seller.setEmail(request.getEmail());
            seller.setWhatsappNumber(request.getWhatsappNumber());
            seller.setBusinessEmail(request.getBusinessEmail());
            seller.setGstNumber(request.getGstNumber());

            // Photo handling - if provided as base64 (for JSON requests)
            if (request.getPhotoBase64() != null && !request.getPhotoBase64().trim().isEmpty()) {
                // Handle base64 photo if provided
                try {
                    byte[] photoBytes = java.util.Base64.getDecoder().decode(request.getPhotoBase64());
                    seller.setPhoto(photoBytes);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Invalid photo format. Please provide a valid base64 encoded image."));
                }
            }

            Seller savedSeller = sellerRepository.saveAndFlush(seller);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seller registered successfully", convertToDTO(savedSeller)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<SellerDTO>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Not authenticated"));
        }

        Optional<Seller> sellerOpt = sellerRepository.findByUsername(userDetails.getUsername());
        if (sellerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Seller not found"));
        }

        return ResponseEntity.ok(ApiResponse.success(convertToDTO(sellerOpt.get())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<SellerDTO>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute SellerUpdateRequest request) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
            }

            Seller seller = sellerRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

            seller.setWhatsappNumber(request.getWhatsappNumber());
            seller.setBusinessEmail(request.getBusinessEmail());
            seller.setGstNumber(request.getGstNumber());

            if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
                seller.setPhoto(request.getPhoto().getBytes());
            }

            Seller updatedSeller = sellerRepository.saveAndFlush(seller);
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", convertToDTO(updatedSeller)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update profile: " + e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<List<String>>> getDashboard() {
        try {
            List<String> categories = productService.getAllCategories();
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch categories: " + e.getMessage()));
        }
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductDTO>> uploadProduct(
            @ModelAttribute ProductUploadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
            }

            Seller seller = sellerRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

            String uniqueProductId = request.getUniqueProductId();
            if (uniqueProductId == null || uniqueProductId.trim().isEmpty()) {
                uniqueProductId = "PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            }

            Product product = new Product(
                request.getProductName(),
                request.getProductDescription(),
                request.getProductPrice(),
                request.getProductImage().getOriginalFilename(),
                request.getProductCategory(),
                uniqueProductId,
                seller
            );

            productService.saveProduct(product, request.getProductImage());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product uploaded successfully", convertProductToDTO(product)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to upload product: " + e.getMessage()));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getMyProducts(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
            }

            Seller seller = sellerRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

            List<Product> products = productRepository.findBySeller(seller);
            List<ProductDTO> productDTOs = products.stream()
                .map(this::convertProductToDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(productDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch products: " + e.getMessage()));
        }
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<SellerDTO>> getHome(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Not authenticated"));
        }

        Optional<Seller> sellerOpt = sellerRepository.findByUsername(userDetails.getUsername());
        if (sellerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Seller not found"));
        }

        return ResponseEntity.ok(ApiResponse.success(convertToDTO(sellerOpt.get())));
    }

    private SellerDTO convertToDTO(Seller seller) {
        SellerDTO dto = new SellerDTO();
        dto.setId(seller.getId());
        dto.setUsername(seller.getUsername());
        dto.setEmail(seller.getEmail());
        dto.setWhatsappNumber(seller.getWhatsappNumber());
        dto.setBusinessEmail(seller.getBusinessEmail());
        dto.setGstNumber(seller.getGstNumber());
        dto.setPhotoBase64(seller.getPhotoBase64());
        return dto;
    }

    private ProductDTO convertProductToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory());
        dto.setUniqueProductId(product.getUniqueProductId());
        dto.setImageUrl("/product-image/" + product.getId());
        return dto;
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return password.length() >= 8 &&
            password.matches(".*[A-Z].*") &&
            password.matches(".*[a-z].*") &&
            password.matches(".*\\d.*") &&
            password.matches(".*[!@#$%^&*()_+=<>?].*");
    }

    @lombok.Data
    static class SellerSignupRequest {
        private String username;
        private String password;
        private String email;
        private String whatsappNumber;
        private String businessEmail;
        private String gstNumber;
        private String photoBase64; // For JSON base64 encoded images (optional)
    }

    @lombok.Data
    static class SellerUpdateRequest {
        private String whatsappNumber;
        private String businessEmail;
        private String gstNumber;
        private MultipartFile photo;
    }

    @lombok.Data
    static class ProductUploadRequest {
        private String productName;
        private String productDescription;
        private double productPrice;
        private String productCategory;
        private String uniqueProductId;
        private MultipartFile productImage;
    }

    @lombok.Data
    static class LoginRequest {
        private String username;
        private String password;
    }
}
