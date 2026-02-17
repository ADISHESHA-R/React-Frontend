package com.Shopping.Shopping.controller.api;

import com.Shopping.Shopping.dto.ApiResponse;
import com.Shopping.Shopping.dto.ProductDTO;
import com.Shopping.Shopping.dto.SellerDTO;
import com.Shopping.Shopping.dto.UserDTO;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.Seller;
import com.Shopping.Shopping.model.User;
import com.Shopping.Shopping.repository.ProductRepository;
import com.Shopping.Shopping.repository.SellerRepository;
import com.Shopping.Shopping.repository.UserRepository;
import com.Shopping.Shopping.security.JwtTokenProvider;
import com.Shopping.Shopping.service.AdminDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
public class ApiAdminController {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AdminDetailsService adminDetailsService;

    public ApiAdminController(UserRepository userRepository,
                             SellerRepository sellerRepository,
                             ProductRepository productRepository,
                             JwtTokenProvider tokenProvider,
                             PasswordEncoder passwordEncoder,
                             AdminDetailsService adminDetailsService) {
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.adminDetailsService = adminDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        try {
            // Load admin details directly (avoiding AuthenticationManager loop)
            UserDetails userDetails = adminDetailsService.loadUserByUsername(request.getUsername());
            
            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid username or password"));
            }

            // Generate token
            String token = tokenProvider.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("username", userDetails.getUsername());
            response.put("roles", userDetails.getAuthorities());

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            List<UserDTO> userDTOs = users.stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(userDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch users: " + e.getMessage()));
        }
    }

    @GetMapping("/sellers")
    public ResponseEntity<ApiResponse<List<SellerDTO>>> getAllSellers() {
        try {
            List<Seller> sellers = sellerRepository.findAll();
            List<SellerDTO> sellerDTOs = sellers.stream()
                .map(this::convertSellerToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(sellerDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch sellers: " + e.getMessage()));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            List<ProductDTO> productDTOs = products.stream()
                .map(this::convertProductToDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(productDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to fetch products: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {
        try {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
            user.setUsername(request.getUsername());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setAlternateNumber(request.getAlternateNumber());
            user.setAddress(request.getAddress());
            userRepository.saveAndFlush(user);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", convertUserToDTO(user)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update user: " + e.getMessage()));
        }
    }

    @PutMapping("/sellers/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<SellerDTO>> updateSeller(
            @PathVariable Long id,
            @RequestBody SellerUpdateRequest request) {
        try {
            Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
            seller.setUsername(request.getUsername());
            seller.setEmail(request.getEmail());
            seller.setWhatsappNumber(request.getWhatsappNumber());
            seller.setBusinessEmail(request.getBusinessEmail());
            seller.setGstNumber(request.getGstNumber());
            sellerRepository.saveAndFlush(seller);
            return ResponseEntity.ok(ApiResponse.success("Seller updated successfully", convertSellerToDTO(seller)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update seller: " + e.getMessage()));
        }
    }

    @PutMapping("/products/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request) {
        try {
            Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setCategory(request.getCategory());
            if (request.getUniqueProductId() != null) {
                product.setUniqueProductId(request.getUniqueProductId());
            }
            productRepository.saveAndFlush(product);
            return ResponseEntity.ok(ApiResponse.success("Product updated successfully", convertProductToDTO(product)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update product: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            userRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/sellers/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteSeller(@PathVariable Long id) {
        try {
            sellerRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Seller deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete seller: " + e.getMessage()));
        }
    }

    @DeleteMapping("/products/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        try {
            productRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete product: " + e.getMessage()));
        }
    }

    private UserDTO convertUserToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAlternateNumber(user.getAlternateNumber());
        dto.setAddress(user.getAddress());
        dto.setPhotoBase64(user.getPhotoBase64());
        return dto;
    }

    private SellerDTO convertSellerToDTO(Seller seller) {
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

    @lombok.Data
    static class UserUpdateRequest {
        private String username;
        private String phoneNumber;
        private String alternateNumber;
        private String address;
    }

    @lombok.Data
    static class SellerUpdateRequest {
        private String username;
        private String email;
        private String whatsappNumber;
        private String businessEmail;
        private String gstNumber;
    }

    @lombok.Data
    static class ProductUpdateRequest {
        private String name;
        private String description;
        private double price;
        private String category;
        private String uniqueProductId;
    }

    @lombok.Data
    static class LoginRequest {
        private String username;
        private String password;
    }
}
