package com.Shopping.Shopping.controller.api;

import com.Shopping.Shopping.dto.ApiResponse;
import com.Shopping.Shopping.dto.ProductDTO;
import com.Shopping.Shopping.dto.SellerDTO;
import com.Shopping.Shopping.model.Product;
import com.Shopping.Shopping.model.Seller;
import com.Shopping.Shopping.repository.ProductImageRepository;
import com.Shopping.Shopping.repository.ProductRepository;
import com.Shopping.Shopping.repository.SellerRepository;
import com.Shopping.Shopping.security.JwtTokenProvider;
import com.Shopping.Shopping.service.ProductService;
import com.Shopping.Shopping.service.SellerDetailsService;
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
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/seller")
public class ApiSellerController {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiSellerController.class);

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final SellerDetailsService sellerDetailsService;
    private final ProductImageRepository productImageRepository;

    public ApiSellerController(SellerRepository sellerRepository,
                               ProductRepository productRepository,
                               ProductService productService,
                               PasswordEncoder passwordEncoder,
                               JwtTokenProvider tokenProvider,
                               SellerDetailsService sellerDetailsService,
                               ProductImageRepository productImageRepository) {
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.sellerDetailsService = sellerDetailsService;
        this.productImageRepository = productImageRepository;
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
    public ResponseEntity<ApiResponse<Map<String, Object>>> signup(@RequestBody SellerSignupRequest request) {
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

            // Validate email format
            if (!isValidEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid email format"));
            }

            // Check if email already exists
            Optional<Seller> existingSellerByEmail = sellerRepository.findByEmail(request.getEmail());
            if (existingSellerByEmail.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Email already registered"));
            }

            // Check if username already exists
            Optional<Seller> existingSellerByUsername = sellerRepository.findByUsername(request.getUsername());
            if (existingSellerByUsername.isPresent()) {
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
            
            Map<String, Object> response = new HashMap<>();
            response.put("sellerId", savedSeller.getId());
            response.put("email", savedSeller.getEmail());
            response.put("message", "Registration successful");
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
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
    @Transactional
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

            // Create product with all fields
            Product product = new Product();
            product.setName(request.getProductName());
            product.setBrandName(request.getBrandName());
            product.setCategory(request.getProductCategory());
            product.setSubCategory(request.getSubCategory());
            product.setDescription(request.getProductDescription());
            product.setLongDescription(request.getLongDescription());
            product.setKeyFeatures(request.getKeyFeatures());
            
            // Pricing
            product.setMrp(request.getMrp());
            double sellingPrice = request.getSellingPrice() != null ? request.getSellingPrice() : 
                (request.getProductPrice() != 0 ? request.getProductPrice() : 0.0);
            product.setSellingPrice(sellingPrice);
            product.setPrice(sellingPrice); // Legacy field
            product.setDiscountPercent(request.getDiscountPercent());
            product.setGstIncluded(request.getGstIncluded());
            product.setMinimumOrderQuantity(request.getMinimumOrderQuantity());
            
            // Inventory
            product.setAvailableQuantity(request.getAvailableQuantity());
            product.setSkuId(request.getSkuId());
            product.setStockAvailability(request.getStockAvailability());
            
            // Shipping
            product.setPackageWeight(request.getPackageWeight());
            product.setPackageLength(request.getPackageLength());
            product.setPackageWidth(request.getPackageWidth());
            product.setPackageHeight(request.getPackageHeight());
            product.setPickupAddress(request.getPickupAddress());
            product.setDeliveryMethod(request.getDeliveryMethod());
            
            // Tax & Compliance
            product.setGstNumber(request.getGstNumber() != null ? request.getGstNumber() : seller.getGstNumber());
            product.setHsnCode(request.getHsnCode());
            product.setInvoiceRequired(request.getInvoiceRequired());
            
            // Legal
            product.setBrandAuthorized(request.getBrandAuthorized());
            product.setTrademarkVerified(request.getTrademarkVerified());
            product.setComplianceCertificates(request.getComplianceCertificates());
            
            // Seller Preferences
            product.setReturnPolicy(request.getReturnPolicy());
            product.setReplacementAvailable(request.getReplacementAvailable());
            product.setWarrantyDetails(request.getWarrantyDetails());
            
            // Legacy fields
            product.setUniqueProductId(uniqueProductId);
            product.setSeller(seller);
            product.setSpecifications(request.getSpecifications());

            // Save product first
            Product savedProduct = productRepository.save(product);

            // Handle multiple images
            if (request.getProductImages() != null && !request.getProductImages().isEmpty()) {
                // Parse image types
                List<String> imageTypes = new ArrayList<>();
                if (request.getImageTypes() != null && !request.getImageTypes().trim().isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        imageTypes = mapper.readValue(request.getImageTypes(), 
                            new TypeReference<List<String>>() {});
                    } catch (Exception e) {
                        imageTypes = Arrays.asList(request.getImageTypes().split(","));
                    }
                }
                
                // Save multiple images
                productService.saveProductImages(savedProduct, request.getProductImages(), imageTypes);
            } else if (request.getProductImage() != null && !request.getProductImage().isEmpty()) {
                // Legacy: single image
                productService.saveProduct(savedProduct, request.getProductImage());
            }

            // Save specifications
            if (request.getSpecifications() != null && !request.getSpecifications().trim().isEmpty()) {
                productService.saveProductSpecifications(savedProduct, request.getSpecifications());
            }
            
            // Save variants
            if (request.getVariants() != null && !request.getVariants().trim().isEmpty()) {
                productService.saveProductVariants(savedProduct, request.getVariants());
            }
            
            // Save documents (optional)
            if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
                List<String> docTypes = new ArrayList<>();
                if (request.getDocumentTypes() != null && !request.getDocumentTypes().trim().isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        docTypes = mapper.readValue(request.getDocumentTypes(), 
                            new TypeReference<List<String>>() {});
                    } catch (Exception e) {
                        docTypes = Arrays.asList(request.getDocumentTypes().split(","));
                    }
                }
                productService.saveProductDocuments(savedProduct, request.getDocuments(), docTypes);
            }

            // Flush to ensure all changes are committed to database
            productRepository.flush();
            
            // Reload product with images - explicitly fetch images
            Product productWithImages = productService.getProductById(savedProduct.getId());
            
            // Explicitly load images from repository to ensure they're available for DTO conversion
            List<com.Shopping.Shopping.model.ProductImage> images = 
                productImageRepository.findByProductOrderByDisplayOrderAsc(savedProduct);
            
            if (!images.isEmpty()) {
                logger.info("Loaded {} images for product ID: {}", images.size(), savedProduct.getId());
            } else {
                logger.warn("No images found for product ID: {}", savedProduct.getId());
            }
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product uploaded successfully", 
                    convertProductToDTO(productWithImages, images)));
        } catch (IllegalArgumentException e) {
            // Validation errors (invalid format, size, etc.) return 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            // Other errors return 500 Internal Server Error
            logger.error("Failed to upload product", e);
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

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private ProductDTO convertProductToDTO(Product product) {
        return convertProductToDTO(product, null);
    }
    
    private ProductDTO convertProductToDTO(Product product, List<com.Shopping.Shopping.model.ProductImage> imagesParam) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrandName(product.getBrandName());
        dto.setCategory(product.getCategory());
        dto.setSubCategory(product.getSubCategory());
        dto.setDescription(product.getDescription());
        dto.setLongDescription(product.getLongDescription());
        dto.setKeyFeatures(product.getKeyFeatures());
        
        // Pricing
        dto.setMrp(product.getMrp());
        dto.setSellingPrice(product.getSellingPrice() != null ? product.getSellingPrice() : product.getPrice());
        dto.setPrice(product.getPrice());
        dto.setDiscountPercent(product.getDiscountPercent());
        dto.setGstIncluded(product.getGstIncluded());
        dto.setMinimumOrderQuantity(product.getMinimumOrderQuantity());
        
        // Inventory
        dto.setAvailableQuantity(product.getAvailableQuantity());
        dto.setSkuId(product.getSkuId());
        dto.setStockAvailability(product.getStockAvailability());
        dto.setUniqueProductId(product.getUniqueProductId());
        
        // Shipping
        dto.setPackageWeight(product.getPackageWeight());
        dto.setPackageLength(product.getPackageLength());
        dto.setPackageWidth(product.getPackageWidth());
        dto.setPackageHeight(product.getPackageHeight());
        dto.setPickupAddress(product.getPickupAddress());
        dto.setDeliveryMethod(product.getDeliveryMethod());
        
        // Tax & Compliance
        dto.setGstNumber(product.getGstNumber());
        dto.setHsnCode(product.getHsnCode());
        dto.setInvoiceRequired(product.getInvoiceRequired());
        
        // Legal
        dto.setBrandAuthorized(product.getBrandAuthorized());
        dto.setTrademarkVerified(product.getTrademarkVerified());
        if (product.getComplianceCertificates() != null && !product.getComplianceCertificates().trim().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                dto.setComplianceCertificates(mapper.readValue(product.getComplianceCertificates(), 
                    new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                logger.warn("Failed to parse compliance certificates", e);
            }
        }
        
        // Seller Preferences
        dto.setReturnPolicy(product.getReturnPolicy());
        dto.setReplacementAvailable(product.getReplacementAvailable());
        dto.setWarrantyDetails(product.getWarrantyDetails());
        
        // Handle multiple images - prefer passed images, fallback to product.getImages()
        List<com.Shopping.Shopping.model.ProductImage> images = imagesParam;
        if (images == null || images.isEmpty()) {
            // Try to get from product entity
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                images = new ArrayList<>(product.getImages());
            }
        }
        
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = images.stream()
                .sorted(Comparator.comparing(img -> img.getDisplayOrder() != null ? img.getDisplayOrder() : 0))
                .map(img -> "/product-image/" + product.getId() + "/" + img.getId())
                .collect(Collectors.toList());
            
            dto.setImageUrls(imageUrls);
            dto.setPrimaryImageUrl(imageUrls.get(0));
            dto.setImageUrl(imageUrls.get(0)); // Legacy field for backward compatibility
            logger.debug("Set {} image URLs for product ID: {}", imageUrls.size(), product.getId());
        } else if (product.getImage() != null && product.getImage().length > 0) {
            // Legacy single image
            dto.setImageUrl("/product-image/" + product.getId());
            dto.setPrimaryImageUrl("/product-image/" + product.getId());
            dto.setImageUrls(List.of("/product-image/" + product.getId()));
            logger.debug("Using legacy single image for product ID: {}", product.getId());
        } else {
            logger.warn("No images found for product ID: {}", product.getId());
        }
        
        // Handle specifications
        if (product.getSpecificationsList() != null && !product.getSpecificationsList().isEmpty()) {
            Map<String, String> specs = new HashMap<>();
            product.getSpecificationsList().forEach(spec -> 
                specs.put(spec.getSpecKey(), spec.getSpecValue()));
            dto.setSpecifications(specs);
        }
        
        // Handle variants
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            List<Map<String, Object>> variantList = product.getVariants().stream()
                .map(v -> {
                    Map<String, Object> variant = new HashMap<>();
                    variant.put("id", v.getId());
                    variant.put("type", v.getVariantType());
                    variant.put("value", v.getVariantValue());
                    variant.put("priceModifier", v.getPriceModifier());
                    variant.put("stock", v.getStockQuantity());
                    variant.put("sku", v.getSku());
                    variant.put("isAvailable", v.getIsAvailable());
                    return variant;
                })
                .collect(Collectors.toList());
            dto.setVariants(variantList);
        }
        
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
        // Basic Product Details
        private String productName;
        private String brandName;
        private String productCategory;
        private String subCategory;
        private String productDescription; // Short description
        private String longDescription;
        private String keyFeatures; // Comma-separated or JSON
        
        // Pricing Details
        private Double mrp;
        private Double sellingPrice;
        private double productPrice; // Legacy field (maps to sellingPrice)
        private Double discountPercent;
        private Boolean gstIncluded;
        private Integer minimumOrderQuantity;
        
        // Inventory & Stock
        private Integer availableQuantity;
        private String skuId;
        private String stockAvailability; // "ready", "made-to-order", "pre-order"
        
        // Product Specifications (JSON string)
        private String specifications; // JSON: {"Model Number": "ABC123", "Warranty": "1 Year"}
        
        // Shipping Details
        private Double packageWeight;
        private Double packageLength;
        private Double packageWidth;
        private Double packageHeight;
        private String pickupAddress;
        private String deliveryMethod; // "fulfilled", "self-ship"
        
        // Tax & Compliance
        private String gstNumber;
        private String hsnCode;
        private Boolean invoiceRequired;
        
        // Legal & Brand Info
        private Boolean brandAuthorized;
        private Boolean trademarkVerified;
        private String complianceCertificates; // JSON array: ["BIS", "FSSAI"]
        
        // Seller Preferences
        private String returnPolicy;
        private Boolean replacementAvailable;
        private String warrantyDetails;
        
        // Variants (JSON string)
        private String variants; // JSON: [{"type": "size", "value": "XL", "priceModifier": 0, "stock": 10}]
        
        // Media
        private List<MultipartFile> productImages; // Multiple images
        private String imageTypes; // JSON: ["front", "back", "lifestyle"] or comma-separated
        private MultipartFile productImage; // Legacy single image
        
        // Documents (optional)
        private List<MultipartFile> documents; // Certificates, authorization letters
        private String documentTypes; // JSON: ["brand_authorization", "bis_certificate"] or comma-separated
        
        // Other
        private String uniqueProductId;
    }

    @lombok.Data
    static class LoginRequest {
        private String username;
        private String password;
    }
}
