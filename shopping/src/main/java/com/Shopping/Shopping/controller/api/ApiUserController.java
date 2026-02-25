package com.Shopping.Shopping.controller.api;

import com.Shopping.Shopping.dto.ApiResponse;
import com.Shopping.Shopping.dto.ProductDTO;
import com.Shopping.Shopping.dto.UserDTO;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.User;
import com.Shopping.Shopping.repository.UserRepository;
import com.Shopping.Shopping.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user")
public class ApiUserController {

    private static final Logger logger = LoggerFactory.getLogger(ApiUserController.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes

    private final UserRepository userRepository;
    private final ProductService productService;

    public ApiUserController(UserRepository userRepository, ProductService productService) {
        this.userRepository = userRepository;
        this.productService = productService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
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

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute UserUpdateRequest request) {
        try {
            logger.info("Profile update request received for user: {}", 
                userDetails != null ? userDetails.getUsername() : "unknown");
            
            if (userDetails == null) {
                logger.warn("Profile update attempted without authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
            }

            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            user.setAlternateNumber(request.getAlternateNumber());
            user.setAddress(request.getAddress());

            // Handle photo upload with validation
            if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
                logger.info("Processing profile photo - Name: {}, Size: {} bytes, Content Type: {}", 
                    request.getPhoto().getOriginalFilename(), 
                    request.getPhoto().getSize(), 
                    request.getPhoto().getContentType());
                
                // Validate photo file
                validatePhotoFile(request.getPhoto());
                
                try {
                    byte[] photoBytes = request.getPhoto().getBytes();
                    user.setPhoto(photoBytes);
                    logger.info("Profile photo processed successfully ({} bytes)", photoBytes.length);
                } catch (IOException e) {
                    logger.error("Failed to read photo file bytes: {}", e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Failed to process photo file: " + e.getMessage()));
                }
            } else {
                logger.info("No photo provided in update request");
            }

            User updatedUser = userRepository.saveAndFlush(user);
            logger.info("Profile updated successfully for user: {}", userDetails.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", convertToDTO(updatedUser)));
        } catch (IllegalArgumentException e) {
            // Validation errors (invalid format, size, etc.) return 400 Bad Request
            logger.warn("Profile update validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Profile update failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during profile update: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update profile: " + e.getMessage()));
        }
    }

    /**
     * Validate profile photo file format and size
     * @param file MultipartFile to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePhotoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Photo file is required");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Photo filename is required");
        }
        
        // Check file extension (JPG/JPEG/PNG allowed for profile photos)
        String fileNameLower = fileName.toLowerCase();
        if (!fileNameLower.endsWith(".jpg") && 
            !fileNameLower.endsWith(".jpeg") && 
            !fileNameLower.endsWith(".png")) {
            throw new IllegalArgumentException(
                "Only JPG, JPEG, and PNG image formats are allowed. Provided: " + fileName);
        }
        
        // Check file size (10MB limit)
        if (file.getSize() > MAX_FILE_SIZE) {
            double sizeInMB = file.getSize() / (1024.0 * 1024.0);
            throw new IllegalArgumentException(
                String.format("Photo size exceeds 10MB limit. Size: %.2f MB", sizeInMB));
        }
        
        // Validate content type if available
        String contentType = file.getContentType();
        if (contentType != null && 
            !contentType.equalsIgnoreCase("image/jpeg") && 
            !contentType.equalsIgnoreCase("image/jpg") &&
            !contentType.equalsIgnoreCase("image/png")) {
            logger.warn("Content type mismatch. Expected image/jpeg or image/png, got: {}", contentType);
        }
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHome(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Not authenticated"));
        }

        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found"));
        }

        // Get user data
        UserDTO userDTO = convertToDTO(userOpt.get());
        
        // Get all products for home page
        List<Product> products = productService.getAllProducts();
        List<ProductDTO> productDTOs = products.stream()
            .map(this::convertProductToDTO)
            .collect(Collectors.toList());
        
        // Get all categories
        List<String> categories = productService.getAllCategories();
        
        // Build home page response
        Map<String, Object> homeData = new HashMap<>();
        homeData.put("user", userDTO);
        homeData.put("products", productDTOs);
        homeData.put("categories", categories);
        
        return ResponseEntity.ok(ApiResponse.success("Home data retrieved successfully", homeData));
    }
    
    private ProductDTO convertProductToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrandName(product.getBrandName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setSellingPrice(product.getSellingPrice() != null ? product.getSellingPrice() : product.getPrice());
        dto.setCategory(product.getCategory());
        dto.setUniqueProductId(product.getUniqueProductId());
        
        // Handle images
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<String> imageUrls = product.getImages().stream()
                .sorted((a, b) -> Integer.compare(
                    a.getDisplayOrder() != null ? a.getDisplayOrder() : 0,
                    b.getDisplayOrder() != null ? b.getDisplayOrder() : 0))
                .map(img -> "/product-image/" + product.getId() + "/" + img.getId())
                .collect(java.util.stream.Collectors.toList());
            dto.setImageUrls(imageUrls);
            dto.setPrimaryImageUrl(imageUrls.get(0));
            dto.setImageUrl(imageUrls.get(0));
        } else {
            dto.setImageUrl("/product-image/" + product.getId());
        }
        
        return dto;
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAlternateNumber(user.getAlternateNumber());
        dto.setAddress(user.getAddress());
        dto.setPhotoBase64(user.getPhotoBase64());
        return dto;
    }

    @lombok.Data
    static class UserUpdateRequest {
        private String alternateNumber;
        private String address;
        private MultipartFile photo;
    }
}
